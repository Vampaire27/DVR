package com.autonavi.amapauto.gdarcameraservicedemo;


import android.hardware.Camera;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.autonavi.amapauto.gdarcameraservice.IGDCameraStateCallBack;
import com.autonavi.amapauto.gdarcameraservice.ImageFormat;
import com.autonavi.amapauto.gdarcameraservice.model.ArCameraOpenResultParam;
import com.autonavi.amapauto.gdarcameraservice.utils.SharedMemUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.ui.record.CameraHandlerThread;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class RawDataDispatch implements Camera.PreviewCallback{
    private String TAG = "RawDataDispatch";

    private MemoryFile mMemoryFile= null;
    private FileDescriptor mShareFd = null;
    private ParcelFileDescriptor mSharePfd = null;

    private int imageHeight = 1280;
    private int imageWidth = 720;
    private String ShareFileName = "PreviewMeminfo";


    /**
     * 申请内存大小, 等于摄像头数据+标记位
     */
    private int memorySize ;

    /**
     * 共享内存的HEADER
     */
    private byte[] header = new byte[SharedMemUtils.HEADER_SIZE];

    private CameraHandlerThread mCameraThread;
    IGDCameraStateCallBack mRawDataCallback;


    public RawDataDispatch(IGDCameraStateCallBack mRawDataCallback, CameraHandlerThread mCameraThread) {
        this.mRawDataCallback = mRawDataCallback;
        this.mCameraThread = mCameraThread;
    }



   public void dispatchData(byte[] data ){
       int length = data.length;
        if(mMemoryFile == null){
             mMemoryFile= null;
            try {
                Log.d(TAG, " yv12 canWrite = {mMemoryFile =" + mMemoryFile);
                memorySize = mCameraThread.getPreviewHeight()*mCameraThread.getPreviewWidth()*3/2  + SharedMemUtils.HEADER_SIZE;
                mMemoryFile= new MemoryFile("PreviewMeminfo", memorySize);
                mMemoryFile.allowPurging(false);
                Method method = MemoryFile.class.getDeclaredMethod("getFileDescriptor");
                mShareFd = (FileDescriptor) method.invoke(mMemoryFile);
                mSharePfd = ParcelFileDescriptor.dup(mShareFd);

                SharedMemUtils.setWirtable(header);
                mMemoryFile.writeBytes(header, 0, 0, SharedMemUtils.HEADER_SIZE);
                ArCameraOpenResultParam arCameraOpenResultParam = new ArCameraOpenResultParam(ImageFormat.I420_822.getFormat(),mCameraThread.getPreviewWidth(),
                        mCameraThread.getPreviewHeight(),length);

                mRawDataCallback.onOpened(mSharePfd,arCameraOpenResultParam,ShareFileName);

            }catch (IOException e){
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            try {
                mMemoryFile.readBytes(header, 0, 0, SharedMemUtils.HEADER_SIZE);

                boolean canWrite = SharedMemUtils.canWrite(header);
                Log.d(TAG, " yv12 canWrite = {?}" + canWrite);
                if (canWrite) {
                    mMemoryFile.writeBytes(data,0,SharedMemUtils.HEADER_SIZE,length);
                    SharedMemUtils.setCanRead(header);
                    mMemoryFile.writeBytes(header, 0, 0, SharedMemUtils.HEADER_SIZE);
                }else{
                    Log.d(TAG, "cannot write data. drop data!");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    public void start() {
        Log.d(TAG,"start...");
        mCameraThread.mCameraBack.setPreviewCallback(this);
        mCameraThread.getAppBaseUI().getFourCameraManager().setAVOpen(true);

    }


    public void destroy() {
        Log.d(TAG,"Destroy...");


        if(mCameraThread != null && mCameraThread.mCameraBack != null ) {
            mCameraThread.mCameraBack.setPreviewCallback(null);
        }

        try {  //add for may PreviewCallback is exe ing....use locked maybe performance problem .
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(mShareFd != null){
            try {
                mSharePfd.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            mShareFd = null;
        }

        if(mMemoryFile != null){
            mMemoryFile.close();
            mMemoryFile = null;
        }

        mSharePfd =null;
        mCameraThread.getAppBaseUI().getFourCameraManager().setAVOpen(false);

    }

    public void openCamera(boolean open) {
        LogUtils.e("openCamera----open=" + open);
        if (open) {
            start();
        } else {
            try {
                mRawDataCallback.onClosed(1, "");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            destroy();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        dispatchData(data);
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }
}

