package com.wwc2.dvr.cameraevent;

import android.util.Log;

import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.ui.AppBaseUI;

public class CameraCrashHandler extends BaseHandler{
    private String TAG = "CameraCrashHandler";

    public CameraCrashHandler(AppBaseUI mAppBaseUI) {
        super(mAppBaseUI);
    }

    @Override
    public boolean needHandler(String code) {
        return Integer.valueOf(code) == Integer.valueOf(Config.RECORD_STATUS_CRASH);
    }

    @Override
    public void toDoHandler(String code) {
        Log.d(TAG,"do handler code =" +code);
        if(getAppBaseUI().getmRecordSerice().getCameraHandlerThread().isCameraBackOpened()) {
            getAppBaseUI().getmRecordSerice().rebootCamera();
        }else{
            Log.d(TAG,"camera is close, do nothing!");
        }
    }
}
