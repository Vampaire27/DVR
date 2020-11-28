package com.wwc2.dvr.ui.record;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.SurfaceView;


import com.autonavi.amapauto.gdarcameraservicedemo.RawDataDispatch;
import com.wwc2.avin_interface.AvinDefine;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Result;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.DvrApplication;
import com.wwc2.dvr.R;
import com.wwc2.dvr.RecordService;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.ui.AppBaseUI;
import com.wwc2.dvr.utils.FileUtils;
import com.wwc2.dvr.utils.SPUtils;
import com.wwc2.dvr.utils.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class CameraHandlerThread extends HandlerThread {
    public static final int MSG_RECORD_STATUS = 8;
    private static final String TAG = CameraHandlerThread.class.getSimpleName();
    //**********************************************************************
    private static final int MSG_OPEN = 0;
    private static final int MSG_CLOSE = 1;
    private static final int MSG_PREVIEW_STOP = 3;
    private static final int MSG_CAPTURE_STILL = 4;
    private static final int MSG_CAPTURE_START = 5;
    private static final int MSG_CAPTURE_STOP = 6;
    //    private static final int MSG_AUTO_RECORD = 9;
    private static final int MSG_PREVIEW_START = 7;
    private static final int MSG_DELAY_OPEN = 8;
    private static final String TAG_THREAD = "CameraThread";
    /**
     * 锁
     */
    private static Lock mLock = new ReentrantLock();
    private final Object mSync = new Object();

    //后置
    private final SurfaceView backCView;
    /**
     * 上下文
     */
    protected Context mContext;
    /**
     * 倒车表面对象
     */
    private Handler mHandler;

    /**
     * for accessing camera
     */
    public Camera mCameraBack;

    private AppBaseUI mAppBaseUI;

    //完成拍照
    private static final int MSG_PHOTO_FINISH = 101;
    //拍照失败
    private static final int MSG_PHOTO_FAIL = 102;
    //拍照异常
    private static final int MSG_PHOTO_ERR = 103;
    //拍照失败，确保下次正常
    private static final int MST_PHOTO_FAIL_2 = 104;

    private Handler mainHandler;
    private boolean accStatus = false;
    private boolean mSurfaceViewCreated = false;
    private boolean hasPreViewed =false ;
    private boolean mSensor = false;

    /**
     * CAMERA是否打开
     */
    private Result mCameraOpen = new Result(AvinDefine.CameraOpen.INIT, new Result.ResultListener() {
        @Override
        public void onResult(int oldVal, int newVal) {
            switch (newVal){
                case AvinDefine.CameraOpen.OPEN_OVER:
                    previewCount = 0;
                    if(isSurfaceViewCreated()) {
                        startPreview();
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_PREVIEW_START, 200);
                    }
                    LogUtils.e(TAG, "...CameraOpen.OPEN_OVER...");
                    if (mRawDataDispatch != null) {
                        mRawDataDispatch.openCamera(true);
                    } else {
                        LogUtils.e(TAG, "...CameraOpen.OPEN_OVER. mRawDataDispatch = null..");
                    }
                    break;
                 case Config.PREVIEW_OVER:
                      doRecord();
                     break;
                 default:
                     break;
            }
        }
    });

    private void doRecord(){
        LogUtils.d(TAG, "...同步检测状态...");

        String camera = mContext.getContentResolver().getType(RecordService.uri_camera);
        boolean isReversing = camera.equals("true") ? true : false;
        if (isReversing) {
            LogUtils.d(TAG, "...启动检测倒车...切换...");
            mainHandler.sendMessage(mainHandler.obtainMessage(Config.CAMERA_REVERSING, true));
        } else {
            String light = mContext.getContentResolver().getType(RecordService.uri_turnlight);
            if ("1".equals(light) || "2".equals(light)) {
                mHandler.sendMessage(mainHandler.obtainMessage(Config.TURN_LIGHT_MODE, true));
            }

        }

        if (SPUtils.getAutoRecord(mContext) ) {
            if (accStatus || mSensor) {
                LogUtils.d(TAG, "...开启..录像...");
                mainHandler.sendMessage(mainHandler.obtainMessage(Config.START_RECORDING, true));
            } else {
                LogUtils.d(TAG, "...不开启..录像...ACC OFF");
            }
        }
    }

    public CameraHandlerThread(Context context, String name, SurfaceView b_View,
                               String platform) {
        super(name, Process.THREAD_PRIORITY_URGENT_DISPLAY);
        mContext = context;
        LogUtils.d(TAG, "Constructor:");
        this.backCView = b_View;
    }


    public void setCurAccStatus(boolean accStatus) {
        this.accStatus = accStatus;

        if (getIsBackPreView()) {
            doRecord();//解决在远程视频马上ACC ON不会自动开始录像
        }
    }

    public void setSensor(boolean sensor) {
        this.mSensor = sensor;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        synchronized (mSync) {
            if (mHandler == null) {
                mHandler = new CameraHandler(this);
            }
            if (accStatus) {
                openCamera(true);
            }
            mSync.notifyAll();
        }
    }

    //********************************************************************************
    int openTimes = 0;
    public void openCamera(boolean open) {
        if (open) openTimes = 0;

        String cameraOpen = FileUtils.readTextFile(Config.CAMERA_WORK_STATUS);
        LogUtils.d("openCamera------cameraOpen=" + cameraOpen);
        if ("0".equals(cameraOpen) || openTimes > 5) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_OPEN));
            openTimes = 0;
        } else {
            openTimes ++;
            mHandler.removeMessages(MSG_DELAY_OPEN);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_OPEN), 1000);
        }
    }

    public void openCameraDelay(int time) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_OPEN),time);
    }

    public void closeCamera() {
        LogUtils.d(TAG, "----start close camera----------isReboot=" );
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_CLOSE);
        }
    }

    public void startRecording() {
        mHandler.sendEmptyMessage(MSG_CAPTURE_START);
    }

    public void startRecordingDelay(int time) {
        mHandler.sendEmptyMessageDelayed(MSG_CAPTURE_START,time);
    }

    public void stopRecording() {
              mHandler.sendEmptyMessage(MSG_CAPTURE_STOP);
    }

    public void captureStill(final String path, final int channel) {
//        handleCaptureStill(path, channel);
    }

    public String captureStill(final int deviceId, final int channel) {
        return handleCaptureStill(deviceId, channel);
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        LogUtils.d(TAG, "####startPreview");

        try {
            Camera.Parameters params= mCameraBack.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            params.setPictureFormat(ImageFormat.JPEG);
            params.set("first-preview-frame-black", 1);
            params.setPreviewFormat(ImageFormat.YV12);
            String curCameraType = SPUtils.getCameraType(mContext);
            if (Config.CAMTYPE_2FHD.equals(curCameraType)) {
                params.setPreviewSize(Config.PREVIEW_FHD[0], Config.PREVIEW_FHD[1]);
            } else {
                params.setPreviewSize(Config.PREVIEW_HD[0], Config.PREVIEW_HD[1]);
            }
//            setFrameRate(params);
            mCameraBack.setParameters(params);
            mCameraBack.setPreviewDisplay(backCView.getHolder());
            mCameraBack.startPreview();
            mCameraOpen.setInt(Config.PREVIEW_OVER);
            setHasPreViewed(true);
            DvrApplication.setInitCamera(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setSurfaceCreate(boolean created) {
            mSurfaceViewCreated = created;
    }

    public boolean isSurfaceViewCreated(){
        return mSurfaceViewCreated;
    }

    public void setCameraPara(){
        if (mAppBaseUI != null) {
            mAppBaseUI.getFourCameraManager().setCamera360Type(SPUtils.getCameraType(mContext));

            mAppBaseUI.getFourCameraManager().setCarNumberToNode(SPUtils.getCarNumber(mContext));

            RecordData recordData = RecordData.getInstance();
            if (recordData != null) {
                int[] params = new int[18];

                String curRecordType = recordData.recordType.getValue();
                if (Config.TYPE_TWO_STREAM.equals(curRecordType) ||
                        Config.TYPE_DUAL_STREAM.equals(curRecordType)) {//二路
                    params[0] = Config.DUAL_DISPLAY;
                    if (Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
                        params[1] = Config.DUAL_RECORD;
                    } else {
                        params[1] = Config.TWO_RECORD;
                    }
                    params[2] = Config.TWO_CAPTURE;
                    params[3] = Config.DUAL_H264;
                } else if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
                    params[0] = Config.FRONT_DISPLAY;
                    params[1] = Config.FRONT_RECORD;
                    params[2] = Config.FRONT_CAPTURE;
                    params[3] = Config.FRONT_H264;
                } else {
                    params[0] = Config.QUART_DISPLAY;
                    if (Config.TYPE_QUART_STREAM.equals(curRecordType)) {
                        params[1] = Config.QUART_RECORD;
                    } else {
                        params[1] = Config.FOUR_RECORD;
                    }
                    params[2] = Config.FOUR_CAPTURE;
                    params[3] = Config.QUART_H264;
                }
                params[4] = recordData.channelWaterMark.getValue() ? Config.MODE_PARAM_OPEN : Config.MODE_PARAM_CLOSE;
                params[5] = recordData.showTimeWaterMark.getValue() ? Config.MODE_PARAM_OPEN : Config.MODE_PARAM_CLOSE;
                params[6] = recordData.gpsWaterMark.getValue() ? Config.MODE_PARAM_OPEN : Config.MODE_PARAM_CLOSE;
                params[7] = recordData.carWaterMark.getValue() ? Config.MODE_PARAM_OPEN : Config.MODE_PARAM_CLOSE;
                params[8] = recordData.muteRecord.getValue() ? Config.MODE_PARAM_OPEN : Config.MODE_PARAM_CLOSE;
                params[9] = recordData.mutableSaveTime.getValue();
                params[10] = recordData.subMirror.getValue();
                params[11] = recordData.mainMirror.getValue();
                params[12] = recordData.leftMirror.getValue();
                params[13] = recordData.rightMirror.getValue();
                params[14] = recordData.recordQuality.getValue();
                params[15] = recordData.mutableLocation;
                params[16] = recordData.mutableLocation;
                params[17] = 7;//通道顺序 (前后互换、左右互换）与硬件相关
                mAppBaseUI.getFourCameraManager().setCameraParam(params);
            }
        }
    }
    /**
     * 上锁的enter
     */
    protected void enter_lock() {
        mLock.lock();
        try {
            openCameraBack(true);
        } catch (Exception e) {
            LogUtils.d(TAG, "enter_lock.....e=" + e.toString());
        } finally {
            mLock.unlock();
        }
        //通知main
        mainHandler.sendMessage(mainHandler.obtainMessage(Config.MSG_SYNC_OK, true));
    }

    /**
     * 上锁的leave
     */
    protected void leave_lock() {

        LogUtils.d(" mLock.lock ....");
        mLock.lock();
        try {
            closeBackCamera();
        } catch (Exception e){

            LogUtils.d(TAG, "---close Camera----error------e=" + e.toString());


        }finally {
            mLock.unlock();
        }

    }

    public boolean isCameraBackOpened() {
        synchronized (mSync) {
            return mCameraBack != null;
        }
    }

    public Camera getCamera(){
        return mCameraBack;
    }

    private void handleOpen() {
        LogUtils.e("---------------handle_Open Camera!");
        synchronized (mSync) {
            enter_lock();
        }
    }
   //todo : zhongyang_todo
    public void handleClose() {
         handleStopRecording();
        synchronized (mSync) {
            leave_lock();
        }
    }


    private void handleStopPreview() {

        LogUtils.d(TAG_THREAD, "handleStopPreview:");

        if (mCameraBack != null) {
            mCameraBack.stopPreview();
        }

        synchronized (mSync) {
            mSync.notifyAll();
        }
    }

    /**
     * 拍照
     * @param path
     * @param channel 2=主摄像头，1=副摄像头
     */
    String fileCapture = null;
    private void handleCaptureStill(final String path, final int channel) {
        LogUtils.d("handleCaptureStill:  path=" + path + ", channel=" + channel);
//        mAppBaseUI.getFourCameraManager().startTakePhoto(path,channel);
//        cameraHandler.sendEmptyMessage(MSG_PHOTO_FINISH);
        if ("".equals(fileCapture)) {
            LogUtils.e("handleCaptureStill return last cmd not end!");
            return;
        }

        int location = RecordData.getInstance().mutableLocation;
        if (!accStatus) {
            for (int i = StorageDevice.NAND_FLASH; i < StorageDevice.USB3; i++) {
                String strDevice = StorageDevice.getPath(i);
                if (path.contains(strDevice)) {
                    location = i;
                    break;
                }
            }
        }

        fileCapture = "";
        mAppBaseUI.getFourCameraManager().startTakePhoto(location, channel);
        fileCapture = mAppBaseUI.getFourCameraManager().readCaptureFile();
        LogUtils.d("handleCaptureStill:  file=" + fileCapture);//
        if (!TextUtils.isEmpty(fileCapture)) {
            cameraHandler.sendEmptyMessage(MSG_PHOTO_FINISH);
        } else {
            cameraHandler.sendEmptyMessageDelayed(MST_PHOTO_FAIL_2, 1500);
        }
    }

    private String handleCaptureStill(final int deviceId, final int channel) {
        LogUtils.d("handleCaptureStill:  deviceId=" + deviceId + ", channel=" + channel);
        if ("".equals(fileCapture)) {
            LogUtils.e("handleCaptureStill return last cmd not end!");
            return "";
        }

        fileCapture = "";
        cameraHandler.sendEmptyMessageDelayed(MSG_PHOTO_FAIL, 2000);

        mAppBaseUI.getFourCameraManager().startTakePhoto(deviceId, channel);
        fileCapture = mAppBaseUI.getFourCameraManager().readCaptureFile();
        LogUtils.d("handleCaptureStill:  file=" + fileCapture);//
        if (!TextUtils.isEmpty(fileCapture) && !" ".equals(fileCapture)) {
            cameraHandler.removeMessages(MSG_PHOTO_FAIL);
            cameraHandler.sendEmptyMessage(MSG_PHOTO_FINISH);
        }
        return fileCapture;
    }

    /**
     * 录像
     */
    private void handleStartRecording() {

        LogUtils.d(TAG_THREAD, "handleStartRecording:  开始.启动录像流程..");
        //是否静音录像
        final boolean isMute = SPUtils.getMuteRecord(mContext);
        mAppBaseUI.getFourCameraManager().setRecordMute(isMute);

        //检测时长
        int saveTime = SPUtils.getAutoSaveTime(mContext);
        mAppBaseUI.getFourCameraManager().setRecordLatency(saveTime);

        //录像质量
        int type = SPUtils.getRecordQuality(mContext);
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_TWO_STREAM.equals(curRecordType) ||
                Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            String curCameraType = RecordData.getInstance().cameraType.getValue();
            if (Config.CAMTYPE_2FHD.equals(curCameraType)) {//双路FHD，录像码率应设置为HD的2倍
                if (type == Config.RECORD_BPS_5) {
                    type = Config.RECORD_BPS_8;//10M会导致底层出错，超高清设置成8M
                } else {
                    type = type * 2;
                }
            }
        }
        mAppBaseUI.getFourCameraManager().setRecordQuality(type);

        //设置保存路径
        if (accStatus) {//ACC OFF状态在开始录制时设置
            int deviceId = SPUtils.getLocation(mContext);
            mAppBaseUI.getFourCameraManager().setRecordLocation(deviceId);//设备不挂载上时，也通知底层，底层有作判断。
//            mAppBaseUI.getFourCameraManager().setPhotoLocation(deviceId);
        }

        mAppBaseUI.getFourCameraManager().startRecord();

        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_RECORD_STATUS, true));

        mainHandler.sendMessage(mainHandler.obtainMessage(Config.MSG_SYNC_OK, true));
    }

    private void handleStopRecording() {
        if(mAppBaseUI.getFourCameraManager().isRecorded()) {
            mAppBaseUI.getFourCameraManager().stopRecord();
            mainHandler.sendMessage(mainHandler.obtainMessage(MSG_RECORD_STATUS, false));
        }
    }

    private Handler cameraHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PHOTO_FINISH://拍照完成
                    ToastUtils.showShort(mContext.getResources().getString(R.string.photo_finish));
                    break;
                case MSG_PHOTO_FAIL://拍照失败
                    fileCapture = null;
                    ToastUtils.showShort(mContext.getResources().getString(R.string.photo_fail));
                    break;
                case MSG_PHOTO_ERR://拍照异常
                    ToastUtils.showShort(mContext.getResources().getString(R.string.photo_err));
                    break;
                case MST_PHOTO_FAIL_2:
                    fileCapture = null;
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 显示视频
     */
    public boolean openCameraBack(boolean isPreView) {
        //设置参数
        setCameraPara();

        try {
            mCameraBack = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

            if(isPreView){
                mCameraOpen.setInt(AvinDefine.CameraOpen.OPEN_OVER);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();

            rebootCamera();
        }

        return true;
    }

    public boolean getIsBackPreView(){
            return hasPreViewed;
    }

    public void setHasPreViewed(boolean value){
            hasPreViewed = value;
    }

    /**
     * 重启摄像头
     */

    public synchronized void rebootCamera(){
        LogUtils.e(TAG, "......rebootCamera...");
        closeCamera();
//        openCameraDelay(300);
        openCamera(true);
        //needReopen = true;
    }


    public void rebootVideo(int type){
        LogUtils.e(TAG, "......rebootVideo...type=" + type);
        if(mAppBaseUI.getFourCameraManager().isRecorded(type)) {
            mAppBaseUI.getFourCameraManager().stopRecord();
            mainHandler.sendMessage(mainHandler.obtainMessage(MSG_RECORD_STATUS, false));
            startRecordingDelay(2000);
        } else {
            mainHandler.sendMessageDelayed(mainHandler.obtainMessage(Config.MSG_SYNC_OK, true), 1000);
        }
    }

    public void setMainHandler(Handler handler) {
        mainHandler = handler;
    }

    /**
     * 检测信号..
     */
    public boolean checkCamera(){

        if(DvrApplication.isInitCamera()){
            boolean isOpen = isCameraBackOpened();
            boolean isPreView = getIsBackPreView();
            if(!isOpen && !isPreView){
                rebootCamera();
                return true;
            }
        }
        return false;
    }

    int previewCount = 0;
    private static class CameraHandler extends Handler {
        private final WeakReference<CameraHandlerThread> mWeakThread;

        public CameraHandler(CameraHandlerThread cameraHandlerThread) {
            super(cameraHandlerThread.getLooper());
            LogUtils.e("thread Looper:" + cameraHandlerThread.getLooper().hashCode());
            mWeakThread = new WeakReference<>(cameraHandlerThread);
        }

        @Override
        public void handleMessage(final Message msg) {
            final CameraHandlerThread thread = mWeakThread.get();
            if (thread == null) return;
            switch (msg.what) {
                case MSG_OPEN:
                    thread.handleOpen();
                    break;
                case MSG_CLOSE:
                    removeMessages(MSG_PREVIEW_START);
                    thread.handleClose();
                    break;
                case MSG_PREVIEW_STOP:
                    removeMessages(MSG_PREVIEW_START);
                    thread.handleStopPreview();
                    break;
                case MSG_CAPTURE_STILL:
                    thread.handleCaptureStill((String) msg.obj, msg.arg1);
                    break;
                case MSG_CAPTURE_START:
                    thread.handleStartRecording();
                    break;
                case MSG_CAPTURE_STOP:
                    thread.handleStopRecording();
                    break;
                case MSG_PREVIEW_START:
                    LogUtils.d("MSG_PREVIEW_START----sufaceCreate=" + thread.isSurfaceViewCreated());
                    if(thread.isSurfaceViewCreated()) {
                        thread.previewCount = 0;
                        thread.startPreview();
                    } else {
                        thread.previewCount ++;
                        sendEmptyMessageDelayed(MSG_PREVIEW_START, 400);
                        if (thread.previewCount > 15) {
                            removeMessages(MSG_PREVIEW_START);
                            thread.previewCount = 0;
                        }
                    }
                    break;
                case MSG_DELAY_OPEN:
                    thread.openCamera(false);
                    break;
                default:
//                throw new RuntimeException("unsupported message:what=" + msg.what);
            }
        }
    }


    public void closeBackCamera(){
        if (null != mCameraBack) {
            if (mRawDataDispatch != null) {
                mRawDataDispatch.openCamera(false);
            }
            if (getIsBackPreView()) {
                mCameraBack.stopPreview();
                hasPreViewed = false;
                LogUtils.d(TAG, "---close Camera----停止 后 camera预览----------");
            }

            mCameraBack.release();
            mCameraBack = null;
            LogUtils.d(TAG, "---close Camera----后camera---close over.------");

        }
    }

    private RawDataDispatch mRawDataDispatch = null;
    public void setRawDataDispatch(RawDataDispatch rawDataDispatch) {
        mRawDataDispatch = rawDataDispatch;
    }

    public void setAppBaseUI(AppBaseUI appbaseui){
        mAppBaseUI =appbaseui;
    }

    public AppBaseUI getAppBaseUI() {
        return mAppBaseUI;
    }

    public  int getPreviewWidth() {
        return 960;
    }

    public int getPreviewHeight() {
        return 540;
    }

}
