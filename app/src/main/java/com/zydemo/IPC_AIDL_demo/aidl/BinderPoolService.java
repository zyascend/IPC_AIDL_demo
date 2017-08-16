package com.zydemo.IPC_AIDL_demo.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 *
 * 远程服务端
 * Created by Administrator on 2016/12/15.
 */

public class BinderPoolService extends Service {


    private Binder mBinderPool = new BinderPool.BinderPoolImpl();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinderPool;
    }

}
