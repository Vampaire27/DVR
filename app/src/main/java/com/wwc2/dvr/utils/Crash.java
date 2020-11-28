package com.wwc2.dvr.utils;

import com.wwc2.corelib.utils.log.LogUtils;

import java.lang.Thread.UncaughtExceptionHandler;

public class Crash implements UncaughtExceptionHandler {
    private UncaughtExceptionHandler mDefaultHandler;
    private static Crash INSTANCE = new Crash();
    private Crash() { }
    public static Crash getInstance() {
        return INSTANCE;
    }

    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        LogUtils.d("uncaughtException " + thread.getName() + throwable.toString());
    /*    if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            LogUtils.d("uncaughtException ex:" + ex);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }*/
    }

    public boolean handleException(Throwable ex) {
        if (ex == null)
            return false;
        return true;
    }
}
