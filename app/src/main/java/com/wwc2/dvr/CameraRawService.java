package com.wwc2.dvr;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.Log;
import java.util.HashMap;

import com.wwc2.dvr.encodec.BaseDispatch;
import com.wwc2.dvr.encodec.H264Dispatch;
import com.wwc2.dvr.encodec.LocalH264BackDispatch;
import com.wwc2.dvr.encodec.LocalH264FrontDispatch;
import com.wwc2.dvr.encodec.LocalH264LeftDispatch;
import com.wwc2.dvr.encodec.LocalH264RightDispatch;
import com.wwc2.dvr.encodec.RawDataDispatch;
import com.wwc2.dvr.ui.record.CameraHandlerThread;


public class CameraRawService extends Service{
    private String TAG = "CameraRawService";

    private static int expectedBytes = 100*1024;
    private static CameraHandlerThread mCameraHandlerThread;

    private ArrayMap<String, BaseDispatch> mRegister = new ArrayMap<>();

    private static final String RAW_TYPE ="raw";
    private static final String H264_TYPE ="h264";

    private static final String LOCAL_H264_FONT_TYPE = "front";
    private static final String LOCAL_H264_BACK_TYPE = "back";
    private static final String LOCAL_H264_LEFT_TYPE = "left";
    private static final String LOCAL_H264_RIGHT_TYPE = "right";

    private int codeType =3;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void setCameraHandlerThread(CameraHandlerThread handlerThread){
        mCameraHandlerThread = handlerThread;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"on Bind  ok " );
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"onUnbind..... " );
        clearRegisterList();
        return super.onUnbind(intent);
    }


    void clearRegisterList(){
      for(String key :mRegister.keySet()){
          Log.d(TAG,"key is == " + key);
           mRegister.get(key).destroy();
      }
      mRegister.clear();
    }

    private  final IRawDataManager.Stub binder = new IRawDataManager.Stub() {

        @Override
        public boolean register(IRawDataCallback cb,String codeType) throws RemoteException {
            boolean ret = false;
            Log.d(TAG, "register !!");
            BaseDispatch mBaseDispatch;
            if(mCameraHandlerThread != null &&
                    mCameraHandlerThread.getCamera() != null) {
                switch (codeType){
                    case H264_TYPE:
                        mBaseDispatch = new H264Dispatch(cb,mCameraHandlerThread.getCamera());
                        mBaseDispatch.start();
                        break;
                    case  RAW_TYPE:
                        mBaseDispatch = new RawDataDispatch(cb,mCameraHandlerThread.getCamera());
                        mBaseDispatch.start();
                        break;
                    case  LOCAL_H264_FONT_TYPE:
                        mBaseDispatch = new LocalH264FrontDispatch(cb,CameraRawService.this);
                        mBaseDispatch.start();  //data not come form PreViewCallback
                        break;
                    case  LOCAL_H264_BACK_TYPE:
                        mBaseDispatch = new LocalH264BackDispatch(cb,CameraRawService.this);
                        mBaseDispatch.start();  //data not come form PreViewCallback
                        break;
                    case  LOCAL_H264_LEFT_TYPE:
                        mBaseDispatch = new LocalH264LeftDispatch(cb,CameraRawService.this);
                        mBaseDispatch.start();  //data not come form PreViewCallback
                        break;
                    default:
                        mBaseDispatch = new LocalH264RightDispatch(cb,CameraRawService.this);
                        mBaseDispatch.start();  //data not come form PreViewCallback
                        break;
                }
               mRegister.put(codeType,mBaseDispatch);
                ret = true;
            }else{
                Log.d(TAG, "register fail ! camera not init! ");
            }
            return ret;
        }

        @Override
        public void unregister(IRawDataCallback cb,String type) throws RemoteException {
            Log.d(TAG, "un register !!");
            mRegister.get(type).destroy();
            mRegister.remove(type);

        }

    };

}
