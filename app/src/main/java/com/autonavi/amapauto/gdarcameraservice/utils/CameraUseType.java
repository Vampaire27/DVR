package com.autonavi.amapauto.gdarcameraservice;

/**
 * 摄像头所适用的类型
 */
public enum CameraUseType {
    /**
     * 用于通用的摄像
     */
    COMMON_CAMERA(0),
    /**
     * 用于AR导航摄像
     */
    AR_NAVI_CAMERA(1);

    /**
     * 摄像头所适用的类型
     */
    private int type;

    private CameraUseType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}
