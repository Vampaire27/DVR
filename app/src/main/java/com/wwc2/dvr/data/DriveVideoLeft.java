package com.wwc2.dvr.data;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * description ： TODO:类的作用
 * user: wangpeng on 2020/4/2.
 * emai: wpeng@waterworld.com.cn
 */
@Entity
public class DriveVideoLeft {
    @Id(autoincrement = true)
    private Long id;
    private String name;
    private boolean lockStatus;
    private int localtion;
    @Generated(hash = 219237017)
    public DriveVideoLeft(Long id, String name, boolean lockStatus, int localtion) {
        this.id = id;
        this.name = name;
        this.lockStatus = lockStatus;
        this.localtion = localtion;
    }
    @Generated(hash = 1909218718)
    public DriveVideoLeft() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean getLockStatus() {
        return this.lockStatus;
    }
    public void setLockStatus(boolean lockStatus) {
        this.lockStatus = lockStatus;
    }
    public int getLocaltion() {
        return this.localtion;
    }
    public void setLocaltion(int localtion) {
        this.localtion = localtion;
    }

}
