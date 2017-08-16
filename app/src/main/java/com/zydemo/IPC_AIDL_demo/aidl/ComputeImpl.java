package com.zydemo.IPC_AIDL_demo.aidl;

import android.os.RemoteException;

/**
 * ICompute的实现类
 * Created by Administrator on 2016/12/15.
 */

public class ComputeImpl extends ICompute.Stub {
    @Override
    public int add(int a, int b) throws RemoteException {
        return a+b;
    }
}
