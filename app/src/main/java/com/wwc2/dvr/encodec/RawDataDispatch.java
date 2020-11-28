package com.wwc2.dvr.encodec;

import android.hardware.Camera;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;


import com.wwc2.dvr.IRawDataCallback;
import android.hardware.Camera;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


public class RawDataDispatch extends BaseDispatch implements Camera.PreviewCallback{
    private String TAG = "RawDataDispatch";

    private int  memorysize = 2*1024*1024;
    private Camera mCamera;

    public RawDataDispatch(IRawDataCallback mRawDataCallback,Camera mCamera) {
        super(mRawDataCallback);
        this.mCamera = mCamera;
    }

    @Override
   public void dispatchData(byte[] data ){
        int length = data.length;
        ParcelFileDescriptor pd = null;
        MemoryFile mMemoryFile= null;
        try {
            mMemoryFile= new MemoryFile("PreviewMeminfo", memorysize);
        }catch (IOException e){
            e.printStackTrace();
        }
      //  Log.d(TAG,"onPreviewFrame  dvr  data.length = " + length);
        try {
            pd = copyAndPost(mMemoryFile,data,length);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            getRawDataCallback().onDataFrame(pd,length);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            pd.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        mMemoryFile.close();
    }

    @Override
    public void start() {
        Log.d(TAG,"start...");
        mCamera.setPreviewCallback(this);
    }

    @Override
    public void destroy() {
        Log.d(TAG,"Destroy...");
        mCamera.setPreviewCallback(null);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        dispatchData(data);
    }
}
