package com.wwc2.dvr.data;

import android.hardware.Camera;
import android.util.Log;

import com.wwc2.corelib.utils.log.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Stereo {
    private final static String TAG = "Stereo";

    public static final String KEY_VSDOF_MODE_SUPPORTED = "stereo-vsdof-mode-values";
    public static final String KEY_STEREO_CAPTURE_MODE = "stereo-capture-mode";
    public static final String KEY_VSDOF_MODE = "stereo-vsdof-mode";
    public static final String KEY_IMAGE_REFOCUS_MODE = "stereo-image-refocus";
    public static final String KEY_STEREO_DENOISE_MODE = "stereo-denoise-mode";
    public static final String PROPERTY_KEY_CLIENT_APP_MODE = "client.appmode";
    public static final String APP_MODE_NAME_MTK_DUAL_CAMERA = "MtkStereo";
    public static final String APP_MODE_NAME_MTK_DEFAULT_CAMERA = "Default";
    public static final String CAMERA_DISPLAY = "wwc2.camera.display.main";
    //主摄
    public static final String CAMERA_DISPLAY_MAIN = "1";
    //副摄
    public static final String CAMERA_DISPLAY_SUB = "0";
    public static final String CAMERA_ENABLE_DISPLAY = "wwc2_camera_enable_display";
    //恢复预览
    public static final String CAMERA_ENABLE_DISPLAY_SHOW = "1";
    /**
     * 暂停预览
     */
    public static final String CAMERA_ENABLE_DISPLAY_PAUSE = "0";

    public static Method getMethod(Class<?> clazz, String methodName,
                                   Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "[getMethod]", e);
        }
        return null;
    }
    public static Object callMethodOnObject(Object... args) {
        try {
            Method method = getMethod(Camera.class, "setProperty", String.class, String.class);
            return method.invoke(null, args);
        } catch (Exception e) {
            LogUtils.d("callMethodOnObject---异常" + e);
            LogUtils.d("callMethodOnObject---异常" + e.getCause());
            e.printStackTrace();
        }
        return null;
    }


}
