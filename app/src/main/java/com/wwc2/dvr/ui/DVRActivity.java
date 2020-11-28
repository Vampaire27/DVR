package com.wwc2.dvr.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.wwc2.dvr.RecordService;
import com.wwc2.dvr.data.Config;

public class DVRActivity extends Activity implements RecordService.SerListener{
    private static final String TAG = DVRActivity.class.getSimpleName();
    private RecordService recordService;
    private HomeKeyReceiver mHomeReceiver;
    private AccObserver accObserver = null;
    private final Uri uri_acc = Uri.parse(new StringBuilder().append("content://").append(Config.AUTHORITY).append("/")
            .append(Config.ACC_STATUS).toString());

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected  name=" + name.getClassName());
            try {
                recordService = ((RecordService.LocalBinder) service).getService();
                recordService.setSerListener(DVRActivity.this);
                recordService.toForegroundWindow();
            }catch (Exception e){
                Log.i(TAG, "onServiceConnected  Exception=" + e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            /* recordService.stopRecording();*/
        }
    };

    public static DVRActivity mDVRActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDVRActivity = this;
        accObserver = new AccObserver(new Handler());
        getContentResolver().registerContentObserver(uri_acc,
                        true, accObserver);
        Intent intent = new Intent(this, RecordService.class);
        intent.putExtra("package", getPackageName());
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        Log.i(TAG, "bindService success");

        mHomeReceiver = new HomeKeyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeReceiver, intentFilter);
    }

    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private class HomeKeyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                if (SYSTEM_DIALOG_REASON_HOME_KEY.equalsIgnoreCase(reason)) {
                    finish();
                }
            }
        }
    }

    private boolean getAccState() {
        String acc = getContentResolver().getType(uri_acc);
//        LogUtils.e(TAG, "acc:" + acc);
        if ("true".equals(acc)) {
            return true;
        }
        return false;
    }
    private final class AccObserver extends ContentObserver {
        public AccObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            boolean accOn = getAccState();
            Log.i(TAG, "activity accOn=" + accOn);
            if (!accOn) {
                finish();
            }
        }
    }

    @Override
    public void onBackground() {
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        unbindService(conn);
        unregisterReceiver(mHomeReceiver);
        getContentResolver().unregisterContentObserver(accObserver);
        super.onDestroy();
    }
}