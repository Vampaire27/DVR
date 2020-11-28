package com.wwc2.dvr.ui;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.R;
import com.wwc2.dvr.RecordService;
import com.wwc2.dvr.cameraevent.CameraCrashHandler;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.databinding.ActivityMainBinding;
import com.wwc2.dvr.fourCamera.FourCameraProxy;
import com.wwc2.dvr.cameraevent.CameraRecordStateHandler;

public class AppBaseUI implements GestureSurfaceView.GestureListener {
    public  final static String TAG= "AppBaseUI";

    private ActivityMainBinding binding;
    private RecordService mRecordSerice;
    private GestureSurfaceView mPreviewSurface;
    private FourCameraProxy mFourCameraProxy;
    private RecordBroadCast mRecordBroadCast;

    private RecordProgressBarWindow mRecordProgressBarWindow;
    public static final String SYSTEM_KEY = "com.wwc2.otherKeyCode";

    private CameraRecordStateHandler mCameraRecordStateHandler;
    private CameraCrashHandler mCameraCrashHandler;

    public AppBaseUI(RecordService service, ActivityMainBinding binding) {
        this.binding = binding;
        mRecordSerice = service;
        mPreviewSurface =binding.getRoot().findViewById(R.id.camera_view_back);
        mPreviewSurface.setGestureListener(this);
        mFourCameraProxy =  new FourCameraProxy(service);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SYSTEM_KEY);
        mRecordBroadCast = new RecordBroadCast();
        mRecordSerice.registerReceiver(mRecordBroadCast, intentFilter);

        mCameraRecordStateHandler = new CameraRecordStateHandler(this);
        mCameraCrashHandler = new CameraCrashHandler(this);
        mCameraRecordStateHandler.setNextHandler(mCameraCrashHandler);

        mCameraCrashHandler.setNextHandler(null);

    }

    @Override
    public void onClickFront() {
        if(mRecordSerice.getCameraStatus()){
            return;
        }

        if (!mFourCameraProxy.isFourDisplay() && !mFourCameraProxy.isTwoDisplay()) {
            mFourCameraProxy.SettingAllCameraView();
        } else {
            mFourCameraProxy.SettingFrontCameraView();
        }
    }

    @Override
    public void onClickBack() {
        if(mRecordSerice.getCameraStatus()){
            return;
        }

        if (!mFourCameraProxy.isFourDisplay() && !mFourCameraProxy.isTwoDisplay()) {
            mFourCameraProxy.SettingAllCameraView();
        } else {
            mFourCameraProxy.SettingBackCameraView();
        }
    }

    @Override
    public void onClickLeft() {
        if(mRecordSerice.getCameraStatus()){
            return;
        }

        if (!mFourCameraProxy.isFourDisplay()) {
            mFourCameraProxy.SettingQuartCameraView();
        } else {
            mFourCameraProxy.SettingLeftCameraView();
        }
    }

    @Override
    public void onClickRight() {
        if(mRecordSerice.getCameraStatus()){
            return;
        }

        if (!mFourCameraProxy.isFourDisplay()) {
            mFourCameraProxy.SettingQuartCameraView();
        } else {
            mFourCameraProxy.SettingRightCameraView();
        }
    }

    @Override
    public void onSwipeUp() {
        if(mRecordSerice.getCameraStatus()){
            return;
        }
        mRecordSerice.showBottom(true);
    }

    @Override
    public void onSwipeDown() {
        mRecordSerice.showBottom(false);
    }

    public FourCameraProxy getFourCameraManager(){
        return mFourCameraProxy;
    }
    private class RecordBroadCast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyValue", 0);
            LogUtils.d("---keyCode=" + keyCode);
            if(keyCode == KeyEvent.KEYCODE_MENU){
                mCameraRecordStateHandler.handlerRequest(mFourCameraProxy.getCode());
            }
        }
    }

    public void updateRecordStatus(){
        mRecordSerice.updateRecordStatefromDrive(mFourCameraProxy.isRecorded());
        mRecordSerice.disRecordPopWindow();
    }

    public void showRecordPopWindow(View v) {
        if(mRecordProgressBarWindow == null) {
            mRecordProgressBarWindow = new RecordProgressBarWindow(mRecordSerice.getContext());
        }
        mRecordProgressBarWindow.show(v);
    }

    public void disMissRecordPopWindow(){
        if(mRecordProgressBarWindow != null) {
            mRecordProgressBarWindow.dismiss();
        }
    }

    public RecordService getmRecordSerice() {
        return mRecordSerice;
    }

    public void sendBack(Context context) {
        Uri uri_key = Uri.parse("content://" + Config.AUTHORITY +"/" + Config.USER_KEY);
        ContentValues values = new ContentValues();
        values.put(Config.USER_KEY, Config.KEY_BACK);
        context.getContentResolver().update(uri_key, values, null, null);
    }
}
