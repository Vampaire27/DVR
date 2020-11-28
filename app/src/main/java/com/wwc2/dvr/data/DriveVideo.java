package com.wwc2.dvr.data;

/**
 * description ： TODO:类的作用
 * user: wangpeng on 2019/9/25.
 * emai: wpeng@waterworld.com.cn
 */
public class DriveVideo {

    private Long id;
    private String name;
    private boolean lockStatus;
    private int localtion;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(boolean lockStatus) {
        this.lockStatus = lockStatus;
    }

    public int getLocaltion() {
        return localtion;
    }

    public void setLocaltion(int localtion) {
        this.localtion = localtion;
    }
}
