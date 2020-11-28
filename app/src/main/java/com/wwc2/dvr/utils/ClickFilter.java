package com.wwc2.dvr.utils;

import android.os.SystemClock;

import com.wwc2.corelib.utils.log.LogUtils;

/**
 * Created by Administrator on 2016/4/27 0027.
 */
public class ClickFilter
{
//    public static final long INTERVAL = 1000L; //最小点击时间间隔
    private static long lastClickTime = 0L; //上一次点击的时间

    public static boolean filter(Long minTime) {

        long time = SystemClock.uptimeMillis();
        LogUtils.d("click", "lastClickTime:" + lastClickTime + "\tuptime:" + time);
        if (lastClickTime == 0L) {
            lastClickTime = time;
            return false;
        } else {
            if ((time - lastClickTime) > minTime) {
                lastClickTime = time;
                return false;
            }else {
                return true;
            }
        }
    }
}