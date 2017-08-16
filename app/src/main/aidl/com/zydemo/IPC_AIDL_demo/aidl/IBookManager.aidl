// IBookManager.aidl
package com.zydemo.IPC_AIDL_demo.aidl;

//自定义的Parcelable对象必须显示导入，并且与当前aidl文件处于同一个包
import com.zydemo.IPC_AIDL_demo.aidl.Book;
import com.zydemo.IPC_AIDL_demo.aidl.IOnNewBookArrivedListener;
// Declare any non-default types here with import statements

interface IBookManager {
    /**
     *
     *
     */
    List<Book> getBookList();
    void addBook(in Book book);
    void registerListener(IOnNewBookArrivedListener listener);
    void unregisterListener(IOnNewBookArrivedListener listener);

}
