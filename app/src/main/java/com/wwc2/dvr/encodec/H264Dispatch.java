package com.wwc2.dvr.encodec;

import android.hardware.Camera;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.wwc2.dvr.IRawDataCallback;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.RecordData;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


public class H264Dispatch extends BaseDispatch implements Camera.PreviewCallback {

    private String TAG = "H264Dispatch";

    private IRawDataCallback mIRawDataCallback;
    private Camera mCamera;
    private int framerate = 24;
    private  int biterate = 3500*1000;

    private H264Encoder avcCodec;

    public H264Dispatch(IRawDataCallback mRawDataCallback,Camera mCamera) {
        super(mRawDataCallback);
        this.mCamera = mCamera;
    }

    @Override
    public void dispatchData(byte[] data) {
        Log.d(TAG,"putYUV data[] " +data[0]);
        avcCodec.putYUVData(data,data.length);
    }


    @Override
    public void start() {
        Log.d(TAG,"start...");
        String curCameraType = RecordData.getInstance().cameraType.getValue();
        if (Config.CAMTYPE_2FHD.equals(curCameraType)) {
            avcCodec = new H264Encoder(Config.PREVIEW_FHD[0], Config.PREVIEW_FHD[1], framerate, biterate, this);
        } else {
            avcCodec = new H264Encoder(Config.PREVIEW_HD[0], Config.PREVIEW_HD[1], framerate, biterate, this);
        }
        avcCodec.StartEncoderThread();
        mCamera.setPreviewCallback(this);
    }

    @Override
    public void destroy() {
        avcCodec.StopThread();
        mCamera.setPreviewCallback(null);
        Log.d(TAG,"destroy...");
    }


    public void outputCodecWrite(byte[] data){
        int length = data.length;
        ParcelFileDescriptor pd = null;
        MemoryFile mMemoryFile= null;
        try {
            mMemoryFile= new MemoryFile("PreviewMeminfo", data.length+8);
        }catch (IOException e){
            e.printStackTrace();
        }
       // Log.d(TAG,"onPreviewFrame  dvr  data.length = " + length);
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
    public void onPreviewFrame(byte[] data, Camera camera) {
        dispatchData(data);
    }
}
