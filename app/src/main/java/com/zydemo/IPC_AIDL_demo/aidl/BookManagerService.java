package com.zydemo.IPC_AIDL_demo.aidl;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zydemo.IPC_AIDL_demo.aidl.Book;
import com.zydemo.IPC_AIDL_demo.aidl.IBookManager;
import com.zydemo.IPC_AIDL_demo.aidl.IOnNewBookArrivedListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 *
 * Created by Administrator on 2016/12/14.
 */

public class BookManagerService extends Service {

    private static final String TAG = "BMS";
    private AtomicBoolean mIsDestroyed = new AtomicBoolean(false);

    //支持并发读写的List
    private CopyOnWriteArrayList<Book> mBookList
            = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<IOnNewBookArrivedListener> mListeners
            = new RemoteCallbackList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Binder mBinder = new IBookManager.Stub(){

        @Override
        public List<Book> getBookList() throws RemoteException {
            //运行在 #服务端# binder线程池中，需处理并发问题。
            // 故用到了CopyOnWriteArrayList
            return mBookList;
        }
        @Override
        public void addBook(Book book) throws RemoteException {
            //运行在 #服务端# binder线程池中，需处理并发问题。
            //故用到了CopyOnWriteArrayList
            mBookList.add(book);

        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (!mListeners.contains(listener)){
//                mListeners.add(listener);
//                Log.d(TAG, "register done");
//            }else {
//                Log.d(TAG, "already exists");
//            }

            mListeners.register(listener);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {

            //以下代码实际上无法达到解注册的目地
            //对象(此处的listener)在跨进程的传输过程中，
            //实际上是序列化与反序列化的过程
            //于是Binder把客户端传过来的对象转化成了新的对象
            //导致 mListeners.contains(listener) 始终为false


//            if (mListeners.contains(listener)){
//                mListeners.remove(listener);
//                Log.d(TAG, "unregister done");
//            }else {
//                Log.d(TAG, "Not Found ! unregister failed");
//            }
//            Log.d(TAG, "unregisterListener: current size = "+mListeners.size());


            //解决方法：使用系统提供的专门用于跨进程删除listener的RemoteCallbackList

            /**
             *  RemoteCallbackList的特点
             * 1. 由于底层binder是同一个不变，就通过当前Binder找到listener并删除
             * 2. 当客户端进程终止时，它自动移除客户端注册的listener
             * 3. 它内部已实现了线程同步
             * 4. 他并不是一个List
             */

            mListeners.unregister(listener);
            //plus：关于 mListeners.unregister(listener)的实现

//            public boolean unregister(E callback) {
//                synchronized (mCallbacks) {    //3.线程同步
//                    Callback cb = mCallbacks.remove(callback.asBinder());
//                    if (cb != null) {
//                        cb.mCallback.asBinder().unlinkToDeath(cb, 0);
//                        return true;
//                    }
//                    return false;
//                }
//            }

        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            //验证权限
            int granted = checkCallingOrSelfPermission("com.zydemo.IPC_AIDL_demo.permission.ACCESS_BOOK_SERVICE");
            if (granted == PackageManager.PERMISSION_DENIED){
                return false;
            }
            //验证进程包名
            String packageName = null;
            String[] packages = getPackageManager().getPackagesForUid(getCallingUid());

            if (packages != null && packages.length>0){
                packageName = packages[0];
            }

            if (packageName == null || !packageName.startsWith("com.zydemo")){
                return false;
            }

            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1,"Android"));
        mBookList.add(new Book(2,"Ios"));
        new Thread(new ServiceWorker()).start();
    }

    @Override
    public void onDestroy() {
        mIsDestroyed.set(true);
        super.onDestroy();
    }

    private class ServiceWorker implements Runnable {
        @Override
        public void run() {
            //在一个新的线程中执行
            //每隔5秒向书库中增加一本新书，并通知所有订阅的客户端

            while (!mIsDestroyed.get()){

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int bookId = mBookList.size()+1;
                Book newBook = new Book(bookId,"new Book#" + bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void onNewBookArrived(Book newBook) throws RemoteException {


        mBookList.add(newBook);
//        for (int i = 0; i < mListeners.size(); i++) {
//            IOnNewBookArrivedListener listener = mListeners.get(i);
//            listener.onNewBookArrived(newBook);
//        }

        //开始通知

        final int N = mListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnNewBookArrivedListener listener = mListeners.getBroadcastItem(i);
            if (listener != null) {
                try {
                    //如果客户端的onNewBookArrived方法太耗时将引起服务端ANR
                    listener.onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        //通知完毕
        //与beginBroadcast()成对使用
        mListeners.finishBroadcast();
    }
}
