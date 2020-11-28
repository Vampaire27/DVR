package com.wwc2.dvr.fourCamera;

import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.RecordData;

public class FourRecord extends RecordBase{

    final static String record_four_status = "/sys/devices/platform/wwc2_camera_combine/record_four_status";//四录、双录
    final static String record_status = "/sys/devices/platform/wwc2_camera_combine/record_status";//四合一、二合一

    public void startRecord() {
        CameraBean bean;
        String curRecordType = RecordData.getInstance().recordType.getValue();
        int value;
        if (Config.TYPE_QUART_STREAM.equals(curRecordType)) {//四合一
            value = Config.QUART_RECORD;
        } else if (Config.TYPE_DUAL_STREAM.equals(curRecordType)) {//二合一
            value = Config.DUAL_RECORD;
        } else if (Config.TYPE_TWO_STREAM.equals(curRecordType)) {//双录
            value = Config.TWO_RECORD;
        } else if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            value = Config.FRONT_RECORD;
        } else {//四录
            value = Config.FOUR_RECORD;
        }
        bean = new CameraBean(FourCameraProxy.CAMERA_ACTION_NODE, Config.MODE_WWC2_RECORD, value);
        bean.Action();
    }

    @Override
    protected boolean isRecorded() {
        boolean ret = false;
        String status;
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_QUART_STREAM.equals(curRecordType) ||
                Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            status = driveRead(record_status);
        } else {
            status = driveRead(record_four_status);
        }

        if (Config.RECORD_STATUS_START.equals(status) ||
                Config.RECORD_STATUS_RUNING.equals(status)) {
            ret = true;
        }
        return ret;
    }

    @Override
    protected boolean isRecorded(int type) {
        boolean ret = false;
        String status;
        if (type == Config.CAMERA_REBOOT_RECORD) {//设置录制类型时
            String curRecordType = RecordData.getInstance().recordType.getValue();
            if (Config.TYPE_QUART_STREAM.equals(curRecordType) ||
                    Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                    Config.TYPE_ONE_STREAM.equals(curRecordType)) {
                status = driveRead(record_four_status);
            } else {
                status = driveRead(record_status);
            }
            if (Config.RECORD_STATUS_START.equals(status) ||
                    Config.RECORD_STATUS_RUNING.equals(status)) {
                ret = true;
            }
        }
        return ret;
    }

    @Override
    protected String getCode() {
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_QUART_STREAM.equals(curRecordType) ||
                Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            return driveRead(record_status);
        } else {
            return driveRead(record_four_status);
        }
    }
}
