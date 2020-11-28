package com.wwc2.dvr.ui.filemanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.data.DaoMaster;
import com.wwc2.dvr.data.DaoSession;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.utils.FileUtils;

import java.io.IOException;

/**
 * Created by swd1 on 19-12-21.
 */

public class RecordFileDataBase {

    private static DaoSession daoSession;
    private static RecordFileDataBase mDataBase = null;
    private Context mContext = null;
    SQLiteDatabase db = null;

    public static RecordFileDataBase getDataBase() {
        if (mDataBase == null) {
            mDataBase = new RecordFileDataBase();
        }
        return mDataBase;
    }

    public void onCreate(Context context) {
        mContext = context;

        setupDatabase();
        LogUtils.d("RecordService","daoSession:" + daoSession);
        if (daoSession != null) {
            RecordFileManager.getInstance(context).onCreate();
        }
    }

    public void onDestroy() {
        RecordFileManager.getInstance(mContext).onDestory();

        mContext = null;
        daoSession = null;
        if (db != null) {
            db.close();
            db = null;
        }
    }

    /**
     * 配置数据库
     */
    private void setupDatabase() {
        if (mContext == null) {
            return;
        }
        int location = RecordData.getInstance().mutableLocation;
        String path = StorageDevice.getPath(StorageDevice.NAND_FLASH);//location);
        LogUtils.d("setupDatabase---location=" + location + ", path=" + path);
        String dbName_Bak = path + "recordDB" + location + "/video_1.db";
        String dbName = "/custom/" + "recordDB" + location + "/video_1.db";
        LogUtils.d("setupDatabase-0-dbName=" + FileUtils.fileIsExists(dbName) + ", dbName_Bak=" + FileUtils.fileIsExists(dbName_Bak));
        if (FileUtils.fileIsExists(dbName)) {
            if (FileUtils.fileIsExists(dbName_Bak)) {
                FileUtils.deleteFile(dbName_Bak);
            }
        } else {
            if (FileUtils.fileIsExists(dbName_Bak)) {
                try {
                    FileUtils.copyFile(dbName_Bak, dbName);
                    FileUtils.deleteFile(dbName_Bak);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        LogUtils.d("setupDatabase-1-dbName=" + FileUtils.fileIsExists(dbName) + ", dbName_Bak=" + FileUtils.fileIsExists(dbName_Bak));
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(mContext, dbName, null);
        //获取可写数据库
        db = helper.getWritableDatabase();
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(db);
        //获取Dao对象管理者
        daoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }
}
