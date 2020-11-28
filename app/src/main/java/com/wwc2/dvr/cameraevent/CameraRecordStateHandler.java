package com.wwc2.dvr.cameraevent;

import android.util.Log;

import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.ui.AppBaseUI;

public class CameraRecordStateHandler extends BaseHandler{
    private String TAG = "CameraRecordStateHandler";

    public CameraRecordStateHandler(AppBaseUI mAppBaseUI) {
        super(mAppBaseUI);
    }

    @Override
    public boolean needHandler(String code) {
        boolean ret = false;
        if(Integer.valueOf(code) <= Integer.valueOf(Config.RECORD_STATUS_FAIL)){
            ret = true;
        }
        return ret;
    }

    @Override
    public void toDoHandler(String code) {
        Log.d(TAG,"do handler code =" +code);
        getAppBaseUI().updateRecordStatus();
    }

}
