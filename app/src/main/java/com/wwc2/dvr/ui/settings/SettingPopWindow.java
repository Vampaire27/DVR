package com.wwc2.dvr.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.SUBRenderer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.usb.IFrameCallback;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.DvrApplication;
import com.wwc2.dvr.R;
import com.wwc2.dvr.data.CameraTypeData;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.databinding.LayoutSettingBinding;
import com.wwc2.dvr.ui.AppBaseUI;
import com.wwc2.dvr.ui.CameraSubWindow;
import com.wwc2.dvr.ui.CameraWindow;
import com.wwc2.dvr.ui.CarNumberWindow;
import com.wwc2.dvr.ui.FactoryWindow;
import com.wwc2.dvr.ui.RecordTypeWindow;
import com.wwc2.dvr.ui.VideoWindow;
import com.wwc2.dvr.utils.FileUtils;
import com.wwc2.dvr.utils.SPUtils;
import com.wwc2.dvr.utils.ToastUtils;
import com.wwc2.dvr.utils.Utils;
import com.wwc2.dvr.widget.CommonPopUpWindow;
import com.wwc2.dvr.widget.CustomPopWindow;

/**
 * Created by huwei on 19-3-19.
 */
public class SettingPopWindow implements SettingContract.V, PopupWindow.OnDismissListener {
    private Context context;
    private View contentView;
    private LayoutSettingBinding settingBinding;
    private SettingPresenter presenter;
    private CustomPopWindow popWindow;
    private CameraWindow cameraWindow1;
    private VideoWindow videoWindow;
    private FactoryWindow factoryWindow;
    private CarNumberWindow mCarNumberWindow;
    private OnSettingsDismissListener listener;
    private View parent;
    private TextView cameraValue;
    private int reboot = Config.CAMERA_REBOOT_NOT;
    private ScrollView scrollView;

    private AppBaseUI mAppBaseUI;

    //设置多选弹出框
    String[] strSaveTime = null;
    String[] strLocation = null;
    String[] strSensor = null;
    int[] SENSOR_VALUE = new int[]{Config.SENSOR_KEY_1, Config.SENSOR_KEY_2, Config.SENSOR_KEY_3, Config.SENSOR_CLOSE};
    String[] strMirror = null;
    String[] strRecordBPS = null;
    int[] RECORD_BPS_VALUE = new int[]{Config.RECORD_BPS_5, Config.RECORD_BPS_2, Config.RECORD_BPS_1};
    String[] strRecordTypeSD = null;
    String[] strRecordTypeUsb = null;
    String[] strRecordTypeTwo = null;
    String[] strRecordTypeAll = null;
    String[] strCameraType = null;
    private View popContentView;
    private CommonPopUpWindow popUpWindow = null;

    public SettingPopWindow(Context context, OnSettingsDismissListener settingsDismissListener, AppBaseUI appBaseUI) {
        this.context = context;
        this.listener = settingsDismissListener;
        this.mAppBaseUI = appBaseUI;
    }

    @Override
    public void openFactory() {
        if (factoryWindow == null) {
            factoryWindow = new FactoryWindow(context, factory);
        }
        factoryWindow.show(parent);
    }
    private FactoryWindow.OnFactoryDismissListener factory = new FactoryWindow.OnFactoryDismissListener() {
        @Override
        public void onFactoryDismiss(String password) {
            LogUtils.d("onFactoryDismiss password=" + password);

            //工厂设置
            settingBinding.rlCommonSettings.setVisibility(View.GONE);
            settingBinding.rlFactorySettings.setVisibility(View.VISIBLE);
            initFactoryLayout();

            scrollView.post(new Runnable() {
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    };

    @Override
    public void carNumberSet() {
        if (mCarNumberWindow == null) {
            mCarNumberWindow = new CarNumberWindow(context,mCarNumberCallback);
        }
        mCarNumberWindow.show(parent);
    }
    private CarNumberWindow.onDismiss mCarNumberCallback = new CarNumberWindow.onDismiss(){
        @Override
        public void onDismissCallBack(String number) {
            setCarNumber(number, true);
        }
    };

    @Override
    public void setAutoSaveTime(int autoSaveTime, boolean isReboot) {
        LogUtils.d("setAutoSaveTime :" + autoSaveTime);
        if (settingBinding != null) {
            String strTime = "";
            if (autoSaveTime == 1) {
                strTime = "1min";
            } else if (autoSaveTime == 3) {
                strTime = "3min";
            } else if (autoSaveTime == 5) {
                strTime = "5min";
            }
            settingBinding.tvTime.setText(strTime);

            if (isReboot) {
                RecordData.getInstance().mutableSaveTime.postValue(autoSaveTime);
                SPUtils.setAutoSaveTime(getContext(), autoSaveTime);
                if (mAppBaseUI != null) {
                    mAppBaseUI.getFourCameraManager().setRecordLatency(autoSaveTime);
                }

                if (reboot != Config.CAMERA_REBOOT_RECORD) {
                    LogUtils.d("setAutoSaveTime REBOOT_VIDEO!");
                    reboot = Config.CAMERA_REBOOT_VIDEO;
                }
            }
        }
    }

    /**
     * 主摄
     */
    @Override
    public void setDvrCamera(int type, boolean isReboot) {
//        if (cameraWindow1 == null) {
//            cameraWindow1 = new CameraWindow(getContext(), cameraListener);
//        }
//        cameraWindow1.show(parent);
        LogUtils.d("setDvrCamera :" + type);
        if (settingBinding != null && strCameraType != null) {
            if (type >= strCameraType.length) {
                type = 0;
            }
            settingBinding.dvrCameraValue.setText(strCameraType[type]);

            if (isReboot) {
                String curCameraType = Config.CAMTYPE_2HD;
                if (type == 1) {
                    curCameraType = Config.CAMTYPE_2FHD;
                }

                RecordData.getInstance().cameraType.setValue(curCameraType);
                SPUtils.setCameraType(getContext(), curCameraType);
                mAppBaseUI.getFourCameraManager().setCamera360Type(curCameraType);

                reboot = Config.CAMERA_REBOOT_PREVIEW;
            }
        }
    }

    private CameraWindow.OnCameraDismissListener cameraListener =
            new CameraWindow.OnCameraDismissListener() {
                @Override
                public void onCameraDismiss(CameraTypeData value) {
                    if (value != null) {
                        cameraValue.setText(value.getKey());
                        SPUtils.putString(getContext(), Config.CAMERA_KEY, value.getKey());
                    }
                }
            };

    @Override
    public void setDvrVideo() {
        LogUtils.d("CameraWindow setDvrVideo!");
        if (videoWindow == null) {
            videoWindow = new VideoWindow(getContext(), videoListener);
        }
        videoWindow.show(parent);
    }

    private VideoWindow.OnVideoDismissListener videoListener =
            new VideoWindow.OnVideoDismissListener() {
                @Override
                public void onVideoDismiss(CameraTypeData value) {
                    if (value != null) {
                        Utils.writeTextFile(value.getValue(), Config.VIDEO_SWITCH_NODE);
                        SPUtils.putString(getContext(), Config.VIDEO_KEY, value.getKey());
                        SPUtils.putString(getContext(), Config.VIDEO_VALUE, value.getValue());
                        reboot = Config.CAMERA_REBOOT_PREVIEW;
                    }
                }
            };

    @Override
    public void setAutoRecord(boolean isAutoRecord) {
        if (settingBinding != null) {
            settingBinding.tvAutoSwitch.setText(isAutoRecord ? R.string.on : R.string.off);
            settingBinding.autoRecordSwitch.setChecked(isAutoRecord);
        }
    }

    @Override
    public void setAutoCheck(boolean b) {
        listener.onAotuCheckListener();
    }

    @Override
    public void setMuteRecord(boolean isMuteRecord, boolean isReboot) {
        if (settingBinding != null) {
            settingBinding.tvMuteSwitch.setText(isMuteRecord ? R.string.on : R.string.off);
            settingBinding.muteRecordSwitch.setChecked(isMuteRecord);

            if (isReboot) {
                RecordData.getInstance().muteRecord.setValue(isMuteRecord);
                SPUtils.setMuteRecord(getContext(), isMuteRecord);
                mAppBaseUI.getFourCameraManager().setRecordMute(isMuteRecord);

                if (reboot != Config.CAMERA_REBOOT_RECORD) {
                    LogUtils.d("setMuteRecord REBOOT_VIDEO!");
                    reboot = Config.CAMERA_REBOOT_VIDEO;
                }
            }
        }
    }

    @Override
    public void setWatermark(boolean showWatermark, boolean isReboot) {
        if (settingBinding != null) {
            settingBinding.tvWatermarkSwitch.setText(showWatermark ? context.getString(R.string.on) : context.getString(R.string.off));
            settingBinding.watermarkSwitch.setChecked(showWatermark);
            if (isReboot) {
                LogUtils.d("setWatermark REBOOT_PREVIEW!");
//                reboot = Config.CAMERA_REBOOT_PREVIEW;//不需要重启
                if (mAppBaseUI != null) {
                    mAppBaseUI.getFourCameraManager().setTimeWaterMask(showWatermark);
                }
            }
        }
    }

 //zhongyang.hu add todo:
    @Override
    public void setChannelWatermark(boolean showChannelWaterMark, boolean isReboot) {
        if (settingBinding != null) {
            settingBinding.tvChannelSwitch.setText(showChannelWaterMark ? context.getString(R.string.on) : context.getString(R.string.off));
            settingBinding.channelSwitch.setChecked(showChannelWaterMark);
            if (isReboot) {
                LogUtils.d("setWatermark REBOOT_PREVIEW!");
//                reboot = Config.CAMERA_REBOOT_PREVIEW;//不需要重启
                if (mAppBaseUI != null) {
                    mAppBaseUI.getFourCameraManager().setChannelWaterMask(showChannelWaterMark);
                }
            }
        }
    }

    public SettingPopWindow() {
        super();
    }

    @Override
    public void setGPSWatermark(boolean showGPSWaterMark, boolean isReboot) {
        if (settingBinding != null) {
            settingBinding.tvGpsWMSwitch.setText(showGPSWaterMark ? context.getString(R.string.on) : context.getString(R.string.off));
            settingBinding.gpsWMSwitch.setChecked(showGPSWaterMark);
            if (isReboot) {
                LogUtils.d("setWatermark REBOOT_PREVIEW!");
//                reboot = Config.CAMERA_REBOOT_PREVIEW;//不需要重启
                if (mAppBaseUI != null) {
                    mAppBaseUI.getFourCameraManager().setGPSWaterMask(showGPSWaterMark);
                }
            }
        }
    }

    @Override
    public void setCarWatermark(boolean showCarWaterMark, boolean isReboot) {
        if (settingBinding != null) {
            settingBinding.tvCarNumberSwitch.setText(showCarWaterMark ? context.getString(R.string.on) : context.getString(R.string.off));
            settingBinding.carNumberSwitch.setChecked(showCarWaterMark);
            if (isReboot) {
                LogUtils.d("setWatermark REBOOT_PREVIEW!");
//                reboot = Config.CAMERA_REBOOT_PREVIEW;//不需要重启
                if (mAppBaseUI != null) {
                    mAppBaseUI.getFourCameraManager().setCarWaterMask(showCarWaterMark);
                }
            }
        }
    }

    @Override
    public void setCarNumber(String str, boolean isReboot) {
        LogUtils.d("setCarNumber----str=" + str + ", isReboot=" + isReboot);

        if (str != null && ((str.length() < 5 && str.length() > 0) || str.length() > 9)) {
            if (isReboot) {
                ToastUtils.showShort(context.getString(R.string.invalid_car_number));
            }
            return;
        }

        if (settingBinding != null) {
            settingBinding.tvCarNumber.setText(str);
        }

        if (isReboot) {
            RecordData.getInstance().carNumber.setValue(str);
            SPUtils.setCarNumber(context, str);
            if (mAppBaseUI != null) {
                mAppBaseUI.getFourCameraManager().setCarNumberToNode(str);
            }
        }
    }

    @Override
    public void setLocation(int location, boolean isReboot) {
        LogUtils.d("--------------SettingPopWindow-setLocation----------------------->" + location);
        if (isReboot) {
            int curLocation = RecordData.getInstance().mutableLocation;
            if (!FileUtils.isDiskMounted(context, StorageDevice.getPath(location))) {
                LogUtils.d("--------------SettingPopWindow-setLocation-------not exist---------------->" + location);
                ToastUtils.showShort(context.getString(R.string.nodevice));
                return;
            }
            if (curLocation == location) {
                LogUtils.e("setLocation----return same location=" + location);
                return;
            }
            RecordData.getInstance().mutableLocation = location;
            SPUtils.setLocation(getContext(), location);
            mAppBaseUI.getFourCameraManager().setRecordLocation(location);
            mAppBaseUI.getFourCameraManager().setPhotoLocation(location);

            if (listener != null) {
                listener.onLocationListener();
            }

            if (Config.TYPE_FOUR_STREAM.equals(RecordData.getInstance().recordType.getValue()) &&
                    location != StorageDevice.MEDIA_CARD && location != StorageDevice.NAND_FLASH) {
                setRecordType(Config.TYPE_QUART_STREAM, isReboot);
            }
        }
        String locationPositon = context.getString(R.string.sd);
        switch (location) {
            case StorageDevice.MEDIA_CARD:
                locationPositon = context.getString(R.string.sd);
                break;
            case StorageDevice.NAND_FLASH:
                locationPositon = context.getString(R.string.local);
                break;
            case StorageDevice.USB:
                locationPositon = context.getString(R.string.usb);
                break;
            case StorageDevice.USB1:
                locationPositon = context.getString(R.string.usb1);
                break;
        }

        if (settingBinding != null) {
            settingBinding.tvLocation.setText(locationPositon);

            if (isReboot && reboot != Config.CAMERA_REBOOT_RECORD) {
                LogUtils.d("setLocation REBOOT_VIDEO!");
                reboot = Config.CAMERA_REBOOT_VIDEO;

//                //设置保存路径
//                String path = StorageDevice.getPath(SPUtils.getLocation(getContext()));
//                path = path + ConstantsData.VIDEO_DIR;
//                if (!TextUtils.isEmpty(path)) {
//                    SystemProperties.set(Config.SAVE_DIRECTORY, path);
//                }
            }
        }
    }

    @Override
    public void onClickRadioGroup(View v) {
        settingBinding.splitScreenVgLayout.setVisibility(View.GONE);

        switch (v.getId()) {
            case R.id.layout_location:
                showPopMenu(strLocation, settingBinding.btnLocation, Config.KEY_LOCATION);
                break;
            case R.id.layout_videoQuality:
                showPopMenu(strRecordBPS, settingBinding.btnRecordQuality, Config.KEY_RECORD_QUALITY);
                break;
            case R.id.layout_saveTime:
                showPopMenu(strSaveTime, settingBinding.btnSaveTime, Config.KEY_SAVETIME);
                break;
            case R.id.layout_sensor:
                showPopMenu(strSensor, settingBinding.btnSensor, Config.KEY_SENSOR);
                break;
            case R.id.layout_main_camera_mirror:
                showPopMenu(strMirror, settingBinding.btnMainCameraMirror, Config.KEY_MAIN_MIRROR);
                break;
            case R.id.layout_sub_camera_mirror:
                showPopMenu(strMirror, settingBinding.btnSubCameraMirror, Config.KEY_SUB_MIRROR);
                break;
            case R.id.layout_left_camera_mirror:
                showPopMenu(strMirror, settingBinding.btnLeftCameraMirror, Config.KEY_LEFT_MIRROR);
                break;
            case R.id.layout_right_camera_mirror:
                showPopMenu(strMirror, settingBinding.btnRightCameraMirror, Config.KEY_RIGHT_MIRROR);
                break;
            case R.id.layout_recordType:
                if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
                    showPopMenu(strRecordTypeTwo, settingBinding.btnRecordType, Config.KEY_RECORD_TYPE);
                } else {
                    int location = RecordData.getInstance().mutableLocation;
                    if (location == StorageDevice.MEDIA_CARD || location == StorageDevice.NAND_FLASH) {
                        showPopMenu(strRecordTypeSD, settingBinding.btnRecordType, Config.KEY_RECORD_TYPE);
                    } else {
                        showPopMenu(strRecordTypeUsb, settingBinding.btnRecordType, Config.KEY_RECORD_TYPE);
                    }
                }
                break;
            case R.id.layout_dvr_camera:
                showPopMenu(strCameraType, settingBinding.btnDvrCameraValue, Config.KEY_CAMERA_TYPE);
                break;
        }
    }

    /**
     * 视频设置
     *
     * @param type 0:单录1 1:单录2 2;双录
     */
    @Override
    public void showVideoRadioGroup(int type) {
        LogUtils.d("<-----showVideoRadioGroup----->" + type);
        RecordData.getInstance().videoSettings.postValue(type);
        SPUtils.setVideoSettings(context, type);
        String title = "";

        settingBinding.tvVideoSettingss.setText(title);
//        reboot = Config.CAMERA_REBOOT_PREVIEW;
    }

    private void saveVideoSettings(String value, String key) {
        Utils.writeTextFile(value, Config.VIDEO_SWITCH_NODE);
        SPUtils.putString(getContext(), Config.VIDEO_KEY, key);
        SPUtils.putString(getContext(), Config.VIDEO_VALUE, value);
    }

    @Override
    public void onClickVideoRL(View v) {
//        rg_video_setting\ split_screen_vg_layout splitScreenLayout
        settingBinding.splitScreenVgLayout.setVisibility(View.GONE);

        if (settingBinding != null) {
            int type = RecordData.getInstance().videoSettings.getValue();
            String title = "";
            if (type == 0 || type == 1) {
                title = getContext().getResources().getString(R.string.str_video_settings_one);
            } else if (type == 2) {
                title = getContext().getResources().getString(R.string.str_video_settings_double);
            }
            LogUtils.d("<-----onClickVideoRL----->" + type);
            settingBinding.tvVideoSettingss.setText(title);
        }
    }

    @Override
    public void setClicksplistRL(View v) {
        int videoType = SPUtils.getVideoSettings(context); //0:单录1 1：单录2 2：双录
        if (videoType == 1) {
            ToastUtils.showShort("当前为单录，无法分屏");
            return;
        }

        if (settingBinding != null) {
            settingBinding.splitScreenVgLayout.setVisibility(View.VISIBLE);
            String str = settingBinding.splitScreenTv.getText().toString();
            if (str.equals(getContext().getString(R.string.full_screen))) {
                settingBinding.splitScreenVgLayout.check(R.id.full_rb);
            } else if (str.equals(getContext().getString(R.string.split_screen))) {
                settingBinding.splitScreenVgLayout.check(R.id.split_rb);
            } else {
                settingBinding.splitScreenVgLayout.check(R.id.split_rb_up_down);
            }
        }
    }

    @Override
    public void setSplitScreen(int type, boolean onlyUI) {
//        setSplitScreen split_screen_vg_layout
        if (settingBinding != null) {
            if (type == Config.FULL_SCREEN) {
                settingBinding.splitScreenVgLayout.check(R.id.full_rb);
                settingBinding.splitScreenTv.setText(getContext().getString(R.string.full_screen));
                if (!onlyUI) context.sendBroadcast(new Intent(Config.FULL_TYPE));
                SPUtils.putInt(context, Config.IS_SPLIT, Config.FULL_SCREEN);
            } else if (type == Config.SPLIT_SCREEN) {
                settingBinding.splitScreenVgLayout.check(R.id.split_rb);
                settingBinding.splitScreenTv.setText(getContext().getString(R.string.split_screen));
                if (!onlyUI) context.sendBroadcast(new Intent(Config.SPLIT_TYPE));
                SPUtils.putInt(context, Config.IS_SPLIT, Config.SPLIT_SCREEN);
            } else if (type == Config.SPLIT_SCREEN_UP_DOWN) {
                settingBinding.splitScreenVgLayout.check(R.id.split_rb_up_down);
                settingBinding.splitScreenTv.setText(getContext().getString(R.string.split_screen_up_down));
                if (!onlyUI) context.sendBroadcast(new Intent(Config.SPLIT_TYPE_UP_DOWN));
                SPUtils.putInt(context, Config.IS_SPLIT, Config.SPLIT_SCREEN_UP_DOWN);
            }
        }
    }

    @Override
    public void setSensor(int val, boolean reboot) {
        LogUtils.d("-------setSensor--------val=" + val);
        Intent intent = new Intent(Config.SENSOR_TYPE);
        int value;
        switch (val) {
            case Config.SENSOR_CLOSE:
                settingBinding.tvSensorValue.setText(context.getString(R.string.sensor_key0));
                value = 0;
                break;
            case Config.SENSOR_KEY_1:
                settingBinding.tvSensorValue.setText(context.getString(R.string.sensor_key1));
                value = 1;
                break;
            case Config.SENSOR_KEY_2:
                settingBinding.tvSensorValue.setText(context.getString(R.string.sensor_key2));
                value = 2;
                break;
            case Config.SENSOR_KEY_3:
                settingBinding.tvSensorValue.setText(context.getString(R.string.sensor_key3));
                value = 3;
                break;
            default:
                return;
        }
        intent.putExtra(Config.SENSOR_TYPE_VALUE, value);
        context.sendBroadcast(intent);

        //关闭=0，轻微=1，中等=2，严重=3
        if (reboot) {
            SPUtils.setSensor(getContext(), val);
            RecordData.getInstance().sensor.postValue(val);
            if (mAppBaseUI != null) {
                mAppBaseUI.getFourCameraManager().setSensorType(val);
            }
        }
    }

    @Override
    public void setCameraMirror(int channel, int mirror, boolean isReboot) {
        if (settingBinding != null) {
            LogUtils.d("setCameraMirror channel=" + channel + ", mirror=" + mirror + ", isReboot=" + isReboot);
            String strMirror = "";
            int id = -1;
            switch (mirror) {
                case Config.CAMERA_MIRROR_NOR:
                    strMirror = context.getString(R.string.normal_mirror);
                    break;
                case Config.CAMERA_MIRROR_L_R:
                    strMirror = context.getString(R.string.left_right_mirror);
                    break;
                case Config.CAMERA_MIRROR_U_D:
                    strMirror = context.getString(R.string.up_down_mirror);
                    break;
                case Config.CAMERA_MIRROR_ALL:
                    strMirror = context.getString(R.string.mirror);
                    break;
                default:
                    return;
            }

            switch (channel) {
                case Config.WWC2_CH0_FLIP:
                    if (isReboot) {
                        RecordData.getInstance().subMirror.setValue(mirror);
                        SPUtils.setS_Mirror(getContext(), mirror);
                    }
                    settingBinding.tvSubMirror.setText(strMirror);
                    break;
                case Config.WWC2_CH1_FLIP:
                    if (isReboot) {
                        RecordData.getInstance().mainMirror.setValue(mirror);
                        SPUtils.setM_Mirror(getContext(), mirror);
                    }
                    settingBinding.tvMainMirror.setText(strMirror);
                    break;
                case Config.WWC2_CH2_FLIP:
                    if (isReboot) {
                        RecordData.getInstance().leftMirror.setValue(mirror);
                        SPUtils.setLeft_Mirror(getContext(), mirror);
                    }
                    settingBinding.tvLeftMirror.setText(strMirror);
                    break;
                case Config.WWC2_CH3_FLIP:
                    if (isReboot) {
                        RecordData.getInstance().rightMirror.setValue(mirror);
                        SPUtils.setRight_Mirror(getContext(), mirror);
                    }
                    settingBinding.tvRightMirror.setText(strMirror);
                    break;
                default:
                    return;
            }

            if (isReboot) {
                LogUtils.d("setCameraMirror REBOOT_PREVIEW!");
//                reboot = Config.CAMERA_REBOOT_PREVIEW;//不需要重启
                if (mAppBaseUI != null) {
                    mAppBaseUI.getFourCameraManager().setCameraMirror(channel, mirror);
                }
            }
        }
    }

    @Override
    public void setRecordQuality(int type, boolean isReboot) {
        LogUtils.d("--------------SettingPopWindow-setRecordQuality----------------------->" + type);
        if (strRecordBPS == null) {
            LogUtils.e("--------------SettingPopWindow-setRecordQuality----------------------->null");
            return;
        }

        String str = strRecordBPS[0];
        switch (type) {
            case Config.RECORD_BPS_5:
                str = strRecordBPS[0];
                break;
            case Config.RECORD_BPS_2:
                str = strRecordBPS[1];
                break;
            case Config.RECORD_BPS_1:
                str = strRecordBPS[2];
                break;
            default:
                break;
        }

        if (settingBinding != null) {
            settingBinding.tvRecordQuality.setText(str);

            if (isReboot) {
                RecordData.getInstance().recordQuality.setValue(type);
                SPUtils.putInt(context, Config.KEY_RECORD_QUALITY, type);

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
                mAppBaseUI.getFourCameraManager().setRecordQuality(type);//多发送一次到底层，避免开启录像时底层会丢数据。

                if (reboot != Config.CAMERA_REBOOT_RECORD) {
                    LogUtils.d("setRecordQuality REBOOT_VIDEO!");
                    reboot = Config.CAMERA_REBOOT_VIDEO;
                }
            }
        }
    }

    @Override
    public void setRecordType(String type, boolean isReboot) {
        int location = RecordData.getInstance().mutableLocation;
        String tmpType = type;
        if (Config.TYPE_FOUR_STREAM.equals(tmpType) &&
                location != StorageDevice.MEDIA_CARD && location != StorageDevice.NAND_FLASH) {
            tmpType = Config.TYPE_QUART_STREAM;
        }

        String strType = "";
        if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
            if (strRecordTypeTwo != null) {
                if (Config.TYPE_ONE_STREAM.equals(tmpType)) {
                    strType = strRecordTypeTwo[0];
                } else {
                    strType = strRecordTypeTwo[1];
                }
            }
        } else {
            if (strRecordTypeAll != null) {
                if (Config.TYPE_FOUR_STREAM.equals(tmpType)) {
                    strType = strRecordTypeAll[3];
                } else if (Config.TYPE_ONE_STREAM.equals(tmpType)) {
                    strType = strRecordTypeAll[0];
                } else if (Config.TYPE_TWO_STREAM.equals(tmpType)) {
                    strType = strRecordTypeAll[1];
                } else {
                    strType = strRecordTypeAll[2];
                }
            }
        }
        if (settingBinding != null) settingBinding.tvRecordType.setText(strType);

        if (type != tmpType || isReboot) {
            LogUtils.d("setRecordType-----type=" + type + ", tmpType=" + tmpType);
            RecordData.getInstance().recordType.setValue(tmpType);
            SPUtils.putString(context, Config.KEY_RECORD_TYPE, tmpType);

            mAppBaseUI.getFourCameraManager().setCameraPlatformType(context, tmpType);

            int recordQuality = RecordData.getInstance().recordQuality.getValue();
            if (Config.TYPE_FOUR_STREAM.equals(tmpType) && recordQuality == Config.RECORD_BPS_5) {
                setRecordQuality(Config.RECORD_BPS_2, isReboot);
            }

            if (Config.TYPE_FOUR_STREAM.equals(tmpType) || Config.TYPE_QUART_STREAM.equals(tmpType)) {
                String curCameraType = RecordData.getInstance().cameraType.getValue();
                if (!Config.CAMTYPE_4HD.equals(curCameraType)) {//设置录制类型时，当设置为四路时必须设置为HD。
                    RecordData.getInstance().cameraType.setValue(Config.CAMTYPE_4HD);
                    SPUtils.setCameraType(getContext(), Config.CAMTYPE_4HD);
                    mAppBaseUI.getFourCameraManager().setCamera360Type(Config.CAMTYPE_4HD);
                }
            }

            reboot = Config.CAMERA_REBOOT_PREVIEW;
        }
    }

    @Override
    public Context getContext() {
        return context;
    }

    public void show(View parent) {
        LogUtils.d("WPTAG","------------------show SettingPopWindow------------------");

        if (contentView == null) {
            contentView = View.inflate(context, R.layout.layout_setting, null);
            cameraValue = contentView.findViewById(R.id.dvr_camera_value);
            settingBinding = DataBindingUtil.bind(contentView);
            scrollView = contentView.findViewById(R.id.setting_scroll);
        }

        initStringArray();

        String camera_value = SPUtils.getString(getContext(), Config.CAMERA_KEY, Config.CAMERA_KEY_DEF);
        LogUtils.d("---show mainStr_value=" + camera_value);
        if(!TextUtils.isEmpty(camera_value)){
            cameraValue.setText(camera_value);
        }

        int videoSettingsType =  SPUtils.getVideoSettings(context);//1：单录 2:双录
        String title = "";
       if (videoSettingsType == 0 ||videoSettingsType ==1){
            title = getContext().getResources().getString(R.string.str_video_settings_one);
        }else if (videoSettingsType ==2){
            title = getContext().getResources().getString(R.string.str_video_settings_double);
        }
        settingBinding.tvVideoSettingss.setText(title);

        reboot = Config.CAMERA_REBOOT_NOT;
        this.parent = parent;
        presenter = new SettingPresenter();
        presenter.onAttach(this);
        settingBinding.setPresenter(presenter);
        CustomPopWindow.PopupWindowBuilder builder = new CustomPopWindow.PopupWindowBuilder(context);
//        settingBinding.tvCarNumber.setText(SPUtils.getCarNumber(getContext()));
        int height = (int) context.getResources().getDimension(R.dimen.setting_window_height);
        popWindow = builder.setView(contentView)
                .size(650, height)
                .setAnimationStyle(R.style.anim_set)
                .setTouchable(true)
                .setFocusable(true)
                .setOnDissmissListener(this)
                .setBackgroundDrawable(context.getResources().getDrawable(
                        R.mipmap.bg_setting, context.getTheme()))
                .create()
                .showAtLocation(parent, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        LogUtils.d("------------------show Create.....------------------");

        settingBinding.rlCommonSettings.setVisibility(View.VISIBLE);
        settingBinding.rlFactorySettings.setVisibility(View.GONE);
        initFactoryLayout();

        if (DvrApplication.getClinetId().contains(Config.CLIENT_WY)) {
            settingBinding.layoutSensor.setVisibility(View.VISIBLE);
        }

        popContentView = LayoutInflater.from(context).inflate(R.layout.popmenu_layout, null, false);
    }

    private void initStringArray() {
        strSaveTime = context.getResources().getStringArray(R.array.array_save_time);
        strLocation = new String[]{context.getResources().getString(R.string.sd),
//                context.getResources().getString(R.string.local),
                context.getResources().getString(R.string.usb),
                context.getResources().getString(R.string.usb1)};
        strSensor = new String[]{context.getResources().getString(R.string.sensor_key1),
                context.getResources().getString(R.string.sensor_key2),
                context.getResources().getString(R.string.sensor_key3),
                context.getResources().getString(R.string.sensor_key0)};
        strMirror = new String[]{context.getResources().getString(R.string.normal_mirror),
                context.getResources().getString(R.string.left_right_mirror),
                context.getResources().getString(R.string.up_down_mirror),
                context.getResources().getString(R.string.mirror)};
        strRecordBPS = context.getResources().getStringArray(R.array.array_record_bps);
        strRecordTypeSD = new String[]{/*context.getResources().getString(R.string.setting_record_type1),
                context.getResources().getString(R.string.setting_record_type2),*/
                context.getResources().getString(R.string.setting_record_type4_1),
                context.getResources().getString(R.string.setting_record_type4)};
        strRecordTypeUsb = new String[]{/*context.getResources().getString(R.string.setting_record_type1),
                context.getResources().getString(R.string.setting_record_type2),*/
                context.getResources().getString(R.string.setting_record_type4_1)};
        strRecordTypeTwo = new String[]{context.getResources().getString(R.string.setting_record_type1),
                context.getResources().getString(R.string.setting_record_type2)};
        strRecordTypeAll = new String[]{context.getResources().getString(R.string.setting_record_type1),//单录
                context.getResources().getString(R.string.setting_record_type2),//双路
                context.getResources().getString(R.string.setting_record_type4_1),//四合一
                context.getResources().getString(R.string.setting_record_type4)};//四录
        strCameraType = new String[]{"HD(720)", "FHD(1080)"};
    }

    private void initFactoryLayout() {
        String curRecordType = RecordData.getInstance().recordType.getValue();
        LogUtils.d("initFactoryLayout   curRecordType=" + curRecordType);
        if (Config.TYPE_DUAL_STREAM.equals(curRecordType) || Config.TYPE_TWO_STREAM.equals(curRecordType)) {
            settingBinding.layoutDvrCamera.setVisibility(View.VISIBLE);
            settingBinding.layoutMainCameraMirror.setVisibility(View.VISIBLE);
            settingBinding.layoutLeftCameraMirror.setVisibility(View.GONE);
            settingBinding.layoutRightCameraMirror.setVisibility(View.GONE);
        } else if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            settingBinding.layoutDvrCamera.setVisibility(View.VISIBLE);
            settingBinding.layoutMainCameraMirror.setVisibility(View.GONE);
            settingBinding.layoutLeftCameraMirror.setVisibility(View.GONE);
            settingBinding.layoutRightCameraMirror.setVisibility(View.GONE);
        } else {
            settingBinding.layoutDvrCamera.setVisibility(View.GONE);
            settingBinding.layoutMainCameraMirror.setVisibility(View.VISIBLE);
            settingBinding.layoutLeftCameraMirror.setVisibility(View.VISIBLE);
            settingBinding.layoutRightCameraMirror.setVisibility(View.VISIBLE);
        }
    }

    public void dismiss() {
        popWindow.dissmiss();
        if(videoWindow != null) videoWindow.dismiss();
        if(cameraWindow1 != null) cameraWindow1.dismiss();
        if (mCarNumberWindow != null) mCarNumberWindow.onDismiss();
        if (factoryWindow != null) factoryWindow.onDismiss();
    }

    public boolean isSettingsShowing() {
        if (popWindow != null) {
            return popWindow.isShowing();
        }

        return false;
    }

    @Override
    public void onDismiss() {
        LogUtils.d("CameraWindow onDismiss! show=");

        settingBinding.splitScreenVgLayout.setVisibility(View.GONE);

        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });
        if (presenter != null) {
            presenter.onDetach();
            presenter.onDestroy();
        }

        if (listener != null) {
            LogUtils.d("AAA 2:已经设置主副类型，开始重启Camera");
            listener.onSettingsDismiss(reboot);
        }
    }

    @Override
    public void setRecordTypeOfCamera(int id) {

    }

    private void showPopMenu(final String[] data, final Button button, final String key) {
        if (null == popContentView) {
            return;
        }

        if (null == popUpWindow) {
            int height = 95;
            if (data != null && data.length > 0) {
                height = (data.length * height) / 2;
                if (data.length == 1 || data.length == 2) {
                    height += 15;
                }
            }
            popUpWindow = new CommonPopUpWindow(popContentView, 126, height, data,
                    getContext().getResources().getDrawable(R.drawable.bg_pop_menu), -1);
        }

        popUpWindow.setOnMyItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtils.d("showPopMenu----position:" + position + ", key=" + key);
                if (Config.KEY_RECORD_TYPE.equals(key)) {
                    onClickPopUpWindow(key, data[position]);
                } else {
                    onClickPopUpWindow(key, position);
                }
                popUpWindow.dismiss();
                popUpWindow = null;
            }
        });
        //监听窗口消失
        popUpWindow.setOnDismissListener(mDismissListener);
        int[] rect = new int[2];
        button.getLocationInWindow(rect);
        int yoff = 0 - popUpWindow.getHeight() - button.getHeight();
//        LogUtils.d("showPopMenu-----top=" + button.getTop() + ", x=" + rect[0] + ", y=" + rect[1] +
//                ", height=" + popUpWindow.getHeight());
        popUpWindow.showAsDropDown(button, -100, yoff, Gravity.BOTTOM);
    }
    private PopupWindow.OnDismissListener mDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            popUpWindow = null;
        }
    };
    private void onClickPopUpWindow(String key, int position) {
        LogUtils.d("onClickPopUpWindow----position:" + position + ", key=" + key + ", len=" + SENSOR_VALUE.length);
        if (Config.KEY_SAVETIME.equals(key)) {
            int saveTime = 3;
            if (position == 1) {
                saveTime = 5;
            }
            setAutoSaveTime(saveTime, true);
        } else if (Config.KEY_LOCATION.equals(key)) {
            if (strLocation != null && position < strLocation.length) {
                String locationStr = strLocation[position];
                int location = -1;
                if (locationStr.equals(getContext().getResources().getString(R.string.sd))) {
                    location = StorageDevice.MEDIA_CARD;
                } else if (locationStr.equals(getContext().getResources().getString(R.string.local))) {
                    location = StorageDevice.NAND_FLASH;
                } else if (locationStr.equals(getContext().getResources().getString(R.string.usb))) {
                    location = StorageDevice.USB;
                } else if (locationStr.equals(getContext().getResources().getString(R.string.usb1))) {
                    location = StorageDevice.USB1;
                }
                if (location != -1) {
                    setLocation(location, true);
                }
            }
        } else if (Config.KEY_SENSOR.equals(key)) {
            LogUtils.d("showPopMenu----position:" + position + ", key=" + key + ", len=" + SENSOR_VALUE.length);
            if (position < SENSOR_VALUE.length) {
                setSensor(SENSOR_VALUE[position], true);
            }
        } else if (Config.KEY_SUB_MIRROR.equals(key)) {
            setCameraMirror(Config.WWC2_CH0_FLIP, position, true);
        } else if (Config.KEY_MAIN_MIRROR.equals(key)) {
            setCameraMirror(Config.WWC2_CH1_FLIP, position, true);
        } else if (Config.KEY_LEFT_MIRROR.equals(key)) {
            setCameraMirror(Config.WWC2_CH2_FLIP, position, true);
        } else if (Config.KEY_RIGHT_MIRROR.equals(key)) {
            setCameraMirror(Config.WWC2_CH3_FLIP, position, true);
        } else if (Config.KEY_RECORD_QUALITY.equals(key)) {
            setRecordQuality(RECORD_BPS_VALUE[position], true);
        } else if (Config.KEY_RECORD_TYPE.equals(key)) {
            String curType = Config.TYPE_QUART_STREAM;
            switch (position) {
                case 0:
                    curType = Config.TYPE_ONE_STREAM;
                    break;
                case 1:
                    curType = Config.TYPE_TWO_STREAM;
                    break;
                case 2:
                    curType = Config.TYPE_QUART_STREAM;
                    break;
                case 3:
                    curType = Config.TYPE_FOUR_STREAM;
                    break;
            }
            setRecordType(curType, true);
        } else if (Config.KEY_CAMERA_TYPE.equals(key)) {
            setDvrCamera(position, true);
        }
    }
    private void onClickPopUpWindow(String key, String type) {
        LogUtils.d("onClickPopUpWindow----type:" + type + ", key=" + key + ", len=" + SENSOR_VALUE.length);
        if (Config.KEY_RECORD_TYPE.equals(key)) {
            String curType;
            if (strRecordTypeAll[0].equals(type)) {
                curType = Config.TYPE_ONE_STREAM;
            } else if (strRecordTypeAll[1].equals(type)) {
                curType = Config.TYPE_TWO_STREAM;
            } else if (strRecordTypeAll[2].equals(type)) {
                curType = Config.TYPE_QUART_STREAM;
            } else {
                curType = Config.TYPE_FOUR_STREAM;
            }
            setRecordType(curType, true);
        }
    }

    public interface OnSettingsDismissListener {
        void onSettingsDismiss(int type);
        void onAotuCheckListener();
        void onMainAndSubValue(String mainAndSub);
        void onLocationListener();
    }
}
