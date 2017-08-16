// IOnNewBookArrivedListener.aidl
package com.zydemo.IPC_AIDL_demo.aidl;

import com.zydemo.IPC_AIDL_demo.aidl.Book;
// Declare any non-default types here with import statements

interface IOnNewBookArrivedListener {
   void onNewBookArrived(in Book book);

}
