package com.wwc2.dvr.data;

/**
 * Created by swd1 on 17-6-13.
 */

public class ConstantsData {
    public static final String SP_NAME = "dvrConfig";

    public static final String KEY_AUTO_SAVE_TIME = "autoSaveTime";
    public static final String KEY_AUTO_RECORD = "autoRecord";
    public static final String KEY_MUTE_RECORD = "muteRecord";
    public static final String KEY_CHANNEL_WATER_MARK = "channelWaterMark";
    public static final String KEY_TIME_WATER_MARK = "timeWaterMark";
    public static final String KEY_GPS_WATER_MARK = "GPSWaterMark";
    public static final String KEY_CAR_WATER_MARK = "carWaterMark";
    public static final String KEY_CAR_NUMBER = "carNumber";

    public static final String KEY_H264_FRAME_RATE = "h264FrameRate";
    public static final String KEY_H264_BIT_RATE = "h264BitRate";

    public static final String KEY_LOCATION = "location";
    public static final String KEY_VIDEOSETTINGS= "videoSettings";
    public static final String KEY_MAIN_MIRROR = "mainMirror_four";
    public static final String KEY_SUB_MIRROR = "subMirror_four";
    public static final String KEY_LEFT_MIRROR = "leftMirror_four";
    public static final String KEY_RIGHT_MIRROR = "rightMirror_four";
    public static final String KEY_LOCK_STATUS = "lockStatus";
    //分钟单位
    public static final int DEF_RECORD_TIME = 3;
    public static final boolean DEF_AUTO_RECORD = false;
    public static final boolean DEF_WATER_MARK = true;
    public static final int DEF_MIRROR = Config.CAMERA_MIRROR_NOR;

    public static final int DEF_H264_FRAME_RATE = 100; //0.1HZ
    public static final int DEF_H264_BIT_RATE =160; // 1KB.

    public static final String PICTURE_DIR = "dvr/pictures";
    public static final String VIDEO_DIR = "dvr";

    //倒车参数设置
    public static final int INVALID_VALUE = -1;
    public static final String KEY_BRIGHTNESS = "BRIGHTNESS";
    public static final String KEY_SATURATION = "SATURATION";
    public static final String KEY_CONTRAST = "CONTRAST";

    public static final int TYPE_FRONT      = 1;
    public static final int TYPE_BACK       = 2;
    public static final int TYPE_LEFT       = 12;
    public static final int TYPE_RIGHT      = 13;
    public static final int TYPE_IMAGE      = 14;
    public static final int TYPE_QUART      = 15;
    public static final int TYPE_DUAL       = 16;

    public static final String KEY_SENSOR = "Sensor";
    public static final String FRONT = "front";
    public static final String BACK = "back";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";
    public static final String QUART = "quart";
    public static final String DUAL = "dual";
    public static final String PICTURES = "pictures";

}
