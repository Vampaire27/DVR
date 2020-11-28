package com.wwc2.dvr.ui.settings;

import android.view.View;

import com.wwc2.IBaseView;

/**
 * Created by huwei on 19-3-19.
 */
public interface SettingContract {
    interface V extends IBaseView {
        void setAutoSaveTime(int time, boolean c);

        void setSplitScreen(int type, boolean onlyUI);

        void setAutoRecord(boolean b);

        void setAutoCheck(boolean b);

        void setMuteRecord(boolean b, boolean c);

        void setWatermark(boolean b, boolean c);

        void setChannelWatermark(boolean b, boolean c);

        void setGPSWatermark(boolean b, boolean c);

        void setCarWatermark(boolean b, boolean c);

        void setCarNumber(String str, boolean c);

        void setLocation(int location, boolean c);

        void onClickRadioGroup(View v);

        void showVideoRadioGroup(int type);

        void onClickVideoRL(View v);

        void setClicksplistRL(View v);

        void setDvrCamera(int type, boolean isReboot);

        void setDvrVideo();


        void openFactory();

        void setSensor(int val, boolean reboot);

        void setRecordTypeOfCamera(int id);

        void carNumberSet();

        void setCameraMirror(int channel, int mirror, boolean c);

        void setRecordQuality(int type, boolean reboot);

        void setRecordType(String type, boolean reboot);
    }

    interface P {
//        void initSetings();
    }
}
