package com.autonavi.amapauto.gdarcameraservicedemo;

import android.os.RemoteException;

import com.autonavi.amapauto.gdarcameraservice.IGDSize;

public class IGDSizeImp extends IGDSize.Stub {
    int mHeight ;
    int mWidth;


    public IGDSizeImp(int mHeight, int mWidth) {
        this.mHeight = mHeight;
        this.mWidth = mWidth;
    }

    @Override
    public int getWidth() throws RemoteException {
        return mWidth;
    }

    @Override
    public int getHeight() throws RemoteException {
        return mHeight;
    }


}
