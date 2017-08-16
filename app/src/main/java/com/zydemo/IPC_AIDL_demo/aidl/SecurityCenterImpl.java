package com.zydemo.IPC_AIDL_demo.aidl;

import android.os.RemoteException;

/**
 * ISecurityCenter的实现类
 * Created by Administrator on 2016/12/15.
 */

public class SecurityCenterImpl extends ISecurityCenter.Stub {
    private static final char SECRET_CODE = '^';

    @Override
    public String encrypt(String content) throws RemoteException {
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= SECRET_CODE;
        }
        return new String(chars);
    }

    @Override
    public String decrypt(String password) throws RemoteException {
        return encrypt(password);
    }
}
