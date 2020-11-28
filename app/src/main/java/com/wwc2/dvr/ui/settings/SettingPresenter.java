package com.wwc2.dvr.ui.settings;

import android.view.View;

import com.wwc2.BasePresenter;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.R;
import com.wwc2.dvr.binding.command.BindingAction;
import com.wwc2.dvr.binding.command.BindingCommand;
import com.wwc2.dvr.binding.command.BindingConsumer;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.utils.SPUtils;

import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by huwei on 19-3-18.
 */
public class SettingPresenter extends BasePresenter<SettingContract.V> implements SettingContract.P {

    //显示时间水印配置文件
    public static final String WATERMARK_ENABLE_NODE = "/sys/class/gpiodrv/gpio_ctrl/watermark_enable";

    public BindingCommand<String> onSaveTimeChange = new BindingCommand<String>(new BindingConsumer<String>() {
        int time = 1;//60 * 1000;
        @Override
        public void call(View v, String s) {
            if (!v.isPressed()) {
                return;
            }
            switch (s) {
                case "1min":
                    time = 1;//60 * 1000;
                    break;
                case "3min":
                    time = 3;//2 * 60 * 1000;
                    break;
                case "5min":
                    time = 5;//5 * 60 * 1000;
                    break;
            }
            mView.setAutoSaveTime(time, true);
        }
    });

    public BindingCommand<String> onSplitScreenChange = new BindingCommand<String>(new BindingConsumer<String>() {
        int type = 0;
        @Override
        public void call(View v, String s) {
            if (!v.isPressed()) {
                return;
            }
            if (s.equals(mContext.getString(R.string.full_screen))){
                type = Config.FULL_SCREEN;
            }else if (s.equals(mContext.getString(R.string.split_screen))){
                type = Config.SPLIT_SCREEN;
            }else {
                type = Config.SPLIT_SCREEN_UP_DOWN;
            }
            mView.setSplitScreen(type, false);
        }

    });

    public BindingCommand<String> onSaveLocationChange = new BindingCommand<String>(new BindingConsumer<String>() {
        int location = StorageDevice.NAND_FLASH;
        @Override
        public void call(View v, String locationStr) {
            if (!v.isPressed()) {
                return;
            }
            if (locationStr.equals(mContext.getString(R.string.sd))){
                location = StorageDevice.MEDIA_CARD;
            }else if (locationStr.equals(mContext.getString(R.string.local))){
                location = StorageDevice.NAND_FLASH;
            }else if (locationStr.equals(mContext.getString(R.string.usb))){
                location = StorageDevice.USB;
            }else if (locationStr.equals(mContext.getString(R.string.usb1))){
                location = StorageDevice.USB1;
            }

            mView.setLocation(location, true);
        }
    });

    public BindingCommand<View> onClickLayoutDvrVideo = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            mView.setDvrVideo();
        }
    });

    public BindingCommand<View> onClickLayoutRadioGroup = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            mView.onClickRadioGroup(v);
        }
    });

    public BindingCommand<View> onClickVideoRL = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            mView.onClickVideoRL(v);
        }
    });
    public BindingCommand<View> onClicksplistRL = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            mView.setClicksplistRL(v);
        }
    });

    public BindingCommand<View> onClickSensor = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            mView.onClickRadioGroup(v);
        }
    });

    public BindingCommand<String> onClickSensorSave = new BindingCommand<String>(new BindingConsumer<String>() {

        int sensor = Config.SENSOR_CLOSE;
        @Override
        public void call(View v, String parameter) {
            if (!v.isPressed()) {
                return;
            }
            switch (parameter) {
                case "关闭":
                    sensor = Config.SENSOR_CLOSE;
                    break;
                case "轻微":
                    sensor = Config.SENSOR_KEY_1;
                    break;
                case "中等":
                    sensor = Config.SENSOR_KEY_2;
                    break;
                case "严重":
                    sensor = Config.SENSOR_KEY_3;
                    break;
            }
            RecordData.getInstance().sensor.postValue(sensor);
            SPUtils.setSensor(mContext, sensor);
            LogUtils.e("...222...test...val=" + sensor);
            mView.setSensor(sensor, false);
        }
    });


    public BindingCommand<View> onClickLayouAutoRecord = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            boolean newVal = !RecordData.getInstance().autoRecord.getValue();
            RecordData.getInstance().autoRecord.setValue(newVal);
            SPUtils.setAutoRecord(mContext, newVal);

            mView.setAutoRecord(newVal);
        }
    });
    public BindingCommand<View> onClickLayoutMuteRecord = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            boolean newVal = !RecordData.getInstance().muteRecord.getValue();
            mView.setMuteRecord(newVal, true);

        }
    });

    public BindingCommand<View> onClickAutoCheck = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            mView.setAutoCheck(true);
        /*    boolean newVal = !RecordData.getInstance().autoCheck.getValue();
            RecordData.getInstance().autoCheck.setValue(newVal);
            SPUtils.putBoolean(mContext,Config.AUTOCHECK, newVal);
            mView.setAutoCheck(newVal);*/

        }
    });

    public BindingCommand<View> onClickLayoutWatermark = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            boolean newVal = !RecordData.getInstance().showTimeWaterMark.getValue();
            RecordData.getInstance().showTimeWaterMark.setValue(newVal);
            SPUtils.setTimeWaterMark(mContext, newVal);
            mView.setWatermark(newVal, true);
        }
    });

    public BindingCommand<View> onClickLayoutChannelWaterMark = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            boolean newVal = !RecordData.getInstance().channelWaterMark.getValue();
            RecordData.getInstance().channelWaterMark.setValue(newVal);
            SPUtils.setChannelWaterMark(mContext, newVal);
            mView.setChannelWatermark(newVal, true);
        }
    });


    public BindingCommand<View> onClickLayoutGPSWaterMark = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            boolean newVal = !RecordData.getInstance().gpsWaterMark.getValue();
            RecordData.getInstance().gpsWaterMark.setValue(newVal);
            SPUtils.setGPSWaterMark(mContext, newVal);
            mView.setGPSWatermark(newVal, true);
        }
    });

    public BindingCommand<View> onClickLayoutCarNumberWaterMark = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            boolean newVal = !RecordData.getInstance().carWaterMark.getValue();
            RecordData.getInstance().carWaterMark.setValue(newVal);
            SPUtils.setCarWaterMark(mContext, newVal);
            mView.setCarWatermark(newVal, true);
        }
    });

    public BindingCommand<View> onClickOpenFactory = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            mView.openFactory();
        }
    });

    public BindingCommand<View> onClickCarNumberSet = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
           mView.carNumberSet();
        }
    });

    public BindingCommand<View> onClickLayoutRecordTypeMain = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            mView.setRecordTypeOfCamera(ConstantsData.TYPE_BACK);
        }
    });

    public BindingCommand<View> onClickLayoutRecordTypeSub = new BindingCommand<>(new BindingAction() {
        @Override
        public void call(View v) {
            mView.setRecordTypeOfCamera(ConstantsData.TYPE_FRONT);
        }
    });

    public BindingCommand<String> onSaveCameraMirrorChange = new BindingCommand<String>(new BindingConsumer<String>() {
        int mirror = Config.CAMERA_MIRROR_NOR;
        @Override
        public void call(View v, String mirrorStr) {
            if (!v.isPressed()) {
                return;
            }
            if (mirrorStr.equals(mContext.getString(R.string.left_right_mirror))){
                mirror = Config.CAMERA_MIRROR_L_R;
            }else if (mirrorStr.equals(mContext.getString(R.string.up_down_mirror))){
                mirror = Config.CAMERA_MIRROR_U_D;
            }else if (mirrorStr.equals(mContext.getString(R.string.mirror))){
                mirror = Config.CAMERA_MIRROR_ALL;
            } else {
                mirror = Config.CAMERA_MIRROR_NOR;
            }

//            LogUtils.e("onSaveCameraMirrorChange-----id=" + v.getId() + ", sub=" + R.id.rg_sub_mirror + ", mirrorStr=" + mirrorStr);
//            switch (v.getId()) {
//                case R.id.rg_sub_mirror:
//                case R.id.rb_sub_mirror_normal:
//                case R.id.rb_sub_mirror_left_right:
//                case R.id.rb_sub_mirror_up_down:
//                case R.id.rb_sub_mirror_all:
//                    mView.setCameraMirror(Config.WWC2_CH0_FLIP, mirror, true);
//                    break;
//                case R.id.rg_main_mirror:
//                case R.id.rb_main_mirror_normal:
//                case R.id.rb_main_mirror_left_right:
//                case R.id.rb_main_mirror_up_down:
//                case R.id.rb_main_mirror_all:
//                    mView.setCameraMirror(Config.WWC2_CH1_FLIP, mirror, true);
//                    break;
//                case R.id.rg_left_mirror:
//                case R.id.rb_left_mirror_normal:
//                case R.id.rb_left_mirror_left_right:
//                case R.id.rb_left_mirror_up_down:
//                case R.id.rb_left_mirror_all:
//                    mView.setCameraMirror(Config.WWC2_CH2_FLIP, mirror, true);
//                    break;
//                case R.id.rg_right_mirror:
//                case R.id.rb_right_mirror_normal:
//                case R.id.rb_right_mirror_left_right:
//                case R.id.rb_right_mirror_up_down:
//                case R.id.rb_right_mirror_all:
//                    mView.setCameraMirror(Config.WWC2_CH3_FLIP, mirror, true);
//                    break;
//                default:
//                    break;
//            }
        }
    });

    @Override
    public void onCreate() {
        mView.setAutoSaveTime(SPUtils.getAutoSaveTime(mView.getContext()), false);
        mView.setAutoRecord(SPUtils.getAutoRecord(mView.getContext()));
        mView.setMuteRecord(SPUtils.getMuteRecord(mView.getContext()), false);
        mView.setWatermark(SPUtils.getTimeWaterMark(mView.getContext()), false);
        mView.setChannelWatermark(SPUtils.getChannelWaterMark(mView.getContext()), false);
        mView.setGPSWatermark(SPUtils.getGPSWaterMark(mView.getContext()), false);
        mView.setCarWatermark(SPUtils.getCarWaterMark(mView.getContext()), false);
        mView.setCarNumber(SPUtils.getCarNumber(mView.getContext()), false);
        mView.setLocation(SPUtils.getLocation(mView.getContext()), false);
        mView.setSplitScreen(SPUtils.getSplitScreen(mView.getContext()), true);
        LogUtils.d("Sensor oncreat:" + SPUtils.getSensor(mView.getContext()));
        mView.setSensor(SPUtils.getSensor(mView.getContext()), false);

        mView.setCameraMirror(Config.WWC2_CH1_FLIP, SPUtils.getM_Mirror(mView.getContext()), false);
        mView.setCameraMirror(Config.WWC2_CH0_FLIP, SPUtils.getS_Mirror(mView.getContext()), false);
        mView.setCameraMirror(Config.WWC2_CH2_FLIP, SPUtils.getLeft_Mirror(mView.getContext()), false);
        mView.setCameraMirror(Config.WWC2_CH3_FLIP, SPUtils.getRight_Mirror(mView.getContext()), false);

        mView.setRecordQuality(SPUtils.getRecordQuality(mView.getContext()), false);
        mView.setRecordType(SPUtils.getRecordType(mContext), false);

        if (Config.CAMTYPE_2FHD.equals(SPUtils.getCameraType(mContext))) {
            mView.setDvrCamera(1, false);
        } else {
            mView.setDvrCamera(0, false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
