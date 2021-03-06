package com.wwc2.dvr.data;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by huwei on 2016/9/12 0012.
 */
@Entity
public class DriveVideoFont {
    @Id(autoincrement = true)
    private Long id;
    @Unique
    @NotNull
    private String name;
    private boolean lockStatus;
    private int localtion;
//    @Transient 添加次标记之后不会生成数据库表的列

    @Generated(hash = 896738484)
    public DriveVideoFont(Long id, @NotNull String name, boolean lockStatus,
            int localtion) {
        this.id = id;
        this.name = name;
        this.lockStatus = lockStatus;
        this.localtion = localtion;
    }
    @Generated(hash = 1015248791)
    public DriveVideoFont() {
    }
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setId(long id) {
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
