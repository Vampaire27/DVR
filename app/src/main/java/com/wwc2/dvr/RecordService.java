package com.wwc2.dvr;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.autonavi.amapauto.gdarcameraservicedemo.GDArCameraService;
import com.google.gson.reflect.TypeToken;
import com.wwc2.common_interface.Provider;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.base.BaseService;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.dvr.bean.CommonBean;
import com.wwc2.dvr.bean.DeleteVideoBean;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.DBUtil;
import com.wwc2.dvr.data.DriveVideo;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.data.Stereo;
import com.wwc2.dvr.databinding.ActivityMainBinding;
import com.wwc2.dvr.parse.ResultPaser;
import com.wwc2.dvr.ui.AppBaseUI;
import com.wwc2.dvr.ui.ImgPopupWindow;
import com.wwc2.dvr.ui.ProgressBarWindow;
import com.wwc2.dvr.ui.VideoPopupWindow;
import com.wwc2.dvr.ui.filemanager.RecordFileDataBase;
import com.wwc2.dvr.ui.filemanager.RecordFileManager;
import com.wwc2.dvr.ui.filemanager.RecordFilePopWindowTest;
import com.wwc2.dvr.ui.record.CameraHandlerThread;
import com.wwc2.dvr.ui.record.RecordContract;
import com.wwc2.dvr.ui.record.RecordPresenter;
import com.wwc2.dvr.ui.reversing.ReversinPopupWindow;
import com.wwc2.dvr.ui.settings.SettingPopWindow;
import com.wwc2.dvr.utils.ClickFilter;
import com.wwc2.dvr.utils.FileUtils;
import com.wwc2.dvr.utils.SDCardUtils;
import com.wwc2.dvr.utils.SPUtils;
import com.wwc2.dvr.utils.ToastUtils;
import com.wwc2.dvr.utils.Utils;
import com.wwc2.dvr.widget.CommonDialog;
import com.wwc2.dvr_interface.DVRDefine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordService extends BaseService implements RecordContract.V, View.OnClickListener ,ReversinPopupWindow.CheckClickListener {

    private static final String TAG = "RecordService";
    private static final int NOTIFICATION_ID = 8891;
    //Channel ID 必须保证唯一
    private static final String CHANNEL_ID = "com.wwc2.dvr.notification.channel";
    /**
     * CAMERA message.
     */
    private static final String CAMERA = "com.android.wwc2.camera";
    /**
     * 360全景
     */
    private static final String PANORAMIC = "com.wwc2.Panoramic";

    /**
     * 系统切源广播
     */
    private static final String CURRENT_PACKAGENAME = "com.wwc2.framework.action.CURRENT_PACKAGENAME";

    private final Uri uri_acc       = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.ACC_STATUS);
    private final Uri uri_dvr       = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.DVR_ENABLE);
    public static Uri uri_camera    = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.CAMERA_STATUS);
    public static Uri uri_turnlight = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.TURN_LIGHT);
    private static Uri uri_cameraType = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.WY_CAMERA_TYPE);
    private static Uri uri_reverseMirror = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.REVERSE_MIRROR);

    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_BACK_KEY = "backKey";

    private ProgressBarWindow progressBarWindow;
    private SettingPopWindow settingPopWindow;
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private View floatView;
    private Binder mBinder;

    private VideoPopupWindow mVideoDialog;
    private ImgPopupWindow mImgDialog;

    private static boolean mCurACCStatus = false;

    private final int MSG_TAKE_PICTURE = 0x102;
    private boolean isForeGround;

    private CameraHandlerThread mCameraThread;
    private ActivityMainBinding binding;
    //录像是否保存 false :没保存
    private boolean isSave = false;
    private boolean isShowUI = true;
    private Context mContext = null;
    private boolean isTurnShowUI = true;
    private boolean isVoiceShowUi = true;

    //录像没开启
    private static final int MSG_VIDEO_NO_OPEN = 200;

    private Uri caruri = Uri.parse("content://wwc2.server.provider.carinfo/sensor");

    /**
     * 自动检测功能标识
     * true=正在检测中，false=未检测
     */
    private boolean SYNC_CHECK_CAMERA_NODE = false;

    private RecordPresenter recordPresenter;
    private RecordFileManager recordFileManager = null;
    private RecordFileDataBase recordFileDataBase = null;

    RecordFilePopWindowTest mRecordFilePopWindowTest;

    //重启系统标识
    private boolean REBOOT_SYSTEM = false;
    private AppBaseUI mAppBaseUI;

    private boolean mRecordBefore = false;

    int location;

    private final Handler mainHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CameraHandlerThread.MSG_RECORD_STATUS:
                    setRecordStatus((Boolean) msg.obj);
                    break;
                case MSG_TAKE_PICTURE:
                    if (msg.obj != null) {
                        takePicture(false, (Integer) msg.obj, false);
                    } else {
                        LogUtils.d("MSG_TAKE_PICTURE obj ==null");
                    }
                    break;
                case Config.MSG_SYNC_OK:
                    syncLoading(false, null);
                    break;
                case MSG_VIDEO_NO_OPEN:
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.video_no_open), Toast.LENGTH_SHORT).show();
                    break;
                case Config.AUTO_CLOSE:
                    mReversinPopupWindow.dismissPop();
                    break;
                case Config.MSG_WRITE_TIMER:
                    removeMessages(Config.MSG_WRITE_TIMER);
                    LogUtils.d(TAG, "MSG_WRITE_TIMER 保存成功...");
                    binding.tvCheckInfo.setText(getContext().getString(R.string.write_ok_check));
                    mainHandler.sendEmptyMessageDelayed(Config.ISAUTOCHECK_FIAG, 1000);
                    break;
                case Config.CAMERA_REVERSING:
                    checkReversing();
                    break;
                case Config.START_RECORDING:
                    startRecording();
                    break;
                case Config.CLEARVIDEO:
                    ToastUtils.showLongSafe(getString(R.string.video_delete_str));
                    mainHandler.removeMessages(Config.MSG_MEMORY_CHECK);
                    break;
                case Config.MSG_MEMORY_CHECK:
                    checkRemainSpace();
                    break;
                case Config.MSG_DESTROY:
                    mCameraThread.stopRecording();
                    mCameraThread.closeCamera();
                    break;
                case Config.TURN_LIGHT_MODE:
                    checkTurnLight();
                    break;
                case Config.MSG_SHOW_DOALOG:
                    initDeleteDialog(getString(R.string.delete_bitty_title));
                    break;
                case Config.MSG_SHOW_RECORD_STATUS:
                    mainHandler.removeMessages(Config.MSG_SHOW_RECORD_STATUS);
                    if (!getCameraStatus()) {
                        mainHandler.sendEmptyMessageDelayed(Config.MSG_SHOW_RECORD_STATUS, Config.DELAY_TIME / 8);
//                    LogUtils.d("MSG_SHOW_RECORD_STATUS----show=" + binding.ivRecordStatusShow.isShown());
                        binding.ivRecordStatusShow.setVisibility(binding.ivRecordStatusShow.isShown() ?
                                View.INVISIBLE : View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private AccObserver accObserver = new AccObserver(mainHandler);
    private DvrEnableObserver dvrObserver = new DvrEnableObserver(mainHandler);
    private CameraObserver cameraObserver = new CameraObserver(mainHandler);
    private TurnLightObserver turnLightObserver = new TurnLightObserver(mainHandler);
    private CameraTypeObserver cameraTypeObserver = new CameraTypeObserver(mainHandler);
    private ReverseMirrorObserver reverseMirrorObserver = new ReverseMirrorObserver(mainHandler);
    private RecordReceiver mRecordReceiver;

    private void voiceOpen(boolean open) {
        if (open) {
            if (!isForeGround()) {
                isVoiceShowUi = false;
                ApkUtils.runApk(mContext, "com.wwc2.dvr", null, false);
                toForegroundWindow();
            }
        } else {
            if (!isVoiceShowUi) {
                toBackgroundWindow();
            } else {
                mAppBaseUI.getFourCameraManager().SettingAllCameraView();
            }
            isVoiceShowUi = true;
        }
    }
    public void switchCameraView(int view){
        LogUtils.d(TAG,"-------switchCameraView---view=" + view);
        String curRecordType = RecordData.getInstance().recordType.getValue();
        switch (view) {
            case 0:  //FRONT_VIEW
                voiceOpen(true);
                mAppBaseUI.getFourCameraManager().SettingFrontCameraView();
                break;
            case 1: //BACK_VIEW
                if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
                    break;
                }
                voiceOpen(true);
                mAppBaseUI.getFourCameraManager().SettingBackCameraView();
                break;
            case 2: //LEFT_VIEW
                if (Config.TYPE_TWO_STREAM.equals(curRecordType) ||
                        Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                        Config.TYPE_ONE_STREAM.equals(curRecordType)) {
                    break;
                }
                voiceOpen(true);
                mAppBaseUI.getFourCameraManager().SettingLeftCameraView();
                break;
            case 3: //RIGHT_VIEW
                if (Config.TYPE_TWO_STREAM.equals(curRecordType) ||
                        Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                        Config.TYPE_ONE_STREAM.equals(curRecordType)) {
                    break;
                }
                voiceOpen(true);
                mAppBaseUI.getFourCameraManager().SettingRightCameraView();
                break;
            case 4: //QUART_VIEW
                voiceOpen(true);
                mAppBaseUI.getFourCameraManager().SettingAllCameraView();
                break;
            case 10://close front
            case 11://close back
            case 12://close left
            case 13://close right
                if (view == 12 || view == 13) {
                    if (Config.TYPE_TWO_STREAM.equals(curRecordType) ||
                            Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                            Config.TYPE_ONE_STREAM.equals(curRecordType)) {
                        break;
                    }
                } else if (view == 11) {
                    if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
                        break;
                    }
                }
                voiceOpen(false);
                break;
            case 55: //FOUR_VIEW
                toForegroundWindow();
                mAppBaseUI.getFourCameraManager().SettingAllCameraView();
                break;
            case -1: // exit
                mAppBaseUI.getFourCameraManager().SettingAllCameraView();
                toBackgroundWindow();
                break;
            default:
                break;
        }
    }

    //更新录像状态
    public void setRecordStatus(Boolean status) {

        ToastUtils.showShort(status ? getString(R.string.start_recording):getString(R.string.stop_recording));
        binding.captureButton.setSelected(status);    // turn blue
//        binding.captureButton.setBackgroundResource(status?R.mipmap.capture_pre :R.mipmap.capture_nor);

        RecordData.getInstance().recordState.setValue(status);

        mainHandler.removeMessages(Config.MSG_MEMORY_CHECK);

        mainHandler.removeMessages(Config.MSG_SHOW_RECORD_STATUS);
        binding.ivRecordStatusShow.setVisibility(status ? View.VISIBLE : View.INVISIBLE);

        LogUtils.d(TAG,"-------setRecordStatus---status=" + status);
        if (status) {
            //定时检测空间
            mainHandler.sendMessageDelayed(mainHandler.obtainMessage(Config.MSG_MEMORY_CHECK), Config.MEMORY_CHECK_INTERVAL);

            mainHandler.sendEmptyMessageDelayed(Config.MSG_SHOW_RECORD_STATUS, Config.DELAY_TIME / 8);
            showBottom(false);//避免录像红点显示慢。

            mAppBaseUI.disMissRecordPopWindow();
        }
    }

    public void updateRecordStatefromDrive(boolean status) {
        RecordData.getInstance().recordState.setValue(status);
        binding.captureButton.setSelected(status);
//        binding.captureButton.setBackgroundResource(status ? R.mipmap.capture_pre : R.mipmap.capture_nor);

        mainHandler.removeMessages(Config.MSG_SHOW_RECORD_STATUS);
        binding.ivRecordStatusShow.setVisibility(status ? View.VISIBLE : View.INVISIBLE);
        if (status) {
            mainHandler.sendEmptyMessageDelayed(Config.MSG_SHOW_RECORD_STATUS, Config.DELAY_TIME / 8);
        }
    }

    @Override
    public String getMessageType() {
        return DVRDefine.MODULE;
    }

    @Override
    public IBinder onBind(Intent intent) {
        String packageName = intent.getStringExtra("package");
        LogUtils.e("onBind,packageName:" + packageName);
        if (getPackageName().equals(packageName)) {
            mBinder = new LocalBinder();
        }
        if ("com.wwc2.networks".equals(packageName)) {
            LogUtils.d("-----onBind  networks----------------->");
            mBinder = new CarDvrImpl(this, getContext());
        }
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        String packageName = intent.getStringExtra("package");

        if (getDvrEnable()) {
            if ("com.wwc2.networks".equals(packageName)) {
                LogUtils.e("断开连接...onUnbind,packageName:" + packageName);
                Intent reboot = new Intent();
                reboot.setAction("com.wwc2.dvr.reboot");
                sendBroadcast(reboot);
            }
        }

        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // The Priority.
        final int priority = android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY;//THREAD_PRIORITY_FOREGROUND;
        // Changes the Priority of the calling Thread!
        android.os.Process.setThreadPriority(priority);
        // Changes the Priority of passed Thread (first param)
        android.os.Process.setThreadPriority(android.os.Process.myTid(), priority);

        getContentResolver().unregisterContentObserver(accObserver);
        getContentResolver().registerContentObserver(uri_acc, true, accObserver);

        getContentResolver().unregisterContentObserver(dvrObserver);
        getContentResolver().registerContentObserver(uri_dvr, true, dvrObserver);

        getContentResolver().unregisterContentObserver(cameraObserver);
        getContentResolver().registerContentObserver(uri_camera, true, cameraObserver);

        getContentResolver().unregisterContentObserver(turnLightObserver);
        getContentResolver().registerContentObserver(uri_turnlight, true, turnLightObserver);

        if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
            //网用的需要要设置界面设置制式。
            getContentResolver().unregisterContentObserver(cameraTypeObserver);
            getContentResolver().registerContentObserver(uri_cameraType, true, cameraTypeObserver);

            getContentResolver().unregisterContentObserver(reverseMirrorObserver);
            getContentResolver().registerContentObserver(uri_reverseMirror, true, reverseMirrorObserver);
        }

        SYNC_CHECK_CAMERA_NODE = false;

        LogUtils.e("RecordService onCreate...重置锁状态.");

        REBOOT_SYSTEM = false;
        mContext = this;

        //初始化SPUtils相关数据
        RecordData.getInstance().readDvrConfig(this);

        recordPresenter = new RecordPresenter();
        recordPresenter.onAttach(this);
        isSave = false;

        recordFileDataBase = RecordFileDataBase.getDataBase();
        recordFileManager = RecordFileManager.getInstance(mContext);
        recordFileDataBase.onCreate(this);

        DvrApplication.setInitCamera(false);

        mCurACCStatus = getAccState();
        createFloatWindow();
        DvrApplication.hideSystemUI(floatView);

        mRecordReceiver = new RecordReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        intentFilter.addAction(Config.ACTION_MSG_SAVE_OK);
//        intentFilter.addAction(Config.FULL_TYPE);
//        intentFilter.addAction(Config.SPLIT_TYPE);
//        intentFilter.addAction(Config.SPLIT_TYPE_UP_DOWN);
        intentFilter.addAction(Config.ACTION_CRASH);
//        intentFilter.addAction(Config.SENSOR_TYPE);
        intentFilter.addAction(CAMERA);
        intentFilter.addAction(PANORAMIC);
        intentFilter.addAction(CURRENT_PACKAGENAME);
        intentFilter.addAction(Config.ACTION_REBOOT_SYSTEM);
        //zhongyang.hu
        intentFilter.addAction(Config.TAKEPHOTO_V9);
        intentFilter.addAction(Config.ACTION_YY_AI);
        intentFilter.addAction(Config.ACTION_EXIT_DVR);
        intentFilter.addAction(Config.ACTION_DVR_VIDEO);
        registerReceiver(mRecordReceiver, intentFilter);

        //增加存储设备的检测
        IntentFilter deviceFilter = new IntentFilter();
        deviceFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        deviceFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        deviceFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        deviceFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        deviceFilter.addDataScheme("file");
        registerReceiver(mDeviceReceiver, deviceFilter);

        setForegroundService();

        //通知绑定
        Intent reboot = new Intent();
        reboot.setAction("com.wwc2.dvr.reboot");
        sendBroadcast(reboot);

//        updateSensor();
        LogUtils.e("RecordService onCreate end");
    }

    /**
     * wangpeng
     * 检测内存空间及删除视频文件
     *
     * 始终保留3个未加锁的文件不删除，设计如此
     * 防止正在录制的文件被删除导致的问题
     */
    public  int pop_time = 3;
    private CommonDialog deleteDialog;
    public void checkRemainSpace() {
        int curLocation = location;
        //在ACC OFF情况下USB可能不挂载上。
        if (location >= Config.DIR_LOCAL_USB0 && location <= Config.DIR_LOCAL_TFCARD) {
            curLocation = StorageDevice.NAND_FLASH;
        }
        long freeBytes = SDCardUtils.getFreeBytesNew(curLocation);//返回值:MB
        if (freeBytes <= 0) { //may be memory check error.
            //暂不作处理，底层会有判断空间。
            return;
        }

        if(freeBytes <= SDCardUtils.LEVEL_1_250M ){
            //提示
//            mainHandler.sendEmptyMessage(Config.CLEARVIDEO);
            initDeleteDialog(getString(R.string.delete_bitty_title));
            mCameraThread.stopRecording();
        } /*else if (freeBytes <= SDCardUtils.LEVEL_2_500M) {//经讨论，此等级去掉2020-08-01
            if (pop_time > 0) {
                initDeleteDialog(getString(R.string.deletetitle));
                pop_time--;
            }
        } else {
            pop_time = 3;//提示3 次;
        }*/

        if (freeBytes <= SDCardUtils.LEVEL_3_1G || freeBytes <= SDCardUtils.getTotalSpace(curLocation) / 10) {//2G或总容量的10%
            LogUtils.d(TAG, "Need delete file ,memory free size = " + freeBytes + ", pop_time=" + pop_time);
            if (!DBUtil.deleteOldestVideoFile()) {
                LogUtils.d(TAG, "the is no unlock file to delete.");
            }
        }

        if (!mainHandler.hasMessages(Config.MSG_MEMORY_CHECK)) {
            mainHandler.sendMessageDelayed(mainHandler.obtainMessage(Config.MSG_MEMORY_CHECK),
                    Config.MEMORY_CHECK_INTERVAL);
        }
    }

    private void initDeleteDialog(String title) {
        deleteDialog = CommonDialog.getInstance(this);
        if (!deleteDialog.getIsShowing()) {
            deleteDialog.setMessage(title).setSingle(false)
                    .setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
                        @Override
                        public void onPositiveClick(List<CommonBean> list) {
                            Intent intent = new Intent(DvrApplication.getContext(), ManagerService.class);
                            intent.putExtra(ManagerService.TASK_KEY, ManagerService.TASK_DELETA_FILE);
                            ArrayList<CommonBean> mlist = new ArrayList<>();
                            mlist.addAll(list);
                            intent.putParcelableArrayListExtra(ManagerService.TASK_EXTRA, mlist);
                            startService(intent);
                            deleteDialog.onDismiss();
                        }

                        @Override
                        public void onNegtiveClick() {
                            deleteDialog.onDismiss();
                        }
                    })
                    .show();
        }
    }

    /**
     * 通过通知启动服务
     */
    public void setForegroundService() {
        if (Build.VERSION.SDK_INT > 26) {
            //设定的通知渠道名称
            String channelName = getString(R.string.channel_name);
            //设置通知的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            //构建通知渠道
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription("DVR record service");
            //在创建的通知渠道上发送通知
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
            builder.setSmallIcon(R.drawable.ic_launcher) //设置通知图标
//                .setContentTitle(notificationTitle)//设置通知标题
//                .setContentText(notificationContent)//设置通知内容
                    .setAutoCancel(false) //用户触摸时，自动关闭
                    .setOngoing(true);//设置处于运行状态
            //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context
                    .NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
            startForeground(NOTIFICATION_ID, builder.build());
        } else {
            Notification.Builder builder = new Notification.Builder(this)
                    //设置小图标
                    .setSmallIcon(R.mipmap.ic_launcher)
                    //设置通知标题
                    .setContentTitle("CameraService is running")
                    //设置通知内容
                    .setContentText("never kill me！");
            //设置通知时间，默认为系统发出通知的时间，通常不用设置
            //.setWhen(System.currentTimeMillis());
            //通过builder.build()方法生成Notification对象,并发送通知,id=1
            startForeground(NOTIFICATION_ID, builder.build());
        }
    }

    private void createFloatWindow() {
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();

        //params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;//这个属性会导致挂倒车不能正常显示
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.TRANSPARENT; // 设置图片格式，效果为背景透明
        params.gravity = Gravity.START | Gravity.TOP;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        params.width = 1;
        params.height = 1;
        floatView = LayoutInflater.from(this).inflate(R.layout.activity_main, null, false);
        floatView.findViewById(R.id.camera_cutover).setOnClickListener(this);
        binding = DataBindingUtil.bind(floatView);
        binding.setPresenter(recordPresenter);
        mAppBaseUI = new AppBaseUI(this,binding);
//        binding.captureButton.setEnabled(false);
//        TimerUtils.setTimer(this, 1000, new Timerable.TimerListener() {
//            @Override
//            public void onTimer(int paramInt) {
//                if (binding != null) {
//                    binding.captureButton.setEnabled(true);
//                }
//            }
//        });
        wm.addView(floatView, params);

        mCameraThread = new CameraHandlerThread(this, "CameraThread",
                binding.cameraViewBack, DvrApplication.getPlatform());
        CameraRawService.setCameraHandlerThread(mCameraThread);
        //hzy_ar
        GDArCameraService.setCameraHandlerThread(mCameraThread);

        mCameraThread.setMainHandler(mainHandler);
        mCameraThread.setCurAccStatus(mCurACCStatus);
        mCameraThread.setAppBaseUI(mAppBaseUI);

        binding.cameraViewBack.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogUtils.d(TAG, " camera surface is Created ...");
                if (mCameraThread != null) {
                    mCameraThread.setSurfaceCreate(true);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                LogUtils.d(TAG, " camera surface is destroy ...");
                String acc = getContentResolver().getType(uri_acc);
                if (acc.equals("true") && !REBOOT_SYSTEM) {
                    mCameraThread.setHasPreViewed(false);
                    mCameraThread.rebootCamera();
                } else {
                    LogUtils.d(TAG, " 后摄预览中断...休眠状态不处理...");
                }

                if (mCameraThread != null) {
                    mCameraThread.setSurfaceCreate(false);
                }
            }
        });

        mCameraThread.start();

        mAppBaseUI.getFourCameraManager().disableCameraView();

        floatView.setFocusableInTouchMode(true);
        floatView.setOnClickListener(this);
        binding.layoutCheckCamera.getBackground().setAlpha(120);

        binding.btnBottomShow.setOnClickListener(this);
        binding.fileButton.setOnClickListener(this);
        binding.photoButton.setOnClickListener(this);
        binding.btnBottomBack.setOnClickListener(this);

        setLockStatus(RecordData.getInstance().lockState/*.getValue()*/, true);
        mAppBaseUI.getFourCameraManager().setCameraPlatformType(mContext, RecordData.getInstance().recordType.getValue());//将配置同步到系统中
    }

    public void createDvr() {
        if (mCameraThread != null) {
            LogUtils.d(TAG, "createDvr mCameraThread.getIsBackPreView()" + mCameraThread.getIsBackPreView());
            if (!getAccState()) {
                try {
                    Thread.sleep(3000); //摄像头工作启动到正常开启后稳定的时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!mCameraThread.getIsBackPreView()) {
                LogUtils.d(TAG, "createDvr checkCamera!");
                boolean resu = mCameraThread.checkCamera();
                LogUtils.d(TAG, "createDvr resu!" + resu);
                if (resu) {
                    LogUtils.d("mCameraThread ==resu" + resu);
                } else {
                    LogUtils.e(TAG, "createDvr getIsBackPreView 异常......");
                    mCameraThread.openCamera(true);
                }
            }
        } else {
            reStartSurfaceView();
        }
    }

    private void reStartSurfaceView(){
        LogUtils.d(TAG, "createDvr 开始等待4000s");
        try {
            Thread.sleep(3000); //摄像头工作启动到正常开启后稳定的时间

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtils.d(TAG, "createDvr 等待完成...启动dvr..");

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG, "createDvr 状态不正常，可重启");
                createFloatWindow();
            }
        });
    }

    /**
     * 接收CameraHandlerThread信号源，轮询查询倒车辅助线是否显示
     */
    private void setTrackView(){
        if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
            //网用的需要动态轨迹线
            return;
        }
        LogUtils.d("WPTAG","setTrackView query -->" +query_camera_guide_line());
        if (query_camera_guide_line()) {
            if (binding.ivTrack != null) {
                binding.ivTrack.setVisibility(View.VISIBLE);
            }
        }else {
            binding.ivTrack.setVisibility(View.GONE);
        }
    }

    /**
     * 查询倒车镜像状态
     */
    private boolean query_camera_guide_line() {
        boolean ret = false;
        final String string = query(com.wwc2.avin_interface.Provider.CAMERA_GUIDE_LINE());
        if (!TextUtils.isEmpty(string)) {
            try {
                ret = Boolean.valueOf(string);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 根据名称查找值
     */
    protected String query(String name) {
        String ret = null;
        if (null != getContext()) {
            ContentResolver resolver = getContext().getContentResolver();
            if (null != resolver) {
                Cursor cursor = resolver.query(Provider.ProviderColumns.CONTENT_URI, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        String string = cursor.getString(cursor.getColumnIndex(name));
                        if (!TextUtils.isEmpty(string)) {
                            ret = string;
                        }
                    }
                    cursor.close();
                }
            }
        }
        return ret;
    }

    public void closeFloatView() {
        if (wm != null) {
            wm.removeView(floatView);
            wm = null;
            LogUtils.d(TAG, "closeFloatView()");
        }
//        this.stopSelf();
    }

    @Override
    public void onDestroy() {
        LogUtils.e(TAG, "dvr onDestroy.............");

        syncLoading(false, null);

        isForeGround = false;
        if (mCameraThread != null) {
            mCameraThread.stopRecording();
            mCameraThread.closeCamera();
        }
        recordPresenter.onDetach();
        recordPresenter.onDestroy();

        recordFileManager.onDestory();

        DvrApplication.showSystemUI(floatView);

        closeFloatView();

        if (mRecordReceiver!=null){
            unregisterReceiver(mRecordReceiver);
            mRecordReceiver = null;
        }
        try {
            unregisterReceiver(mDeviceReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        stopForeground(true);


        LogUtils.e(TAG, "dvr onDestroy.............over？？");

        //如果close没有完成，就remove了消息？
//        mCameraThread.removeMes();
//        mCameraThread.getLooper().quitSafely();

       // getContentResolver().unregisterContentObserver(accObserver);
        super.onDestroy();
    }

    /**
     * 点击View时判断是否是倒车
     */
    private ReversinPopupWindow mReversinPopupWindow;
    @Override
    public void onClick(View v) {

        boolean isReversing = getCameraStatus();
        LogUtils.d(TAG, "onClick  view... isReversing --->" + isReversing + ", id=" + v.getId());

        if (ClickFilter.filter(500L)) {
            return;
        }
        switch (v.getId()) {
            case R.id.camera_cutover:
                LogUtils.d(TAG, "cameraCutover...");
                String display = SystemProperties.get(Stereo.CAMERA_DISPLAY);
                LogUtils.d(TAG, "cameraCutover..."+display);
                if(display.equals(Stereo.CAMERA_DISPLAY_MAIN)){
                    SystemProperties.set(Stereo.CAMERA_DISPLAY, Stereo.CAMERA_DISPLAY_SUB);
                }
                if(display.equals(Stereo.CAMERA_DISPLAY_SUB)){
                    SystemProperties.set(Stereo.CAMERA_DISPLAY, Stereo.CAMERA_DISPLAY_MAIN);
                }
                return;
            case R.id.file_button:
                showFilePopWindow(floatView);
                return;
            case R.id.btn_bottom_show:
                showBottom(!binding.viewBottom.isShown());
                return;
            case R.id.photo_button:
                onClickTakePicture();
                return;
            case R.id.btn_bottom_back:
                if (mAppBaseUI != null) {
                    mAppBaseUI.sendBack(mContext);
                }
                return;
        }
        if (isReversing){
            if (!isSave){
                if (mReversinPopupWindow ==null){
                    mReversinPopupWindow = new ReversinPopupWindow(getContext(), mainHandler,this);
                }

                if (binding.layoutCheckCamera.getVisibility() !=View.VISIBLE){
                    if(!mReversinPopupWindow.isShow()){
                        mReversinPopupWindow.show(v);
                    }
                }

                isSave = false; //重置状态
                if (mainHandler !=null){
                    mainHandler.removeMessages(Config.AUTO_CLOSE);
                    mainHandler.sendEmptyMessageDelayed(Config.AUTO_CLOSE,Config.DELAY_TIME);
                }
            }
            return;
        }

        LogUtils.d(TAG, "onClick  view...-wm=" + wm);
        if (binding != null && binding.viewBottom != null) {
            showBottom(!binding.viewBottom.isShown());
        }
    }

    /**
     * 开始自动检测popWindow
     */
    @Override
    public void checkClick() {
         if (binding.layoutCheckCamera != null) {
             setCheckUIdefault();
             mReversinPopupWindow.dismissPop();
      }
    }

    /**
     * 检测默认UI样式
     */
    private void setCheckUIdefault(){
        binding.layoutCheckCamera.setVisibility(View.VISIBLE);
        binding.rlCheckSuccess.setVisibility(View.GONE);
//        if (isAutoCheck){
        binding.tvCheckInfo.setText(getContext().getString(R.string.check_warn_info_check));
//        }else {
//            binding.tvCheckInfo.setText(getContext().getString(R.string.check_warn_info));
//        }

    }

    private final int DEF_HIDE_UI = 5000;
    private Runnable timeRun = new Runnable() {
        @Override
        public void run() {
            showBottom(false);
        }
    };

    public void startHideBarTime(){
        mainHandler.removeCallbacks(timeRun);
        mainHandler.postDelayed(timeRun, DEF_HIDE_UI);
    }

    public void stopHideBarTime(){
        mainHandler.removeCallbacks(timeRun);
    }

    @Override
    public void showSettingPopWindow(View view) {
        if (settingPopWindow == null) {
            settingPopWindow = new SettingPopWindow(getContext(), onSettingsDismissListener, mAppBaseUI);
        }
        settingPopWindow.show(view);

        LogUtils.d(TAG, "showSettingPopWindow...show");

        stopHideBarTime();
    }

    private void syncLoading(boolean isShow, String text){
        try {
            if (isShow) {
                showBottom(false);
                if (floatView != null) {
                    if (progressBarWindow == null) {
                        progressBarWindow = new ProgressBarWindow(getContext());
                    }
                    progressBarWindow.show(floatView, text);//getContext().getString(R.string.str_setuping));
                }

            } else {
                if (progressBarWindow != null) {
                    progressBarWindow.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //是否自动检测
//    private boolean isAutoCheck = false;

    private SettingPopWindow.OnSettingsDismissListener onSettingsDismissListener =
            new SettingPopWindow.OnSettingsDismissListener(){

        @Override
        public void onSettingsDismiss(int type) {
            LogUtils.d(TAG, "onSettingsDismiss...close...type=" + type);

            startHideBarTime();

            switch (type){
                case Config.CAMERA_REBOOT_PREVIEW:
                    LogUtils.d(TAG, "onSettingsDismiss..1.开始loading...功能锁住...");
                    syncLoading(true, getContext().getString(R.string.str_setuping));
                    mCameraThread.rebootCamera();
                    break;
                case Config.CAMERA_REBOOT_VIDEO:
                case Config.CAMERA_REBOOT_RECORD:
                    LogUtils.d(TAG, "onSettingsDismiss..2.开始loading...功能锁住...");
                    syncLoading(true, getContext().getString(R.string.str_setuping));
                    mCameraThread.rebootVideo(type);
                    break;
            }
        }

        @Override
        public void onAotuCheckListener() {
            SYNC_CHECK_CAMERA_NODE = true;
            LogUtils.e("onAotuCheckListener.....触发自动检测...上锁=SYNC_CHECK_CAMERA_NODE = " + SYNC_CHECK_CAMERA_NODE);

            settingPopWindow.dismiss();
            showBottom(false);
            setCheckUIdefault();
        }

        @Override
        public void onMainAndSubValue(String onMainAndSubValue) {

        }

        @Override
        public void onLocationListener() {
            LogUtils.d("onLocationListener-");
            if (recordFileDataBase != null && mContext != null) {
                recordFileDataBase.onDestroy();
                recordFileDataBase.onCreate(mContext);
            }
        }
    };

    @Override
    public void showFilePopWindow(View view) {
        if (mRecordFilePopWindowTest == null) {
            mRecordFilePopWindowTest = new RecordFilePopWindowTest(this, fileDismissListenerTest);
        }
        mRecordFilePopWindowTest.show(view, recordFileManager, mAppBaseUI);

   /*     if (filePopupWindow == null) {
            filePopupWindow = new RecordFilePopWindow(getContext(), fileDismissListener);
        }
        filePopupWindow.show(view, recordFileManager);*/

        LogUtils.d(TAG, "showFilePopWindow...show");

        stopHideBarTime();
    }

    private RecordFilePopWindowTest.OnFileDismissListener fileDismissListenerTest =
            new RecordFilePopWindowTest.OnFileDismissListener() {
                @Override
                public void onFileDismiss() {
                    LogUtils.d(TAG, "onFileDismiss...close");
                    startHideBarTime();
                }

                @Override
                public void onVideoClick(int position, List<DriveVideo> list) {
                    LogUtils.d("onVideoClick---------------");
                    mVideoDialog = new VideoPopupWindow(getContext(), position, list, mAppBaseUI);
                    mVideoDialog.show(floatView);
                }

                @Override
                public void onImgClick(String path) {
                    LogUtils.d("onImgClick---------------");
                    mImgDialog = new ImgPopupWindow(getContext(), path);
                    mImgDialog.show(floatView);
                }

                @Override
                public void onFormatClick(String path) {
                    LogUtils.e("onFormatClick----------");
                    formatDevice(path);
                }
            };

    public interface FormatCallBack {
        public void formatsuccess();
    }
    class FormatThread implements Runnable {
        private FormatCallBack callBack;
        private String path;

        public FormatThread(String path, FormatCallBack callBack) {
            this.path = path;
            this.callBack = callBack;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " start execute");
//                FileUtils.formatDevice(getContext(), path);
//                FileUtils.mountDevice(getContext(), "/storage/sdcard1/");//path);

//                path = path + ConstantsData.VIDEO_DIR;
                File directory = new File(path);
                FileUtils.delAllFile(directory);
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + " end execute");
                this.callBack.formatsuccess();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void formatDevice(String path) {
        if (path == null || !FileUtils.isDiskMounted(getContext(), path)) {
            return;
        }
        if (mRecordFilePopWindowTest.isFileShowing()) {
            mRecordFilePopWindowTest.dismiss();
        }
        showBottom(false);

        LogUtils.d("formatDevice---------------path=" + path);
        if (mAppBaseUI != null && mAppBaseUI.getFourCameraManager().isRecorded()) {
            stopRecording();
        }

        ToastUtils.showShort(getContext().getString(R.string.str_start_format));
        LogUtils.d("onFormatClick---------------开始格式化!");
        syncLoading(true, getContext().getString(R.string.str_formating));

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(new FormatThread(path, new FormatCallBack() {
            @Override
            public void formatsuccess() {
                LogUtils.d("onFormatClick---------------格式化完成!");
                mainHandler.sendMessage(mainHandler.obtainMessage(Config.MSG_SYNC_OK, true));
                ToastUtils.showShort(getContext().getString(R.string.str_format_success));
                LogUtils.d("onFormatClick---------------格式化完成! end");
            }
        }));
        executorService.shutdown();
    }

    @Override
    public void toBackgroundWindow() {
        if(activitylistener != null){
            activitylistener.onBackground();
        }
        isForeGround = false;
        params.width = 1;
        params.height = 1;
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if (wm != null) {
            showBottom(false);
            wm.updateViewLayout(floatView, params);
        }
        if (null != settingPopWindow) settingPopWindow.dismiss();
        if (null != mRecordFilePopWindowTest) mRecordFilePopWindowTest.dismiss();
        if (null != mVideoDialog) mVideoDialog.dismiss();
        if (null != mImgDialog) mImgDialog.dismiss();
        if (null != progressBarWindow) progressBarWindow.dismiss();
        disRecordPopWindow();

        binding.ivTrack.setVisibility(View.GONE);

        mAppBaseUI.getFourCameraManager().disableCameraView();
    }

    @Override
    public void toForegroundWindow() {
        LogUtils.d(TAG, "...toForegroundWindow...");

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        wm.updateViewLayout(floatView, params);

        showBottom(true);

        isForeGround = true;
        DvrApplication.hideSystemUI(floatView);
        //检测倒车状态
        if (!getCameraStatus()) {
            //zhongyang show camera
            binding.ivTrack.setVisibility(View.GONE);
            mAppBaseUI.getFourCameraManager().enableCameraView();
        } else {
            LogUtils.d("-------------setTrackView-------------");
            setTrackView();
            mAppBaseUI.getFourCameraManager().SettingBackCameraView();
        }
    }

    public boolean isForeGround() {
        return isForeGround;
    }

    private void switchDVR() {
        LogUtils.e(TAG, "...switchDVR...检测信号...isSave=" + isSave);
        if(isSave){
            mainHandler.sendEmptyMessage(Config.ISAUTOCHECK_FIAG);
        }

        binding.btnBottomShow.setVisibility(View.VISIBLE);
        binding.layoutNosignal.setVisibility(View.GONE);
        binding.layoutCheckCamera.setVisibility(View.GONE);
        if (mReversinPopupWindow != null){
            mReversinPopupWindow.dismissPop();
        }

        binding.ivTrack.setVisibility(View.GONE);

        //显示录像状态图标
        boolean record = RecordData.getInstance().recordState.getValue();
        binding.ivRecordStatusShow.setVisibility(record ? View.VISIBLE : View.INVISIBLE);
        mainHandler.removeMessages(Config.MSG_SHOW_RECORD_STATUS);
        LogUtils.e(TAG, "-------showBottom---status=" + record);
        if (record) {
            mainHandler.sendEmptyMessageDelayed(Config.MSG_SHOW_RECORD_STATUS, Config.DELAY_TIME / 8);
        }

        LogUtils.e(TAG, "...switchDVR...isShowUI=" + isShowUI + ", isTurnShowUI=" + isTurnShowUI);//false
        //aaaaaaaaaaaaaaaaaaa
        if (!isShowUI) {
            toBackgroundWindow();
        } else {
            DvrApplication.hideSystemUI(floatView);
            mAppBaseUI.getFourCameraManager().SettingAllCameraView();
        }
        isShowUI = true;
    }

    private boolean getAccState() {
        String acc = getContentResolver().getType(uri_acc);
        LogUtils.e(TAG, "acc:" + acc);
        if ("true".equals(acc)) {
            return true;
        }
        return false;
    }

    private boolean getDvrEnable() {
        String dvrEnable = getContentResolver().getType(uri_dvr);
        if ("false".equals(dvrEnable)) {
            return false;
        }
        return true;
    }

    @Override
    public void startRecording() {
        location = RecordData.getInstance().mutableLocation;
        LogUtils.d("---startRecording----location=" + location);
        if (!FileUtils.isDiskMounted(mContext, StorageDevice.getPath(location))) {
            if (getAccState()) {
                mAppBaseUI.disMissRecordPopWindow();
                Toast.makeText(getContext(), getString(R.string.nodevice), Toast.LENGTH_SHORT).show();
                return;
            } else {//ACC OFF情况下未挂载，先录制到本地。2020-01-19
                switch (location) {//由于底层设置存储位置接口改动，需由底层处理。2020-06-18
                    case StorageDevice.USB :
                        location = Config.DIR_LOCAL_USB0;
                        break;
                    case StorageDevice.USB1:
                        location = Config.DIR_LOCAL_USB1;
                        break;
                    case StorageDevice.USB2:
                        location = Config.DIR_LOCAL_USB2;
                        break;
                    case StorageDevice.USB3:
                        location = Config.DIR_LOCAL_USB3;
                        break;
                    case StorageDevice.MEDIA_CARD:
                        location = Config.DIR_LOCAL_TFCARD;
                        break;
                    default:
                        return;
                }
            }
        }

        mainHandler.post(checkMemoryAndStartRecording);
        startHideBarTime();
    }

    @Override
    public void showRecordPopWindow(View v) {
        mAppBaseUI.showRecordPopWindow(v);
    }

    public void disRecordPopWindow() {
        mAppBaseUI.disMissRecordPopWindow();
    }

    private Runnable checkMemoryAndStartRecording = new Runnable() {
        @Override
        public void run() {
            int curLocation = location;
            //在ACC OFF情况下USB可能不挂载上。
            if (curLocation >= Config.DIR_LOCAL_USB0 && curLocation <= Config.DIR_LOCAL_TFCARD) {
                curLocation = StorageDevice.NAND_FLASH;
            }
            long freeBytes = SDCardUtils.getFreeBytesNew(curLocation);//返回值:MB
            LogUtils.d("RecordService", "---startRecording-freeBytes=" + freeBytes + ", LEVEL_1_250M=" + SDCardUtils.LEVEL_1_250M);

            if (freeBytes <= SDCardUtils.LEVEL_1_250M /** 250m*/) {
                mAppBaseUI.disMissRecordPopWindow();
                if (freeBytes <= 0) {//USB刚挂载，获取空间为0 2020-06-22
                    ToastUtils.showShort(mContext.getString(R.string.device_error));
                } else {
                    ToastUtils.showShort(mContext.getString(R.string.video_delete_str));
                    mainHandler.sendEmptyMessage(Config.MSG_SHOW_DOALOG);
                }
                //同步检测状态
            }else {
                if (!getAccState()) {
                    mAppBaseUI.getFourCameraManager().setRecordLocation(location);//设备不挂载上时，也通知底层，底层有作判断。
//                    mAppBaseUI.getFourCameraManager().setPhotoLocation(location);
                }
                mCameraThread.startRecording();
            }
        }
    };

    @Override
    public void stopRecording() {
        LogUtils.d("---stopRecording-");

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                startHideBarTime();
                binding.captureButton.setSelected(false);
                RecordData.getInstance().recordState.setValue(false);
            }
        });

        mCameraThread.stopRecording();
    }

    @Override
    public void onClickTakePicture( ) {
        startHideBarTime();
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_TWO_STREAM.equals(curRecordType) ||
                Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
            takePicture(false, Config.TWO_CAPTURE, false);
        } else if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            takePicture(false, Config.FRONT_CAPTURE, false);
        } else {
            takePicture(false, Config.FOUR_CAPTURE, false);//mAppBaseUI.getFourCameraManager().getDisplayMode());//白工确认，2020-06-05
        }
    }

    @Override
    public String takePicture(boolean isOff, final int channel, boolean network) {//network:true手机拍照
        location = RecordData.getInstance().mutableLocation;
        LogUtils.d(TAG, "takePicture channel=" + channel + ",location = " + location + ", acc=" + getAccState());
        //检测设备是否挂载
        if (!FileUtils.isDiskMounted(mContext, StorageDevice.getPath(location))) {
            if (getAccState() && !network) {//ACC ON状态，设备不存在时，手机仍要拍照，本地才不拍
                mAppBaseUI.disMissRecordPopWindow();
                ToastUtils.showShort(mContext.getString(R.string.nodevice));
                return "";
            } else {//ACC OFF情况下未挂载，先拍到本地
                switch (location) {//由于底层设置存储位置接口改动，需由底层处理。2020-06-18
                    case StorageDevice.USB:
                        location = Config.DIR_LOCAL_USB0;
                        break;
                    case StorageDevice.USB1:
                        location = Config.DIR_LOCAL_USB1;
                        break;
                    case StorageDevice.USB2:
                        location = Config.DIR_LOCAL_USB2;
                        break;
                    case StorageDevice.USB3:
                        location = Config.DIR_LOCAL_USB3;
                        break;
                    case StorageDevice.MEDIA_CARD:
                        location = Config.DIR_LOCAL_TFCARD;
                        break;
                    default:
                        return "";
                }
            }
        }

        //检测空间是否可用
        int curLocation = location;
        //在ACC OFF情况下USB可能不挂载上。
        if (location >= Config.DIR_LOCAL_USB0 && location <= Config.DIR_LOCAL_TFCARD) {
            curLocation = StorageDevice.NAND_FLASH;
        }
        long freeBytes = SDCardUtils.getFreeBytesNew(curLocation);
        if (freeBytes == 0 && curLocation != StorageDevice.NAND_FLASH) {
            curLocation = StorageDevice.NAND_FLASH;
            freeBytes = SDCardUtils.getFreeBytesNew(curLocation);
        }

        LogUtils.d(TAG, "takePicture channel=" + channel  + ",freeBytes=" + freeBytes + ",location = " + location);
        if (freeBytes <= SDCardUtils.MIN_TAKEPICTURE_SIZE /** 5M*/) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogUtils.d(TAG, "takePicture 空间不足,拍照失败!");
                    ToastUtils.showShort(getResources().getString(R.string.photo_err_size));
                }
            });
            return "1000";
        }

        if (mCameraThread != null) {
            LogUtils.d(TAG, "takePicture mCameraThread.getIsBackPreView()" + mCameraThread.getIsBackPreView());
            if (!getAccState()) {//ACC OFF情况下先等待2s，避免出现拍照黑屏的问题。
                try {
                    Thread.sleep(3000); //摄像头工作启动到正常开启后稳定的时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mCameraThread.isCameraBackOpened()) {
                if (!mCameraThread.getIsBackPreView()) {
                    mCameraThread.closeCamera();
                    mCameraThread.openCamera(true);
                }
            } else {
                mCameraThread.openCamera(true);
            }
        } else {
            reStartSurfaceView();
        }

        int count = 0;
        while (true) {//此处解决ACC OFF未预览时无法拍照的问题。
            if (mCameraThread == null || !mCameraThread.getIsBackPreView()) {
                count ++;
                if (count > 20) {
                    break;
                }
            } else {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogUtils.d(TAG, "takePicture count=" + count + ", preview=" + mCameraThread.getIsBackPreView());

        String image = mCameraThread.captureStill(location, channel);
//        if (!TextUtils.isEmpty(image)) {// /storage/sdcard0/dvr/pictures/IMG_2020-06-20_15-32-10.jpg
//            recordFileManager.updateDriveVideo(ConstantsData.TYPE_IMAGE, image, -1, false);
//        }
        return image;
//        return outputFile.getPath();
    }

    /**
     * ACCOff 情况下关闭DVR，防止电流过大默认20秒-->carnetwork中以设置
     */
    public void doclose(){

        LogUtils.d("---test---doclose---" + mCameraThread);
        LogUtils.d("---test---doclose---" + SPUtils.getLockStatus(getContext()));
        mainHandler.post(new Runnable() {
           @Override
           public void run() {
              //还原加锁状态
                RecordData.getInstance().lockState = SPUtils.getLockStatus(getContext());
          }
       });
        if (mCameraThread != null) {
            mCameraThread.closeCamera();
        }else {
            LogUtils.d("---test---mCameraThread ===null---");
        }
    }

    @Override
    public void setLockStatus(Boolean b, Boolean viewSet) {
        //
        //<!--lock_pre  lock_nor -->
        LogUtils.d("setLockStatus---b=" + b + ", viewSet=" + viewSet);
        if (viewSet) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (binding != null) {
                        if (b) {
                            binding.lockButton.setBackgroundResource(R.mipmap.btn_bottom_lock_p);
                        } else {
                            binding.lockButton.setBackgroundResource(R.mipmap.btn_bottom_lock_n);
                        }

                        startHideBarTime();
                        binding.lockButton.setSelected(b);
                    }

                    SPUtils.setLockStatus(mContext, b);
                }
            });
        }

        RecordData.getInstance().lockState = b;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (recordFileManager != null) {
                    String curRecordFile = recordFileManager.getCurRecordFileName(ConstantsData.TYPE_BACK);
                    if (curRecordFile != null && curRecordFile.contains(ConstantsData.BACK)) {
                        recordFileManager.updateDriveVideo(ConstantsData.TYPE_BACK, curRecordFile, -1, b);
                    }

                    curRecordFile = recordFileManager.getCurRecordFileName(ConstantsData.TYPE_FRONT);
                    if (curRecordFile != null && curRecordFile.contains(ConstantsData.FRONT)) {
                        recordFileManager.updateDriveVideo(ConstantsData.TYPE_FRONT, curRecordFile, -1, b);
                    }

                    curRecordFile = recordFileManager.getCurRecordFileName(ConstantsData.TYPE_LEFT);
                    if (curRecordFile != null && curRecordFile.contains(ConstantsData.LEFT)) {
                        recordFileManager.updateDriveVideo(ConstantsData.TYPE_LEFT, curRecordFile, -1, b);
                    }

                    curRecordFile = recordFileManager.getCurRecordFileName(ConstantsData.TYPE_RIGHT);
                    if (curRecordFile != null && curRecordFile.contains(ConstantsData.RIGHT)) {
                        recordFileManager.updateDriveVideo(ConstantsData.TYPE_RIGHT, curRecordFile, -1, b);
                    }

                    curRecordFile = recordFileManager.getCurRecordFileName(ConstantsData.TYPE_QUART);
                    if (curRecordFile != null && curRecordFile.contains(ConstantsData.QUART)) {
                        recordFileManager.updateDriveVideo(ConstantsData.TYPE_QUART, curRecordFile, -1, b);
                    }
                }
            }
        }).start();
    }

    @Override
    public Context getContext() {
        return this;
    }

    public class LocalBinder extends Binder {
        public RecordService getService() {
            return RecordService.this;
        }
    }

    private final class AccObserver extends ContentObserver {
        public AccObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            boolean accOn = getAccState();
            if (mCurACCStatus == accOn) {
                LogUtils.e("acc AccObserver return same status!" + mCurACCStatus);
                return;
            }

            mCurACCStatus = accOn;
            if (mCameraThread != null) {
                mCameraThread.setCurAccStatus(accOn);
            }
            LogUtils.e("AccObserver acc:" + accOn);

            if (!accOn) {
                mainHandler.sendMessageDelayed(mainHandler.obtainMessage(Config.MSG_DESTROY)
                        ,Config.ACC_DELAY_INTERVAL);
            } else {
                mainHandler.removeMessages(Config.MSG_DESTROY);
               if(!mCameraThread.isCameraBackOpened()){
                   mCameraThread.openCamera(true);
               }
                //todo : need...
//                mainHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        //还原加锁状态
//                        RecordData.getInstance().lockState = SPUtils.getLockStatus(getContext());
//                        setLockStatus(RecordData.getInstance().lockState, true);
//                    }
//                });
            }
        }
    }

    private final class DvrEnableObserver extends ContentObserver {
        public DvrEnableObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            boolean dvrEnable = getDvrEnable();
            LogUtils.e("DvrEnableObserver dvrEnable=" + dvrEnable);
            if (!dvrEnable) {
                REBOOT_SYSTEM = true;
                onDestroy();

                Utils.killProcess(getContext(), "com.wwc2.dvr");
            }
        }
    }

    private final class CameraObserver extends ContentObserver {
        public CameraObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

            LogUtils.d("cameraObserver", "selfChange=" + selfChange + ", uri=" + uri);
            if (!getAccState()) {//ACC OFF状态不处理。
                return;
            }
            checkReversing();
        }
    }

    private final class TurnLightObserver extends ContentObserver {
        public TurnLightObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            LogUtils.d("TurnLightObserver", "selfChange=" + selfChange + ", uri=" + uri);
            if (!getAccState()) {//ACC OFF状态不处理。
                return;
            }
            checkTurnLight();
        }
    }

    public boolean getCameraStatus() {
        boolean ret = false;
        String camera = getContentResolver().getType(uri_camera);
        if (camera != null) {
            ret = camera.equals("true") ? true : false;
        }
//        LogUtils.d("getCameraStatus----isReversing=" + ret);
        return ret;
    }

    public int getTurnLight() {
        int ret = 0;
        String light = getContentResolver().getType(uri_turnlight);
        if (light != null) {
            try {
                ret = Integer.parseInt(light);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        LogUtils.d("getTurnLight----light=" + ret);
        return ret;
    }

    private final class CameraTypeObserver extends ContentObserver {
        public CameraTypeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

            LogUtils.d("CameraTypeObserver", "selfChange=" + selfChange + ", uri=" + uri);
            String curCameraType = SPUtils.getCameraTypeFromMian(mContext);
            RecordData.getInstance().cameraType.setValue(curCameraType);
            SPUtils.setCameraType(getContext(), curCameraType);
            mAppBaseUI.getFourCameraManager().setCamera360Type(curCameraType);

            if (mCameraThread != null) {
                mCameraThread.rebootCamera();
            }
        }
    }

    private final class ReverseMirrorObserver extends ContentObserver {
        public ReverseMirrorObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

            LogUtils.d("ReverseMirrorObserver", "selfChange=" + selfChange + ", uri=" + uri);
            boolean reverseMirror = SPUtils.getReverseImage(mContext);
            RecordData.getInstance().mainMirror.setValue(reverseMirror ? 1 : 0);
//            SPUtils.setCameraType(getContext(), curCameraType);
            if (mAppBaseUI != null) {
                mAppBaseUI.getFourCameraManager().setCameraMirror(Config.WWC2_CH1_FLIP, reverseMirror ? 1 : 0);
            }
        }
    }

    private BroadcastReceiver mDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String path = intent.getData().getPath() + "/";
            int type = StorageDevice.parseFileOrDirName(context,path);//FileUtils.getDeviceId(path);
            int location = RecordData.getInstance().mutableLocation;
            LogUtils.d("DeviceReceiver---action=" + action + ", ID=" + type + ", location=" + location
                    + ", path=" + path);
            LogUtils.d("DeviceReceiver---getAccState=" +  getAccState());
//            if (!getAccState()){
//                LogUtils.d("<------mDeviceReceiver acc off  不做处理---->");
//                return;
//            }

            if (type != -1) {
                if (Intent.ACTION_MEDIA_EJECT.equals(action) ||
                        Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
                    // 拔出
                    if (type == location) {
                        stopRecording();
                        if (recordFileDataBase != null) {
                            recordFileDataBase.onDestroy();
                        }
                    }
                    if (recordFileManager != null) {
                        recordFileManager.setUsbMounted(type, false);
                    }
                } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                    // 已挂载
                    if (location == type) {
                        recordFileDataBase.onCreate(context);
                        if (getAccState()) {//ACC OFF状态不自动录制
                            boolean recordValue = RecordData.getInstance().autoRecord.getValue();
                            if (recordValue) {
                                startRecording();
                            }
                        }
                    }
                    LogUtils.e("ACTION_MEDIA_MOUNTED---recordFileManager=" + recordFileManager);
                    if (recordFileManager != null && getAccState()) {//ACC ON状态才作拷贝
                        recordFileManager.setUsbMounted(type, true);
                    }
                } else if (Intent.ACTION_MEDIA_CHECKING.equals(action)) {
                    // 正在检测 暂不作处理
                }
            }
        }
    };

    private class RecordReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CAMERA.equals(action)) {
                //处理监听
            } else if (PANORAMIC.equals(action)) {
                final boolean panoramic = intent.getBooleanExtra("panoramic", false);
                LogUtils.d(TAG, " 0 PANORAMIC_CLOSE panoramic = " + panoramic);
            } else if (CURRENT_PACKAGENAME.equals(action)) {
                Bundle bundle = intent.getExtras();
                if (null != bundle) {
                    String packetName = bundle.getString("packagename");
                    String className = bundle.getString("classname");
                    LogUtils.d(TAG, "top package name = " + packetName + ", className = " + className);
                    if (packetName.equals("com.wwc2.video")){
                        if (mVideoDialog!=null){
                            mVideoDialog.dismiss();
                        }
                        if (mImgDialog!=null){
                            mImgDialog.dismiss();
                        }
                    }

                    if (packetName.equals(getPackageName())) {
                        return;
                    }
                    LogUtils.d(TAG, "Activity isForeGround=" + isForeGround());

                    //倒车中，不会触发切换动作，无需检测倒车
                    if (isForeGround()) {
                        //检测倒车状态
                        if (!getCameraStatus() && getTurnLight() == 0) {
                            toBackgroundWindow();
                        } else {
                            LogUtils.d(TAG, "Activity CURRENT 正在倒车中，不可退出！");
                        }
                    }
                }
            } else if (action.equals(Config.ACTION_REBOOT_SYSTEM)) {
                LogUtils.d(TAG, " REBOOT_SYSTEM close camera!");
                REBOOT_SYSTEM = true;
                onDestroy();
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equalsIgnoreCase(reason) ||
                        SYSTEM_DIALOG_REASON_BACK_KEY.equalsIgnoreCase(reason)) {
                    //在所有应用界面按home键返回到首页
                    LogUtils.e("home key pressed...isForeGround=" + isForeGround() + ", reason=" + reason);

                    if (SYSTEM_DIALOG_REASON_BACK_KEY.equalsIgnoreCase(reason)) {
                        if (mImgDialog != null && mImgDialog.isImgShowing()) {
                            mImgDialog.dismiss();
                            return;//避免直接退到主页。
                        }
                        if (mVideoDialog != null && mVideoDialog.isVideoShowing()) {
                            mVideoDialog.dismiss();
                            return;//避免直接退到主页。
                        }
                        if (mRecordFilePopWindowTest != null && mRecordFilePopWindowTest.isFileShowing()) {
                            mRecordFilePopWindowTest.dismiss();
                            return;//避免直接退到主页。
                        }
                        if (settingPopWindow != null && settingPopWindow.isSettingsShowing()) {
                            settingPopWindow.dismiss();
                            return;//避免直接退到主页。
                        }
                    }
                    if (isForeGround()) {
                        toBackgroundWindow();
                    }
                }
            } else if (Config.ACTION_CRASH.equals(action)) {
//                if (mCameraThread != null) {
//                    File outputFile = FileUtils.getCaptureFile(ConstantsData.PICTURE_DIR, SPUtils.getLocation(getContext())/*location*/);
//
//                    if (outputFile == null) {
//                        mainHandler.sendMessageDelayed(mainHandler.obtainMessage(MSG_TAKE_PICTURE), 500);
//                    }
//                    mCameraThread.captureStill(outputFile.getPath(), Config.CHANNEL_BACK);
//
//                    mainHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            mCameraThread.captureStill(outputFile.getPath(), Config.CHANNEL_FRONT);
//                        }
//                    }, 4000);
//                    setLockStatus(true, false);
//                }
            } else if (action.equals(Config.ACTION_EXIT_DVR)) {
                if (isForeGround()) {
                    LogUtils.d(TAG, "Main: toBackgroundWindow");
                    toBackgroundWindow();
                }
            } else if (action.equals(Config.ACTION_YY_AI)) {
                int value = intent.getIntExtra(Config.ACTION_YY_AI_KEY, 0);
                switchCameraView(value);
            } else if (action.equals(Config.TAKEPHOTO_V9)) {
                String curRecordType = RecordData.getInstance().recordType.getValue();
                if (Config.TYPE_TWO_STREAM.equals(curRecordType) ||
                        Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
                    takePicture(false, Config.TWO_CAPTURE, false);
                } else if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
                    takePicture(false, Config.FRONT_CAPTURE, false);
                } else {
                    takePicture(false, Config.FOUR_CAPTURE, false);
                }
            } else if (action.equals(Config.ACTION_DVR_VIDEO)) {
                boolean status = intent.getBooleanExtra(Config.KEY_DVR_VIDEO, false);
                if (status) {
                    if (mAppBaseUI != null && mAppBaseUI.getFourCameraManager().isRecorded()) {
                        mRecordBefore = true;
                        stopRecording();
                    }
                } else {
                    if (mRecordBefore) {
                        mRecordBefore = false;
                        startRecording();
                    }
                }
            }
        }
    }

    private void checkReversing(){
        //检测倒车状态
        if (getCameraStatus()) {
            switchReversing();
            setTrackView();
        } else {
            switchDVR();

            int light = getTurnLight();
            if (light == 1 || light == 2) {
                switchTurnLight(light);
            }
        }
    }

    private void switchReversing() {
        LogUtils.e(TAG, "...switchReversing...尝试检测");

        boolean open = mCameraThread.isCameraBackOpened();
        if (!open){
            LogUtils.e(TAG, "...switchReversing1...开始切换为倒车，打开摄像头并预览" );
            mCameraThread.openCamera(true);
        }

        if (null != settingPopWindow) settingPopWindow.dismiss();
        if (null != mRecordFilePopWindowTest) mRecordFilePopWindowTest.dismiss();
        if (null != mVideoDialog) mVideoDialog.dismiss();
        if (null != mImgDialog) mImgDialog.dismiss();
        binding.btnBottomShow.setVisibility(View.GONE);
        if (null != deleteDialog) deleteDialog.onDismiss();

        if (!isForeGround()) {
            isShowUI = isForeGround();//修改快速打倒车时会出现退出倒车后返回到DVR界面。2019-11-12
            toForegroundWindow();
        } else {
            if (getTurnLight() != 0) {
                isShowUI = isTurnShowUI;
            }
        }
        LogUtils.e(TAG, "...switchReversing...isShowUI=" + isShowUI + ", isTurnShowUI=" + isTurnShowUI + ", fore=" + isForeGround());//false

        mAppBaseUI.getFourCameraManager().SettingBackCameraView();
        showBottom(false);
    }

    private void checkTurnLight() {
        //检测转向灯状态
        int light = getTurnLight();
        if (light == 1 || light == 2) {
            switchTurnLight(light);
        } else {
            if (!getCameraStatus()) {
                if (!isTurnShowUI) {
                    toBackgroundWindow();
                } else {
                    DvrApplication.hideSystemUI(floatView);
                    mAppBaseUI.getFourCameraManager().SettingAllCameraView();
                }
            }
            isTurnShowUI = true;
        }
    }

    private void switchTurnLight(int light) {
        LogUtils.e(TAG, "...switchTurnLight...尝试检测");
        if (getCameraStatus()) {
            return;
        }

        boolean open = mCameraThread.isCameraBackOpened();
        if (!open){
            LogUtils.e(TAG, "...switchTurnLight...开始切换为倒车，打开摄像头并预览" );
            mCameraThread.openCamera(true);
        }

        if (null != settingPopWindow) settingPopWindow.dismiss();
        if (null != mRecordFilePopWindowTest) mRecordFilePopWindowTest.dismiss();
        if (null != mVideoDialog) mVideoDialog.dismiss();
        if (null != mImgDialog) mImgDialog.dismiss();

        if (!isForeGround()) {
            isTurnShowUI = isForeGround();//修改快速打倒车时会出现退出倒车后返回到DVR界面。2019-11-12
            toForegroundWindow();
        }
        if (light == 1) {
            mAppBaseUI.getFourCameraManager().SettingLeftCameraView();
        } else {
            mAppBaseUI.getFourCameraManager().SettingRightCameraView();
        }
    }

    /**
     * 开始自动检测-确定
     * @param view
     */
    @Override
    public  void saveOkClick(View view){

        //节点锁已释放..
        SYNC_CHECK_CAMERA_NODE = false;
        LogUtils.e("saveOkClick......确定保存!...释放节点锁.");
        mainHandler.sendEmptyMessage(Config.MSG_WRITE_TIMER);
    }

    @Override
    public void saveNoClick(View view){
        LogUtils.e("saveNoClick......取消，不保存!...释放节点锁.");

        //节点锁已释放..
        SYNC_CHECK_CAMERA_NODE = false;
        binding.layoutCheckCamera.setVisibility(View.GONE);

        //开启loading
        syncLoading(true, getContext().getString(R.string.str_setuping));

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraThread.rebootCamera();
            }
        });
    }

    @Override
    public void onClickShowBottom(View view) {
        //此处不处理，快速点击会无效
        if (binding != null && binding.viewBottom != null) {
            LogUtils.e("onClickShowBottom---isShown=" + binding.viewBottom.isShown() +
                    ", visible=" + binding.viewBottom.getVisibility());
            if (getCameraStatus()) {
                showBottom(false);
            } else {
                showBottom(!binding.viewBottom.isShown());
            }
        }
    }

    public void showBottom(boolean show) {
        if (binding.viewBottom != null) {
            binding.viewBottom.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                LogUtils.d(TAG, "onClick viewBottom VISIBLE" + binding.viewBottom);
                DvrApplication.showSystemUI(floatView);
                startHideBarTime();
            } else {
                LogUtils.d(TAG, "onClick viewBottom GONE");
                DvrApplication.hideSystemUI(floatView);
                stopHideBarTime();
            }

            if (getCameraStatus()) {
                binding.viewBottom.setVisibility(View.GONE);
                DvrApplication.hideSystemUI(floatView);
                stopHideBarTime();

                binding.ivRecordStatusShow.setVisibility(View.INVISIBLE);
            } else {
                boolean record = RecordData.getInstance().recordState.getValue();
                binding.ivRecordStatusShow.setVisibility(record ? View.VISIBLE : View.INVISIBLE);
                mainHandler.removeMessages(Config.MSG_SHOW_RECORD_STATUS);
                LogUtils.e(TAG, "-------showBottom---record status=" + record);
                if (record) {
                    mainHandler.sendEmptyMessageDelayed(Config.MSG_SHOW_RECORD_STATUS, Config.DELAY_TIME / 8);
                }
            }
        }

        if (binding.btnBottomBack != null) {
            if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
                binding.btnBottomBack.setVisibility(show ? View.VISIBLE : View.GONE);
            } else {
                binding.btnBottomBack.setVisibility(View.GONE);
            }
        }
    }

    private boolean isSensor = false;
    public String postSensor(boolean isOff, boolean syncStatus){
        LogUtils.e("postSensor...isOff=" + isOff + ",,syncStatus=" + syncStatus);
        if (mCameraThread == null) {
            LogUtils.e("postSensor...mCameraThread == null");
            return "";
        }

        if (syncStatus) {
            //拍照
            //由CarNetwork直接调用takePicture
//            String path = takePicture(isOff, 0);
            //上锁
            //同步状态
            isSensor = true;

            boolean record = RecordData.getInstance().recordState.getValue();
            if (!record && recordFileManager != null) {
                recordFileManager.setCurRecordFileName(-1, "");
            }

            setLockStatus(true, false);//里面有判断是否在录制中
        } else {
            //同步状态
            if (getAccState()) {//ACC ON状态碰撞后需要还原加锁状态
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //还原加锁状态
                        RecordData.getInstance().lockState = SPUtils.getLockStatus(getContext());
                    }
                });
            }
//            setLockStatus(false);
        }
        mCameraThread.setSensor(syncStatus);
        return "";
    }

    public void updateSensor(){
        String serial = getContentResolver().getType(caruri);
        LogUtils.d(TAG, "updateSensor...serial=" + serial);
        if(!TextUtils.isEmpty(serial)){
            int sensor = 0;
            try {
                sensor = Integer.parseInt(serial);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            switch (sensor){
                case 0:
                    sensor = Config.SENSOR_CLOSE;
                    break;
                case 1:
                    sensor = Config.SENSOR_KEY_1;
                    break;
                case 2:
                    sensor = Config.SENSOR_KEY_2;
                    break;
                case 3:
                    sensor = Config.SENSOR_KEY_3;
                    break;
            }
            SPUtils.setSensor(getContext(), sensor);
            RecordData.getInstance().sensor.postValue(sensor);
            if (mAppBaseUI != null) {
                mAppBaseUI.getFourCameraManager().setSensorType(sensor);
            }
        }
    }

    public int getTakeCameraStatus(){
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_DUAL_STREAM.equals(curRecordType) || Config.TYPE_TWO_STREAM.equals(curRecordType)) {
            return 2;
        } else if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            return 1;
        }
        return Config.CAMERA_NUM;
    }

    public interface SerListener {
        void onBackground();
    }

    SerListener activitylistener;
    public void setSerListener(SerListener listener){
        if(listener != null){
            activitylistener = listener;
        }
    }

    public static boolean getCurAccStatus() {
        return mCurACCStatus;
    }

    public void rebootCamera(){
        mCameraThread.rebootCamera();
    }

    public CameraHandlerThread getCameraHandlerThread(){
        return mCameraThread;
    }

    public void setH264StreamMode(int mode){
        mAppBaseUI.getFourCameraManager().setH264StreamMode(mode);
    }

    /**
     * 远程删除文件
     * @param path
     * @param callBack
     */
    public void deleteFileFromNetwork(String path, DeleteVideoCallBack callBack){
        DeleteThread tmpThread = new DeleteThread("deletFile-thread", callBack, path);
        tmpThread.start();
    }

    class DeleteThread extends Thread {
        DeleteVideoCallBack mDeleteVideoCallBack;
        String path ;

        public DeleteThread(@NonNull String name, DeleteVideoCallBack mDeleteVideoCallBack, String path) {
            super(name);
            this.mDeleteVideoCallBack = mDeleteVideoCallBack;
            this.path = path;
        }

        @Override
        public void run() {
            super.run();
            boolean reslut = true;
            List<DeleteVideoBean> videoBeans = ResultPaser.paserCollection(path, new TypeToken<List<DeleteVideoBean>>() {
            }.getType());
            for (DeleteVideoBean bean: videoBeans){
                LogUtils.d("deleteFile beanName--->" + bean.getName());
                if (!FileUtils.deleteFile(bean.getName())) {
                    reslut = false;
                } else {
                    //由文件监听去删除数据库
                }
            }

            try {
                mDeleteVideoCallBack.deleteAction(path,reslut);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}

