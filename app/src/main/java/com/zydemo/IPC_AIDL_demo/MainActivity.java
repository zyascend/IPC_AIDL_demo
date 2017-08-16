package com.zydemo.IPC_AIDL_demo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.zydemo.IPC_AIDL_demo.aidl.Book;
import com.zydemo.IPC_AIDL_demo.aidl.BookManagerService;
import com.zydemo.IPC_AIDL_demo.aidl.IBookManager;
import com.zydemo.IPC_AIDL_demo.aidl.IOnNewBookArrivedListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;
    private IBookManager mRemoteBookManager;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.d(TAG, "received new Book");
                    break;
                default:
                    super.handleMessage(msg);

            }
        }
    };

    private IOnNewBookArrivedListener mNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book book) throws RemoteException {
            //在客户端的Binder线程池中执行
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED).sendToTarget();
        }
    };

    private ServiceConnection mConntection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IBookManager bookManager = IBookManager.Stub.asInterface(service);

            /**
             * 在UI线程调用服务端的方法，客户端线程会被挂起
             * 若服务端方法太耗时，会出现ANR
             * 另 ：onServiceConnected 与 onServiceDisconnected执行在UI线程
             *  所以，不可以在这里调用服务端的耗时方法
             *
             */
            try {
                mRemoteBookManager = bookManager;
                List<Book> list = bookManager.getBookList();
                Log.d(TAG, "query book list: "+list.toString());

                Book newBook = new Book(3,"Android开发艺术探索");
                bookManager.addBook(newBook);

                List<Book> newList = bookManager.getBookList();
                Log.d(TAG, "newList = "+newList.toString());

                bookManager.registerListener(mNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteBookManager = null;
            Log.d(TAG, "binder died");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this,BookManagerService.class);
        bindService(intent,mConntection,BIND_AUTO_CREATE);


    }

    @Override
    protected void onDestroy() {

        //解注册listener
        if (mRemoteBookManager != null && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                mRemoteBookManager.unregisterListener(mNewBookArrivedListener);
                Log.d(TAG, "unRegistered...");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConntection);
        super.onDestroy();
    }

    /**
     * 示例调用服务端耗时方法的正确姿势
     */
    private void example(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Book> list = mRemoteBookManager.getBookList();
                    Log.d(TAG, "run: "+list.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
