package com.wwc2.dvr.ui.record;

import android.os.Handler;
import android.os.SystemProperties;
import android.view.View;

import com.wwc2.BasePresenter;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.DvrApplication;
import com.wwc2.dvr.binding.command.BindingAction;
import com.wwc2.dvr.binding.command.BindingCommand;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.data.Stereo;
import com.wwc2.dvr.utils.Event;
import com.wwc2.dvr.utils.FileUtils;
import com.wwc2.dvr.utils.RxBus;
import com.wwc2.dvr.utils.SPUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by huwei on 19-3-18.
 */
public class RecordPresenter extends BasePresenter<RecordContract.V>  {

    private static final String TAG = RecordPresenter.class.getSimpleName();
    private static RecordData recordData;
    private Handler mHandler;

    public BindingCommand onClickCapture = new BindingCommand(new BindingAction() {
        @Override
        public void call(View v) {
            if (recordData.recordState.getValue()) {
                mView.stopRecording();
            } else {
                mView.showRecordPopWindow(v);
                mView.startRecording();
            }
        }
    });
    public BindingCommand onClickPhoto = new BindingCommand(new BindingAction() {
        @Override
        public void call(View v) {
                mView.onClickTakePicture();
        }
    });

    public BindingCommand onClickLockState = new BindingCommand(new BindingAction() {
        @Override
        public void call(View v) {
//            recordData.lockState.setValue(!recordData.lockState.getValue());
//            SPUtils.setLockStatus(mContext, recordData.lockState.getValue());
            mView.setLockStatus(!recordData.lockState/*.getValue()*/, true);
        }
    });
    public BindingCommand onClickFilemanager = new BindingCommand(new BindingAction() {
        @Override
        public void call(View v) {
            mView.showFilePopWindow(v);
        }
    });
    public BindingCommand onClickSettings = new BindingCommand(new BindingAction() {
        @Override
        public void call(View v) {
            mView.showSettingPopWindow(v);
        }
    });

    public BindingCommand saveOkClick = new BindingCommand(new BindingAction() {
        @Override
        public void call(View v) {
            mView.saveOkClick(v);
        }
    });
    public BindingCommand saveNoClick = new BindingCommand(new BindingAction() {
        @Override
        public void call(View v) {
            mView.saveNoClick(v);
        }
    });

    public BindingCommand onClickShowBottom = new BindingCommand(new BindingAction() {
        @Override
        public void call(View v) {
            mView.onClickShowBottom(v);
        }
    });

    @Override
    public void onCreate() {
        Disposable disposable = RxBus.getDefault().register(Event.class, new Consumer<Event>() {
            @Override
            public void accept(Event event) throws Exception {
                if (event.getCode() == Event.EVENT_STOP_RECORD_AND_EXIT) {
                    //zyh修改BUG:点击选择列表中的视频播放，关闭视频后，没有重新开始录像。
                    //mView.stopRecording();
                    mView.toBackgroundWindow();
                }
            }
        });
        mCompositeDisposable.add(disposable);
        recordData = RecordData.getInstance();
        if (recordData.brightness.getValue() == ConstantsData.INVALID_VALUE ||
                recordData.saturation.getValue() == ConstantsData.INVALID_VALUE ||
                recordData.contrast.getValue() == ConstantsData.INVALID_VALUE) {
            readDefaultParam();
        }
    }

    public static void readDefaultParam() {
        try {
            String cameraParam = FileUtils.readTextFile(Config.CAMERA_PARAMS_NODE);//readTextFile(CAMERA_PARAMS_NODE);
            LogUtils.e("readDefaultParam:" + cameraParam);
            String[] cameraParams = cameraParam.split(" ");
            if (cameraParams != null && cameraParams.length == 3) {
                recordData.brightness.setValue(Integer.parseInt(cameraParams[0]));
                recordData.saturation.setValue(Integer.parseInt(cameraParams[1]));
                recordData.contrast.setValue(Integer.parseInt(cameraParams[2]));
                LogUtils.e("readDefaultParam:" + recordData.brightness.getValue() + ", mDefSaturation=" +
                        recordData.saturation.getValue() + ", mDefContrast=" + recordData.contrast.getValue());
            } else {
                recordData.brightness.setValue(120);
                recordData.saturation.setValue(128);
                recordData.contrast.setValue(120);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//
//    /**
//     * 清除对应ID的视频文件
//     * @param oldEstId
//     */
//    private void clearVideoList(final long oldEstId){
//        final String oldEstVideoName = DBUtil.getVideNameById(oldEstId);
//        DBUtil.deleteDriveVideoById(oldEstId);
//        File file = new File(oldEstVideoName);
//        if (file.exists() && file.isFile()) {
//            file.delete();
//        }
//        //再次检查存储空间
//        checkRemainSpace();
//    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
