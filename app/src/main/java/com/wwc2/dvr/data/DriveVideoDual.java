package com.wwc2.dvr.data;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class DriveVideoDual {
    @Id(autoincrement = true)//手动输入
    private Long id;
    //手动输入
    @Unique
    @NotNull
    private String name;
    private boolean lockStatus;
    private int localtion;

    //手动输入　@Generated后，编译，后面的会自动生成
    @Generated(hash = 676543388)
    public DriveVideoDual(Long id, @NotNull String name, boolean lockStatus,
            int localtion) {
        this.id = id;
        this.name = name;
        this.lockStatus = lockStatus;
        this.localtion = localtion;
    }

    @Generated(hash = 1561227927)
    public DriveVideoDual() {
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
