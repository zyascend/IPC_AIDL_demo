// ISecurityCenter.aidl
package com.zydemo.IPC_AIDL_demo.aidl;

// Declare any non-default types here with import statements

interface ISecurityCenter {
    String encrypt(String content);
    String decrypt(String password);
}
