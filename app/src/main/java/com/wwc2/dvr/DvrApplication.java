package com.wwc2.dvr;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.SystemProperties;
import android.view.View;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.DaoMaster;
import com.wwc2.dvr.data.DaoSession;
import com.wwc2.dvr.utils.Crash;
import com.wwc2.dvr.utils.FileUtils;
import com.wwc2.dvr.utils.ThreadpoolUtil;
import com.wwc2.dvr.utils.Utils;

import java.io.IOException;

public class DvrApplication extends Application {
    private static DaoSession daoSession;
    //0=后视，1=前视
    private static int CHANNELS = 0;
    //平台
    private static String platform;
    //初始化Camera,true表示为非初始化
    private static boolean INIT_CAMERA = false;
    private static Context mContext;

	public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Crash.getInstance().init();
        Utils.init(this);
        ThreadpoolUtil.init();
        platform = SystemProperties.get(Config.SYSTEM_PLATFORM);

        LogUtils.init(getApplicationContext(), true, "DvrSimple", false);

        LogUtils.e("application onCreate");
        if (getDvrEnable()) {
            Intent intent = new Intent(this, RecordService.class);
            startService(intent);
        } else {
            LogUtils.e("application onCreate DvrEnable close!");
        }
    }

    // This snippet hides the system bars.
    public static void hideSystemUI(View view) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        if (null != view) {
            view.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | 0x00002000);
        }
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    public static void showSystemUI(View view) {
        if (null != view) {
            view.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    public static Context getContext() {
        return mContext;
    }

    public static int getCHANNELS() {
        return CHANNELS;
    }

    public static void setCHANNELS(int CHANNELS) {
        DvrApplication.CHANNELS = CHANNELS;
    }

    public static String getPlatform() {
	    return  Config.SYSTEM_PLATFORM_65;
    }

    public static boolean isInitCamera() {
        return INIT_CAMERA;
    }

    public static void setInitCamera(boolean initCamera) {
        LogUtils.d("...setInitCamera...initCamera=" + initCamera);
        INIT_CAMERA = initCamera;
    }

    public static String getClinetId() {
        Uri uri = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.CLIENT_ID);
	    String ret = "";
	    if (mContext != null) {
            ret = mContext.getContentResolver().getType(uri);
        }
	    return ret;
    }

    private boolean getDvrEnable() {
        Uri uri_dvr = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.DVR_ENABLE);
        String dvrEnable = getContentResolver().getType(uri_dvr);
        if ("false".equals(dvrEnable)) {
            return false;
        }
        return true;
    }
}