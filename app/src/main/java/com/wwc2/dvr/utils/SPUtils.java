package com.wwc2.dvr.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.net.Uri;

import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.DvrApplication;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.fourCamera.FourCameraProxy;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Created by admin on 2016/5/24.
 */
public class SPUtils {
    private static SharedPreferences mPreferences;

    public static void putBoolean(Context context, String key, boolean value) {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(context,
                    ConstantsData.SP_NAME);
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, String key) {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(context,
                    ConstantsData.SP_NAME);
        }
        return mPreferences.getBoolean(key, false);
    }

    public static boolean getBoolean(Context context, String key, boolean def) {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(context,
                    ConstantsData.SP_NAME);
        }
        return mPreferences.getBoolean(key, def);
    }

    public static void putInt(Context context, String key, int value) {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(context,
                    ConstantsData.SP_NAME);
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String key) {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(context,
                    ConstantsData.SP_NAME);
        }
        return mPreferences.getInt(key, 0);
    }

    public static int getInt(Context context, String key, int def) {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(context,
                    ConstantsData.SP_NAME);
        }
        return mPreferences.getInt(key, def);
    }

    public static void putString(Context context, String key, String value) {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(context,
                    ConstantsData.SP_NAME);
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(context,
                    ConstantsData.SP_NAME);
        }
        return mPreferences.getString(key, null);
    }

    public static String getString(Context context, String key, String def) {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(context,
                    ConstantsData.SP_NAME);
        }
        return mPreferences.getString(key, def);
    }

    public static boolean getLockStatus(Context context) {
        return getBoolean(context, ConstantsData.KEY_LOCK_STATUS, false);
    }

    public static void setLockStatus(Context context, boolean lock) {
        putBoolean(context, ConstantsData.KEY_LOCK_STATUS, lock);
    }

    public static int getAutoSaveTime(Context context) {
        return getInt(context, ConstantsData.KEY_AUTO_SAVE_TIME,ConstantsData.DEF_RECORD_TIME);
    }

    public static void setAutoSaveTime(Context context, int time) {
        putInt(context, ConstantsData.KEY_AUTO_SAVE_TIME, time);
    }

    public static boolean getAutoRecord(Context context) {
        return getBoolean(context, ConstantsData.KEY_AUTO_RECORD,ConstantsData.DEF_AUTO_RECORD);
    }

    public static void setAutoRecord(Context context, boolean isAutoRecord) {
        putBoolean(context, ConstantsData.KEY_AUTO_RECORD, isAutoRecord);
    }
    public static boolean getMuteRecord(Context context) {
        return getBoolean(context, ConstantsData.KEY_MUTE_RECORD, true);//默认静音录制
    }

    public static void setMuteRecord(Context context,boolean isMuteRecord) {
        putBoolean(context, ConstantsData.KEY_MUTE_RECORD, isMuteRecord);

    }

    public static void setChannelWaterMark(Context context, boolean b) {
        putBoolean(context,ConstantsData.KEY_CHANNEL_WATER_MARK,b);
    }

    public static boolean getChannelWaterMark(Context context) {
        return getBoolean(context, ConstantsData.KEY_CHANNEL_WATER_MARK ,ConstantsData.DEF_WATER_MARK);
    }

    public static void setTimeWaterMark(Context context, boolean b) {
        putBoolean(context,ConstantsData.KEY_TIME_WATER_MARK,b);
    }

    public static boolean getTimeWaterMark(Context context) {
        return getBoolean(context, ConstantsData.KEY_TIME_WATER_MARK ,ConstantsData.DEF_WATER_MARK);
    }

    public static void setGPSWaterMark(Context context, boolean b) {
        putBoolean(context,ConstantsData.KEY_GPS_WATER_MARK,b);
    }

    public static boolean getGPSWaterMark(Context context) {
        return getBoolean(context, ConstantsData.KEY_GPS_WATER_MARK , false);
    }

    public static void setCarWaterMark(Context context, boolean b) {
        putBoolean(context,ConstantsData.KEY_CAR_WATER_MARK,b);
    }

    public static boolean getCarWaterMark(Context context) {
        return getBoolean(context, ConstantsData.KEY_CAR_WATER_MARK ,false);
    }


    public static void setH264FrameRate(Context context, int fbs) {
        putInt(context,ConstantsData.KEY_H264_BIT_RATE,fbs);
    }

    public static int getH264FrameRate(Context context) {
        return getInt(context, ConstantsData.KEY_H264_FRAME_RATE ,ConstantsData.DEF_H264_FRAME_RATE);
    }


    public static void setH264BitRate(Context context, int fbs) {
        putInt(context,ConstantsData.KEY_H264_BIT_RATE,fbs);
    }

    public static int getH264BitRate(Context context) {
        return getInt(context, ConstantsData.KEY_H264_BIT_RATE ,ConstantsData.DEF_H264_BIT_RATE);
    }


    public static int getLocation(Context context) {
        return getInt(context,ConstantsData.KEY_LOCATION, StorageDevice.USB);//MEDIA_CARD);
    }

    public static void setLocation(Context context, int location) {
        putInt(context,ConstantsData.KEY_LOCATION,location);
        FourCameraProxy.setDvrLocationToSystem(context, location);
    }

    public static int getVideoSettings(Context context) {
        return getInt(context,ConstantsData.KEY_VIDEOSETTINGS, StorageDevice.NAND_FLASH);
    }

    /**
     *
     * @param context
     * @param type 0:单路1 1:单录2 2:双录
     */
    public static void setVideoSettings(Context context, int type) {
        putInt(context,ConstantsData.KEY_VIDEOSETTINGS,type);
    }

    public static void setM_Mirror(Context context, int value) {
        if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
            setReverseImage(context, value == 1);
        } else {
            putInt(context, ConstantsData.KEY_MAIN_MIRROR, value);
        }
    }

    public static int getM_Mirror(Context context) {
        if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
            return (getReverseImage(context) ? 1 : 0);
        } else {
            return getInt(context, ConstantsData.KEY_MAIN_MIRROR, ConstantsData.DEF_MIRROR);
        }
    }

    public static void setS_Mirror(Context context, int value) {
        putInt(context,ConstantsData.KEY_SUB_MIRROR, value);
    }

    public static int getS_Mirror(Context context) {
        return getInt(context, ConstantsData.KEY_SUB_MIRROR ,ConstantsData.DEF_MIRROR);
    }

    public static void setLeft_Mirror(Context context, int value) {
        putInt(context, ConstantsData.KEY_LEFT_MIRROR, value);
    }

    public static int getLeft_Mirror(Context context) {
        return getInt(context, ConstantsData.KEY_LEFT_MIRROR ,ConstantsData.DEF_MIRROR);
    }

    public static void setRight_Mirror(Context context, int value) {
        putInt(context, ConstantsData.KEY_RIGHT_MIRROR, value);
    }

    public static int getRight_Mirror(Context context) {
        return getInt(context, ConstantsData.KEY_RIGHT_MIRROR ,ConstantsData.DEF_MIRROR);
    }

    public static int getSplitScreen(Context context) {
        int isSplit = SPUtils.getInt(context, Config.IS_SPLIT, 1);
        if (isSplit == 1){ //全屏
        return  Config.FULL_SCREEN;
        }else if (isSplit == 2){ //分屏（左右）
            return  Config.SPLIT_SCREEN;
        }else  if (isSplit ==3){//分屏（上下)
            return  Config.SPLIT_SCREEN_UP_DOWN;
        }
        return  1;
    }

    public static int getSensor(Context context) {
        return getInt(context,ConstantsData.KEY_SENSOR, Config.SENSOR_CLOSE);
    }

    public static void setSensor(Context context, int Sensor) {
        putInt(context,ConstantsData.KEY_SENSOR, Sensor);
    }


    public static String getCarNumber(Context context) {
        return getString(context,ConstantsData.KEY_CAR_NUMBER, "");
    }

    public static void setCarNumber(Context context,String vaule) {
         putString(context,ConstantsData.KEY_CAR_NUMBER, vaule);
    }

    public static int getRecordQuality(Context context) {
        int def = Config.RECORD_BPS_2;
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_QUART_STREAM.equals(curRecordType) ||
                Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            def = Config.RECORD_BPS_5;
        }
        return getInt(context, Config.KEY_RECORD_QUALITY, def);
    }

    public static String getRecordType(Context context) {
        String curType = getString(context, Config.KEY_RECORD_TYPE, "undefine");
        if (curType == null || curType.equals("undefine")) {//配置文件中未保存
            curType = FourCameraProxy.getCameraPlatformType();//读取系统属性
            if (curType == null || curType.equals("undefine")) {
                if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
                    curType = Config.TYPE_TWO_STREAM;
                } else {
                    curType = Config.TYPE_QUART_STREAM;

                    int location = RecordData.getInstance().mutableLocation;
                    if (location == StorageDevice.MEDIA_CARD || location == StorageDevice.NAND_FLASH) {
                        curType = Config.TYPE_FOUR_STREAM;
                    }
                }
            }
        }
        LogUtils.d("getRecordType-----curType=" + curType);
        return curType;
    }

    public static String getCameraType(Context context) {
        String cameraType = getString(context, Config.KEY_CAMERA_TYPE, Config.CAMTYPE_4HD);
        if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
            cameraType = getCameraTypeFromMian(context);
        }
        String tempType = cameraType;
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_TWO_STREAM.equals(curRecordType) ||
                Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            if (cameraType.equals(Config.CAMTYPE_4HD)) {
                tempType = Config.CAMTYPE_2HD;
            }
        } else {
            if (!cameraType.equals(Config.CAMTYPE_4HD)) {
                tempType = Config.CAMTYPE_4HD;
            }
        }

        if (!tempType.equals(cameraType)) {
//            RecordData.getInstance().cameraType.setValue(tempType);
            setCameraType(context, tempType);
        }

        LogUtils.d("getCameraType-----cameraType=" + cameraType + ", tempType=" + tempType);
        return tempType;
    }

    //同步设置和获取Main中的制式
    public static String getCameraTypeFromMian(Context context) {
        String cameraType = Config.CAMTYPE_2HD;
        Uri uri = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.WY_CAMERA_TYPE);
        try {
            int type = Integer.parseInt(context.getContentResolver().getType(uri));
            if (type == 8) {
                cameraType = Config.CAMTYPE_2FHD;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return cameraType;
    }
    public static void setCameraType(Context context, String cameraType) {
        putString(context, Config.KEY_CAMERA_TYPE, cameraType);

        if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
            int tmpCameraType = 2;
            if (Config.CAMTYPE_2FHD.equals(cameraType)) {
                tmpCameraType = 8;
            }
            Uri uri = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.WY_CAMERA_TYPE);
            ContentValues updateValues = new ContentValues();
            updateValues.put(Config.WY_CAMERA_TYPE, tmpCameraType);
            context.getContentResolver().update(uri, updateValues, null, null);
        }
    }
    //同步设置和获取Main中的倒车镜像开关
    public static boolean getReverseImage(Context context) {
        boolean ret = false;
        Uri uri = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.REVERSE_MIRROR);
        try {
            ret = Boolean.parseBoolean(context.getContentResolver().getType(uri));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return ret;
    }
    public static void setReverseImage(Context context, boolean mirror) {
        Uri uri = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.REVERSE_MIRROR);
        ContentValues updateValues = new ContentValues();
        updateValues.put(Config.REVERSE_MIRROR, mirror);
        context.getContentResolver().update(uri, updateValues, null, null);
    }

    /**
     * @param context
     * @param fileName
     * @return 返回修改路径(路径不存在会自动创建)以后的 SharedPreferences :%FILE_PATH%/%fileName%.xml<br/>
     */
    private static SharedPreferences getSharedPreferences(Context context, String fileName) {
        try {
            // 获取ContextWrapper对象中的mBase变量。该变量保存了ContextImpl对象
            Field field = ContextWrapper.class.getDeclaredField("mBase");
            field.setAccessible(true);
            // 获取mBase变量
            Object obj = field.get(context);
            // 获取ContextImpl。mPreferencesDir变量，该变量保存了数据文件的保存路径
            field = obj.getClass().getDeclaredField("mPreferencesDir");
            field.setAccessible(true);
            // 创建自定义路径
            File file = new File("/custom/");
            // 修改mPreferencesDir变量的值
            field.set(obj, file);
            // 返回修改路径以后的 SharedPreferences :%FILE_PATH%/%fileName%.xml
            return context.getSharedPreferences(fileName, Activity.MODE_PRIVATE);
        } catch (NoSuchFieldException e) {
            LogUtils.d("getSharedPreferences E1");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            LogUtils.d("getSharedPreferences E2");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            LogUtils.d("getSharedPreferences E3");
            e.printStackTrace();
        }

        // 返回默认路径下的 SharedPreferences : /data/data/%package_name%/shared_prefs/%fileName%.xml
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }
}
