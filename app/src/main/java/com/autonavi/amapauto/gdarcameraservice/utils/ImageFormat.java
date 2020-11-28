package com.autonavi.amapauto.gdarcameraservice;

/**
 * 图像格式
 */
public enum ImageFormat {
    NV21_822(0),
    I420_822(1),
    RGB_888(2),
    RGBA_8888(3),
    YV12(4);

    private int format;

    private ImageFormat(int format) {
        this.format = format;
    }

    public int getFormat() {
        return this.format;
    }
}

