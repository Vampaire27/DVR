package com.wwc2.dvr.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * description ： TODO:类的作用
 * user: wangpeng on 2020/4/29.
 * emai: wpeng@waterworld.com.cn
 */
public class CommonBean implements Parcelable {//putParcelableArrayListExtra
    private int index; //1:删除未加锁 2：删除已加锁
    private String name;
    private boolean isCheck;

    public CommonBean(int index, String name, boolean isCheck) {
        this.name = name;
        this.isCheck = isCheck;
        this.index = index;
    }

    protected CommonBean(Parcel in) {
        index = in.readInt();
        name = in.readString();
        isCheck = in.readByte() != 0;
    }

    public static final Creator<CommonBean> CREATOR = new Creator<CommonBean>() {
        @Override
        public CommonBean createFromParcel(Parcel in) {
            return new CommonBean(in);
        }

        @Override
        public CommonBean[] newArray(int size) {
            return new CommonBean[size];
        }
    };

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeInt(index);
        parcel.writeString(name);
        parcel.writeByte((byte) (isCheck ? 1 : 0));
    }
}
