package com.zydemo.IPC_AIDL_demo;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.zydemo.IPC_AIDL_demo.aidl.BinderPool;
import com.zydemo.IPC_AIDL_demo.aidl.ComputeImpl;
import com.zydemo.IPC_AIDL_demo.aidl.ICompute;
import com.zydemo.IPC_AIDL_demo.aidl.ISecurityCenter;
import com.zydemo.IPC_AIDL_demo.aidl.SecurityCenterImpl;

/**
 * Created by Administrator on 2016/12/15.
 */

public class BinderPoolActivity extends AppCompatActivity {

    private ISecurityCenter mSecurityCenter;
    private static final String TAG = "BinderPoolActivity";
    private ICompute mCompute;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                doWork();
            }
        }).start();
    }

    /**
     * Binder连接池测试方法
     * 在子线程中执行
     */
    private void doWork() {
        BinderPool binderPool = BinderPool.getInstance(BinderPoolActivity.this);

        IBinder securityBinder = binderPool.queryBinder(BinderPool.BINDER_SECURITY_CENTER);
        mSecurityCenter = SecurityCenterImpl.asInterface(securityBinder);
        Log.d(TAG, "visit SecurityCenter");

        String msg = "test msg";
        try {
            String password = mSecurityCenter.encrypt(msg);
            Log.d(TAG, "encrypt: "+password);
            Log.d(TAG, "decrypt: "+mSecurityCenter.decrypt(password));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        IBinder computeBinder = binderPool.queryBinder(BinderPool.BINDER_COMPUTE);
        mCompute = ComputeImpl.asInterface(computeBinder);
        Log.d(TAG, "visit Icompute");
        try {
            Log.d(TAG, "9999+666 = "+mCompute.add(9999,666));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
