package com.zydemo.IPC_AIDL_demo.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

/**
 * Binder连接池的实现类
 * Created by Administrator on 2016/12/15.
 */

public class BinderPool {
    public static final int BINDER_SECURITY_CENTER = 0;
    public static final int BINDER_COMPUTE = 1;
    private static final String TAG = "BinderPool";
    private static volatile BinderPool sInstance;
    private Context mContext;
    private CountDownLatch mBinderPoolCountDownLatch;

    private BinderPool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    public static BinderPool getInstance(Context context){
        if (sInstance == null){
            synchronized (BinderPool.class){
                if (sInstance == null){
                    sInstance = new BinderPool(context);
                }
            }
        }
        return sInstance;
    }


    private synchronized void connectBinderPoolService() {
        mBinderPoolCountDownLatch = new CountDownLatch(1);
        Intent service = new Intent(mContext,BinderPoolService.class);
        mContext.bindService(service,mBinderPoolConnection,Context.BIND_AUTO_CREATE);
        try {
            mBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private IBinderPool mBinderPool;
    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(mDeathRecipient,0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // TODO: 2016/12/15 作用？？？
            mBinderPoolCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {            //do nothing
            //do nothing

        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            //意外停止后重新连接服务
            Log.d(TAG, "binderDied: ");
            mBinderPool.asBinder().unlinkToDeath(mDeathRecipient,0);
            mBinderPool = null;
            connectBinderPoolService();
        }
    };


    public IBinder queryBinder(int binderCode){
        IBinder binder = null;
        try {
            if (mBinderPool != null){
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return binder;
    }

    public static class BinderPoolImpl extends IBinderPool.Stub{



        public BinderPoolImpl(){
            super();
        }

        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {

            IBinder iBinder = null;
            switch (binderCode){
                case BINDER_SECURITY_CENTER:
                    iBinder = new SecurityCenterImpl();
                    break;
                case BINDER_COMPUTE:
                    iBinder = new ComputeImpl();
                    break;
                default:
                    break;
            }
            return iBinder;
        }
    }


}
