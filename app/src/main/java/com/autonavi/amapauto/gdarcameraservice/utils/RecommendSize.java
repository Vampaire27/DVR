package com.autonavi.amapauto.gdarcameraservice;

import android.os.RemoteException;

/**
 * 所建议的图像规格大小类
 */
public class RecommendSize {
    /**
     * 所建议的图像宽度
     */
    private int width = 0;
    /**
     * 所建议的图像高度
     */
    private int height = 0;

    public RecommendSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public RecommendSize(IGDSize gdSize) throws RemoteException{
        this.width = gdSize.getWidth();
        this.height = gdSize.getHeight();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}

