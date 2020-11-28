package com.autonavi.amapauto.gdarcameraservicedemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Surface;

import com.autonavi.amapauto.gdarcameraservice.IGDCameraService;
import com.autonavi.amapauto.gdarcameraservice.IGDCameraStateCallBack;
import com.autonavi.amapauto.gdarcameraservice.IGDSize;
import com.autonavi.amapauto.gdarcameraservice.model.GDArCameraParam;
import com.wwc2.dvr.data.Stereo;
import com.wwc2.dvr.ui.record.CameraHandlerThread;


/**
 * 摄像头服务实现类
 */
public class GDArCameraService extends Service {

    private String TAG = "GDArCameraService";

    private ArrayMap<String, RawDataDispatch> mRegister = new ArrayMap<>();
    private static CameraHandlerThread mCameraHandlerThread;
    private boolean mHasSetPreviewCallback = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void setCameraHandlerThread(CameraHandlerThread handlerThread){
        mCameraHandlerThread = handlerThread;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"on Bind  ok " );
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"on onUnbind   " );
        while(!mRegister.isEmpty()) {
            mRegister.removeAt(0).destroy();
        }
        return super.onUnbind(intent);
    }

    private final IGDCameraService.Stub binder = new IGDCameraService.Stub() {
        @Override
        public boolean registerCameraStateCallback(String clientId, IGDCameraStateCallBack gdCameraStateCallBack) throws RemoteException {
            Log.d(TAG,"on register CameraStateCallback  clientId =" +clientId);
            if(mRegister.get(clientId) == null) {
                RawDataDispatch rawDataDispatch = new RawDataDispatch(gdCameraStateCallBack, mCameraHandlerThread);
                mRegister.put(clientId, rawDataDispatch);
                mCameraHandlerThread.setRawDataDispatch(rawDataDispatch);
                return true;
            }else{
                Log.d(TAG," CameraStateCallback  has been register... clientId =" + clientId );
                return true;
            }
        }

        @Override
        public boolean unregisterCameraStateCallback(String clientId, IGDCameraStateCallBack gdCameraStateCallBack) throws RemoteException {
            Log.d(TAG,"un register CameraStateCallback  clientId= "+ clientId );
            mRegister.get(clientId).destroy();
            mRegister.remove(clientId);
            return true;
        }

        @Override
        public boolean isSupportArNavi(String clientId) throws RemoteException {
            Log.e(TAG,"isSupportArNavi----clientId=" + clientId);
            return true;
        }

        @Override
        public IGDSize getRecommendSize(String clientId) throws RemoteException {
            return new IGDSizeImp(mCameraHandlerThread.getPreviewHeight(),mCameraHandlerThread.getPreviewWidth()) ;
        }

        @Override
        public boolean isCameraConnected(String clientId) throws RemoteException {
            Log.d(TAG,"isCamera Connected..." );
            return true;
        }

        @Override
        public boolean isCameraOpened(String clientId) throws RemoteException {
            Log.d(TAG,"isCamera Opened..." );
            return mHasSetPreviewCallback;
        }

        @Override
        public boolean openCamera(String clientId) throws RemoteException {
            RawDataDispatch dispatch = mRegister.get(clientId);
            if(dispatch != null) {
                mRegister.get(clientId).start();
                mHasSetPreviewCallback =true;
                Log.d(TAG," mHas SetPreviewCallback  =" +mHasSetPreviewCallback );
            }else{
                Log.d(TAG,"open Camera fail, register first   clientId  " +clientId );
            }
            return true;
        }

        @Override
        public boolean closeCamera(String clientId) throws RemoteException {
            Log.d(TAG," close Camera.....    " );
            RawDataDispatch dispatch = mRegister.get(clientId);
            if(dispatch !=null) {
                mRegister.get(clientId).destroy();
                mHasSetPreviewCallback =false;
            }else{
                Log.d(TAG,"open Camera fail, register first    " );
            }

            return true;
        }



        @Override
        public boolean initCamera(String clientId, GDArCameraParam gdArCameraParam, Surface surface) throws RemoteException {
            Log.d(TAG,"init Camera ..." );
            return false;
        }

        @Override
        public boolean unInitCamera(String clientId) throws RemoteException {
            Log.d(TAG,"unInit Camera ..." );
            return false;
        }
    };
}
