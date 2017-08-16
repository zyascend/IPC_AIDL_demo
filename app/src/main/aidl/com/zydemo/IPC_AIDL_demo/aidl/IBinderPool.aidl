// IBinderPool.aidl
package com.zydemo.IPC_AIDL_demo.aidl;

// Declare any non-default types here with import statements

interface IBinderPool {
    IBinder queryBinder(int binderCode);
}
