package com.wwc2.dvr.data;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.dvr.fourCamera.FourCameraProxy;
import com.wwc2.dvr.utils.SPUtils;

/**
 * Created by huwei on 19-3-19.
 */
public class RecordData extends ViewModel {

    //存储位置
//    public MutableLiveData<Integer> mutableLocation = new MutableLiveData<>();
    public int mutableLocation = StorageDevice.USB;//MEDIA_CARD;

    //视频设置
    public MutableLiveData<Integer> videoSettings = new MutableLiveData<>();

    //录像分段时间
    public MutableLiveData<Integer> mutableSaveTime = new MutableLiveData<>();

    public MutableLiveData<Boolean> muteRecord = new MutableLiveData<>();
    public MutableLiveData<Boolean> autoRecord = new MutableLiveData<>();
    public MutableLiveData<Boolean> showTimeWaterMark = new MutableLiveData<>();
    public MutableLiveData<Boolean> channelWaterMark = new MutableLiveData<>();
    public MutableLiveData<Boolean> gpsWaterMark = new MutableLiveData<>();
    public MutableLiveData<Boolean> carWaterMark = new MutableLiveData<>();
    public MutableLiveData<String> carNumber = new MutableLiveData<>();

//    public MutableLiveData<Boolean> lockState = new MutableLiveData<>();
    public boolean lockState = false;
    public MutableLiveData<Boolean> recordState = new MutableLiveData<>();

    public MutableLiveData<Integer> mainMirror = new MutableLiveData<>();
    public MutableLiveData<Integer> subMirror = new MutableLiveData<>();
    public MutableLiveData<Integer> leftMirror = new MutableLiveData<>();
    public MutableLiveData<Integer> rightMirror = new MutableLiveData<>();

    //倒车参数
    public MutableLiveData<Integer> brightness = new MutableLiveData<>();
    public MutableLiveData<Integer> saturation = new MutableLiveData<>();
    public MutableLiveData<Integer> contrast = new MutableLiveData<>();

    public MutableLiveData<Integer> sensor = new MutableLiveData<>();

    //制式
    public MutableLiveData<String> cameraType = new MutableLiveData<>();

    //录制的格式
    public MutableLiveData<String> recordTypeMain = new MutableLiveData<>();
    public MutableLiveData<String> recordTypeSub = new MutableLiveData<>();

    //录像质量
    public MutableLiveData<Integer> recordQuality = new MutableLiveData<>();
    //录像类型
    public MutableLiveData<String> recordType = new MutableLiveData<>();

    public static RecordData getInstance(){
        return InstanceHolder.getInstance();
    }
    private static class InstanceHolder{
        private static RecordData instance = new RecordData();

        static RecordData getInstance() {
            return instance;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
    public void readDvrConfig(Context mContext) {

        mutableLocation = SPUtils.getLocation(mContext);
        //本地
        if (mutableLocation == StorageDevice.NAND_FLASH) {
            mutableLocation = StorageDevice.USB;//MEDIA_CARD;
            SPUtils.setLocation(mContext, mutableLocation);
        } else {
            FourCameraProxy.setDvrLocationToSystem(mContext, mutableLocation);
        }

        videoSettings.setValue(SPUtils.getVideoSettings(mContext));
        autoRecord.setValue(SPUtils.getAutoRecord(mContext));
        muteRecord.setValue(SPUtils.getMuteRecord(mContext));
        showTimeWaterMark.setValue(SPUtils.getTimeWaterMark(mContext));
        channelWaterMark.setValue(SPUtils.getChannelWaterMark(mContext));
        gpsWaterMark.setValue(SPUtils.getGPSWaterMark(mContext));
        carWaterMark.setValue(SPUtils.getCarWaterMark(mContext));
        carNumber.setValue(SPUtils.getCarNumber(mContext));
        mutableSaveTime.setValue(SPUtils.getAutoSaveTime(mContext));
//        lockState.setValue(SPUtils.getLockStatus(mContext));
        lockState = SPUtils.getLockStatus(mContext);
        recordState.setValue(false);
        mainMirror.setValue(SPUtils.getM_Mirror(mContext));
        subMirror.setValue(SPUtils.getS_Mirror(mContext));
        leftMirror.setValue(SPUtils.getLeft_Mirror(mContext));
        rightMirror.setValue(SPUtils.getRight_Mirror(mContext));

        brightness.setValue(SPUtils.getInt(mContext, ConstantsData.KEY_BRIGHTNESS, ConstantsData.INVALID_VALUE));
        saturation.setValue(SPUtils.getInt(mContext, ConstantsData.KEY_SATURATION, ConstantsData.INVALID_VALUE));
        contrast.setValue(SPUtils.getInt(mContext, ConstantsData.KEY_CONTRAST, ConstantsData.INVALID_VALUE));

        sensor.setValue(SPUtils.getSensor(mContext));

        recordType.setValue(SPUtils.getRecordType(mContext));
        recordQuality.setValue(SPUtils.getRecordQuality(mContext));

        cameraType.setValue(SPUtils.getCameraType(mContext));//必须在recordType后
    }
}
