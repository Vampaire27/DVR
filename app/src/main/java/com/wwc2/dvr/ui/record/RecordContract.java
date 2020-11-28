package com.wwc2.dvr.ui.record;

import android.view.View;

import com.wwc2.IBaseView;

/**
 * Created by huwei on 19-3-18.
 */
public interface RecordContract {
    interface V extends IBaseView {
        void startRecording();

        void stopRecording();

        void onClickTakePicture();
        String takePicture(boolean isOff, int channel, boolean network);

        void setLockStatus(Boolean b, Boolean viewSet);

        void showFilePopWindow(View v);

        void showSettingPopWindow(View v);

        void toBackgroundWindow();

        void toForegroundWindow();

        void saveOkClick(View view);
        void saveNoClick(View view);

        void showRecordPopWindow(View v);

        void onClickShowBottom(View v);
    }
}
