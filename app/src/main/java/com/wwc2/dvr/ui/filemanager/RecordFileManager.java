package com.wwc2.dvr.ui.filemanager;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;

import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.RecordService;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.DBUtil;
import com.wwc2.dvr.data.DriveVideo;
import com.wwc2.dvr.data.DriveVideoBack;
import com.wwc2.dvr.data.DriveVideoDual;
import com.wwc2.dvr.data.DriveVideoFont;
import com.wwc2.dvr.data.DriveVideoLeft;
import com.wwc2.dvr.data.DriveVideoQuart;
import com.wwc2.dvr.data.DriveVideoRight;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.fourCamera.FourCameraProxy;
import com.wwc2.dvr.utils.FileUtils;
import com.wwc2.dvr.utils.ListSortUtils;
import com.wwc2.dvr.utils.MultiFileObserver;
import com.wwc2.dvr.utils.SPUtils;
import com.wwc2.dvr.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by swd1 on 19-10-30.
 */

public class RecordFileManager {

    private static RecordFileManager manager = null;

    private MultiFileObserver observer;
    private MultiFileObserver observerImage;
    private RecordData recordData;

    private boolean syncImage = false;
    private ObservableList<DriveVideo> imageList = new ObservableArrayList<>();

    private String curRecordFileNameBack = "";
    private String curRecordFileNameFront = "";
    private String curRecordFileNameLeft = "";
    private String curRecordFileNameRight = "";
    private String curRecordFileNameQuart = "";
    private String curRecordFileNameDual = "";

    private Context context;

    private static final String TAG = "RecordFileManager";

    private final int MSG_VIDEO_OBVER  = 1;
    private final int MSG_IMAGE_OBVER  = 2;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VIDEO_OBVER:
                    startVideoFileObserver();
                    break;
//                case MSG_IMAGE_OBVER:
//                    startImageFileObserver();
//                    break;
                default:
                    break;
            }
        }
    };

    public static RecordFileManager getInstance(Context context) {
        if (manager == null) {
            manager = new RecordFileManager(context);
        }

        return manager;
    }

    public RecordFileManager(Context context) {
        this.context = context;
    }

    public void onCreate() {
        if (observer != null) {
            observer.stopWatching();
            observer = null;
        }
        if (observerImage != null) {
            LogUtils.d("startImageFileObserver---stop first!");
            observerImage.stopWatching();
            observerImage = null;
        }

        curRecordFileNameBack = "";
        curRecordFileNameFront = "";
        curRecordFileNameLeft = "";
        curRecordFileNameRight = "";
        curRecordFileNameQuart = "";
        curRecordFileNameDual = "";

        recordData = RecordData.getInstance();
        startVideoFileObserver();
//        startImageFileObserver();

        sortDataBase();
    }

    public void onDestory() {
        LogUtils.d("onDestory!");
        if (observer != null) {
            observer.stopWatching();
            observer = null;
        }

        if (observerImage != null) {
            observerImage.stopWatching();
            observerImage = null;
        }

        if (mHandler != null) {
//            mHandler.removeCallbacks(timeCheckFile);
//            mHandler = null;
        }

        imageList.clear();
    }

    private void sortDataBase() {
        /**检查数据库文件是否存在*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DriveVideoQuart> list1 = DBUtil.getAllDriveQuartVideo();
                if (list1 != null && list1.size() > 0) {
                    for (DriveVideoQuart quart : list1) {
                        if (!FileUtils.fileIsExists(quart.getName()) && locationExist()) {
                            DBUtil.deleteDriveQuartVideoByName(quart.getName());
                            LogUtils.e("onCreate----quart delete---name=" + quart.getName());
                        }
                    }
                } else {
                    LogUtils.e("onCreate----quart 0---size=0");
                }
                doSearchVideo(getVideoDir(recordData.mutableLocation) + "/" + ConstantsData.QUART);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DriveVideoFont> list = DBUtil.getAllDriveFontVideo();
                if (list != null && list.size() > 0) {
//                    LogUtils.e("onCreate----front 0---" + SystemClock.currentThreadTimeMillis() + ", size=" + list.size());
                    for (DriveVideoFont font : list) {
                        if (!FileUtils.fileIsExists(font.getName()) && locationExist()) {
                            LogUtils.e("onCreate----front delete---name=" + font.getName());
                            DBUtil.deleteDriveFontVideoByName(font.getName());
                        }
                    }
                } else {
                    LogUtils.e("onCreate----front 0---size==0");
                }
                doSearchVideo(getVideoDir(recordData.mutableLocation) + "/" + ConstantsData.FRONT);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DriveVideoBack> list1 = DBUtil.getAllDriveBackVideo();
                if (list1 != null && list1.size() > 0) {
//                    LogUtils.e("onCreate----back 0---" + SystemClock.currentThreadTimeMillis() + ", size=" + list1.size());
                    for (DriveVideoBack back : list1) {
                        if (!FileUtils.fileIsExists(back.getName()) && locationExist()) {
                            DBUtil.deleteDriveBackVideoByName(back.getName());
                            LogUtils.e("onCreate----back delete---name=" + back.getName());
                        }
                    }
                } else {
                    LogUtils.e("onCreate----back 0---size=0");
                }
                doSearchVideo(getVideoDir(recordData.mutableLocation) + "/" + ConstantsData.BACK);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DriveVideoLeft> list1 = DBUtil.getAllDriveLeftVideo();
                if (list1 != null && list1.size() > 0) {
//                    LogUtils.e("onCreate----left 0---" + SystemClock.currentThreadTimeMillis() + ", size=" + list1.size());
                    for (DriveVideoLeft left : list1) {
                        if (!FileUtils.fileIsExists(left.getName()) && locationExist()) {
                            DBUtil.deleteDriveLeftVideoByName(left.getName());
                            LogUtils.e("onCreate----left delete---name=" + left.getName());
                        }
                    }
                } else {
                    LogUtils.e("onCreate----back 0---size=0");
                }
                doSearchVideo(getVideoDir(recordData.mutableLocation) + "/" + ConstantsData.LEFT);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DriveVideoRight> list1 = DBUtil.getAllDriveRightVideo();
                if (list1 != null && list1.size() > 0) {
//                    LogUtils.e("onCreate----right 0---" + SystemClock.currentThreadTimeMillis() + ", size=" + list1.size());
                    for (DriveVideoRight right : list1) {
                        if (!FileUtils.fileIsExists(right.getName()) && locationExist()) {
                            DBUtil.deleteDriveRightVideoByName(right.getName());
//                                LogUtils.e("onCreate----right delete---name=" + right.getName());
                        }
                    }
                } else {
                    LogUtils.e("onCreate----back 0---size=0");
                }
                doSearchVideo(getVideoDir(recordData.mutableLocation) + "/" + ConstantsData.RIGHT);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DriveVideoRight> list1 = DBUtil.getAllDriveRightVideo();
                if (list1 != null && list1.size() > 0) {
//                    LogUtils.e("onCreate----right 0---" + SystemClock.currentThreadTimeMillis() + ", size=" + list1.size());
                    for (DriveVideoRight right : list1) {
                        if (!FileUtils.fileIsExists(right.getName()) && locationExist()) {
                            DBUtil.deleteDriveRightVideoByName(right.getName());
//                                LogUtils.e("onCreate----right delete---name=" + right.getName());
                        }
                    }
                } else {
                    LogUtils.e("onCreate----back 0---size=0");
                }
                doSearchVideo(getVideoDir(recordData.mutableLocation) + "/" + ConstantsData.RIGHT);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DriveVideoDual> list1 = DBUtil.getAllDriveDualVideo();
                if (list1 != null && list1.size() > 0) {
//                    LogUtils.e("onCreate----right 0---" + SystemClock.currentThreadTimeMillis() + ", size=" + list1.size());
                    for (DriveVideoDual dual : list1) {
                        if (!FileUtils.fileIsExists(dual.getName()) && locationExist()) {
                            DBUtil.deleteDriveDualVideoByName(dual.getName());
//                                LogUtils.e("onCreate----dual delete---name=" + dual.getName());
                        }
                    }
                } else {
                    LogUtils.e("onCreate----back 0---size=0");
                }
                doSearchVideo(getVideoDir(recordData.mutableLocation) + "/" + ConstantsData.DUAL);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                syncImage = true;
                imageList.clear();
                imageList.addAll(doSearchImage(getImageDir(recordData.mutableLocation)));
                sortImage();
                syncImage = false;
            }
        }).start();
    }

    private  void sortImage() {
        Collections.sort(imageList, new Comparator<DriveVideo>() {
            @Override
            public int compare(DriveVideo arg0, DriveVideo arg1) {
                String datastr1 = arg0.getName().substring(arg0.getName().lastIndexOf("/")).replaceAll("/","").replaceAll(".jpg","");
                String datastr2 = arg1.getName().substring(arg1.getName().lastIndexOf("/")).replaceAll("/","").replaceAll(".jpg","");;
                Date date1 = ListSortUtils.parseServerTime(datastr1);
                Date date2 =  ListSortUtils.parseServerTime(datastr2);

                if (date2 == null) {
                    return 0;
                }

                return  date2.compareTo(date1);
            }
        });
    }

    private List<DriveVideo> doSearchImage(String path) {
        List<DriveVideo> mData = new ArrayList<DriveVideo>();
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] fileArray = file.listFiles();

                for (File f : fileArray) {
                    if (f.getName().endsWith("jpg")
                            || f.getName().endsWith("png")
                            || f.getName().endsWith("gif")) {
                        DriveVideo mDriveVideo = new DriveVideo();
                        String filepath = f.getAbsolutePath();

                        mDriveVideo.setName(filepath);
                        mDriveVideo.setLockStatus(false);
                        mData.add(mDriveVideo);
                    }
                }
            }
        }
        return mData;
    }

    private synchronized void doSearchVideo(String path) {
        LogUtils.d("doSearchVideo-----path=" + path);
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] fileArray = file.listFiles();

                for (File f : fileArray) {
                    if (f.getName().endsWith(Config.VIDEO_FLAG) || f.getName().endsWith(Config.VIDEO_FLAG_TS)) {
                        String strDate = FileUtils.replaceExt(f.getName().substring(ListSortUtils.getPosition(f.getName(), 2), f.getName().length()));
                        if (null != strDate) {
                            Date date = ListSortUtils.parseServerTime(strDate);
                            if (date != null) {
                                String filepath = f.getAbsolutePath();
//                                LogUtils.d("doSearchVideo-----filepath=" + filepath);
                                if (path.contains(ConstantsData.QUART)) {
                                    DriveVideoQuart driveVideo = DBUtil.getQuartDriveVideoByName(filepath);
                                    if (driveVideo == null) {
                                        driveVideo = new DriveVideoQuart(null, filepath, false, recordData.mutableLocation);
                                        DBUtil.addQuartDriveVideo(driveVideo);
                                    }
                                } else if (path.contains(ConstantsData.FRONT)) {
                                    DriveVideoFont driveVideo = DBUtil.getFontDriveVideoByName(filepath);
                                    if (driveVideo == null) {
                                        driveVideo = new DriveVideoFont(null, filepath, false, recordData.mutableLocation);
                                        DBUtil.addFontDriveVideo(driveVideo);
                                    }
                                } else if (path.contains(ConstantsData.BACK)) {
                                    DriveVideoBack driveVideo = DBUtil.getBackDriveVideoByName(filepath);
                                    if (driveVideo == null) {
                                        driveVideo = new DriveVideoBack(null, filepath, false, recordData.mutableLocation);
                                        DBUtil.addBackDriveVideo(driveVideo);
                                    }
                                } else if (path.contains(ConstantsData.LEFT)) {
                                    DriveVideoLeft driveVideo = DBUtil.getLeftDriveVideoByName(filepath);
                                    if (driveVideo == null) {
                                        driveVideo = new DriveVideoLeft(null, filepath, false, recordData.mutableLocation);
                                        DBUtil.addLeftDriveVideo(driveVideo);
                                    }
                                } else if (path.contains(ConstantsData.RIGHT)) {
                                    DriveVideoRight driveVideo = DBUtil.getRightDriveVideoByName(filepath);
                                    if (driveVideo == null) {
                                        driveVideo = new DriveVideoRight(null, filepath, false, recordData.mutableLocation);
                                        DBUtil.addRightDriveVideo(driveVideo);
                                    }
                                } else if (path.contains(ConstantsData.DUAL)) {
                                    DriveVideoDual driveVideo = DBUtil.getDualDriveVideoByName(filepath);
                                    if (driveVideo == null) {
                                        driveVideo = new DriveVideoDual(null, filepath, false, recordData.mutableLocation);
                                        DBUtil.addDualDriveVideo(driveVideo);
                                    }
                                }
                            } else {
                                LogUtils.e("doSearchVideo-----name=" + f.getName());
                            }
                        }
                    }
                }
            } else {
                LogUtils.e("doSearchVideo-----return 0");
            }
        } else {
            LogUtils.e("doSearchVideo-----return 1");
        }
    }

    void createFileDir(String path){
        File file = new File(path);
        if (!file.exists()) {
            directoryDelete(path, false);
            file.mkdirs();
        }
        LogUtils.d("createFileDir-----path=" + path);
    }

    void createFourFile(String path){
        LogUtils.e("createFourFile-----path=" + path);
        createFileDir(path);

        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_TWO_STREAM.equals(curRecordType) ||
                Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            if (Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
                createFileDir(path + "/" + ConstantsData.DUAL);
            }
            createFileDir(path + "/" + ConstantsData.BACK);
            createFileDir(path + "/" + ConstantsData.FRONT);
        } else {
            createFileDir(path + "/" + ConstantsData.QUART);
            if (!path.contains("usbotg")) {//存储到USB只能录制四合一，不新建下面四个文件夹。
                createFileDir(path + "/" + ConstantsData.BACK);
                createFileDir(path + "/" + ConstantsData.FRONT);
                createFileDir(path + "/" + ConstantsData.LEFT);
                createFileDir(path + "/" + ConstantsData.RIGHT);
            }
        }
        createFileDir(path + "/" + ConstantsData.PICTURES);
    }

    private  void startVideoFileObserver() {
        if (observer != null) {
            return;
        }
        final int location = recordData.mutableLocation;
        String path = getVideoDir(location);
        createFourFile(path);

        observer = new MultiFileObserver(location, path/*getVideoDir(location)*/,
                FileObserver.DELETE | FileObserver.CREATE | FileObserver.DELETE_SELF | FileObserver.MOVE_SELF);

        LogUtils.e("startVideoFileObserver----path=" + getVideoDir(location));

        observer.setFileListener(new MultiFileObserver.FileListener() {

            @Override
            public void onFileCreated(int storageId, String name) {
                if (name != null && name.startsWith(getVideoDir(location))) {
                    if (name.endsWith(Config.VIDEO_FLAG) || name.endsWith(Config.VIDEO_FLAG_TS)) {
                        LogUtils.d(TAG, "create file， name = " + name);
                        boolean lock = recordData.lockState;
                        if (isSensor) {
                            lock = true;
                        }
                        if (name.contains(ConstantsData.FRONT)) {
                            DriveVideoFont driveVideo = new DriveVideoFont(null, name, lock, location);
                            curRecordFileNameFront = name;
                            DBUtil.addFontDriveVideo(driveVideo);
                        } else if (name.contains(ConstantsData.BACK)) {
                            DriveVideoBack driveVideo = new DriveVideoBack(null, name, lock, location);
                            curRecordFileNameBack = name;
                            DBUtil.addBackDriveVideo(driveVideo);
                        } else if (name.contains(ConstantsData.LEFT)) {
                            DriveVideoLeft driveVideo = new DriveVideoLeft(null, name, lock, location);
                            curRecordFileNameLeft = name;
                            DBUtil.addLeftDriveVideo(driveVideo);
                        } else if (name.contains(ConstantsData.RIGHT)) {
                            DriveVideoRight driveVideo = new DriveVideoRight(null, name, lock, location);
                            curRecordFileNameRight = name;
                            DBUtil.addRightDriveVideo(driveVideo);
                        } else if (name.contains(ConstantsData.QUART)) {
                            DriveVideoQuart driveVideo = new DriveVideoQuart(null, name, lock, location);
                            curRecordFileNameQuart = name;
                            DBUtil.addQuartDriveVideo(driveVideo);
                        } else if (name.contains(ConstantsData.DUAL)) {
                            DriveVideoDual driveVideo = new DriveVideoDual(null, name, lock, location);
                            curRecordFileNameDual = name;
                            DBUtil.addDualDriveVideo(driveVideo);
                        }

                        String curRecordType = RecordData.getInstance().recordType.getValue();
                        if (Config.TYPE_QUART_STREAM.equals(curRecordType)) {
                            if (!TextUtils.isEmpty(curRecordFileNameQuart)) {
                                isSensor = false;
                            }
                        } else if (Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
                            if (!TextUtils.isEmpty(curRecordFileNameDual)) {
                                isSensor = false;
                            }
                        } else if (Config.TYPE_TWO_STREAM.equals(curRecordType)) {
                            if (!TextUtils.isEmpty(curRecordFileNameFront) && !TextUtils.isEmpty(curRecordFileNameBack)) {
                                isSensor = false;
                            }
                        } else if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
                            if (!TextUtils.isEmpty(curRecordFileNameFront)) {
                                isSensor = false;
                            }
                        } else {
                            if (!TextUtils.isEmpty(curRecordFileNameFront) && !TextUtils.isEmpty(curRecordFileNameBack) &&
                                    !TextUtils.isEmpty(curRecordFileNameLeft) && !TextUtils.isEmpty(curRecordFileNameRight)) {
                                isSensor = false;
                            }
                        }
                    } else if (Utils.isImageFile(name)) {//图片
                        LogUtils.d(TAG, "create file， name = " + name);
                        if (name.contains(ConstantsData.PICTURES)) {
                            updateDriveVideo(ConstantsData.TYPE_IMAGE, name, -1, false);//YDG
                        }
                    }
                }
            }

            @Override
            public void onFileDeleted(int storageId, String name) {
                LogUtils.e(TAG,"delete name:" + name);
                if (name != null) {
                    File file = new File(name);
                    if (isValidDvrDir(recordData.mutableLocation, name)) {//file.isDirectory()) {
//                        directoryDelete(name, true);
                    } else {
                        if (name.startsWith(getVideoDir(location))/* && name.endsWith(VIDEO_FLAG)*/) {
                            //视频文件删除
                            if (name.contains(ConstantsData.FRONT)) {
                                DBUtil.deleteDriveFontVideoByName(name);
                            } else if (name.contains(ConstantsData.BACK)) {
                                DBUtil.deleteDriveBackVideoByName(name);
                            } else if (name.contains(ConstantsData.LEFT)) {
                                DBUtil.deleteDriveLeftVideoByName(name);
                            } else if (name.contains(ConstantsData.RIGHT)) {
                                DBUtil.deleteDriveRightVideoByName(name);
                            } else if (name.contains(ConstantsData.QUART)) {
                                DBUtil.deleteDriveQuartVideoByName(name);
                            } else if (name.contains(ConstantsData.PICTURES)) {
                                deleteDriveVideo(ConstantsData.TYPE_IMAGE, name, -1);
                            } else if (name.contains(ConstantsData.DUAL)) {
                                DBUtil.deleteDriveDualVideoByName(name);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFileModified(int storageId, String name) {
                LogUtils.d(TAG,"modified name:" + name);
            }

            @Override
            public void onFileRenamed(int storageId, String oldName, String newName) {
                LogUtils.e(TAG,"renamed oldName:" + oldName + " new name:" + newName);

            }

            @Override
            public void onFileDeleteSelf(int storgeId, String name) {
                LogUtils.e("onFileDeleteSelf----storgeId=" + storgeId + ", name=" + name);
                if (isValidDvrDir(recordData.mutableLocation, name)) {
                    directoryDelete(name, true);
                } else {
                    LogUtils.e("onFileDeleteSelf----not ValidDvrDir return!");
                }
            }
        });
        observer.startWatching();
    }

    private void directoryDelete(String name, boolean isObserver) {
        int curDevice = recordData.mutableLocation;
        LogUtils.e("directoryDelete----curDevice=" + curDevice + ", video=" + getVideoDir(curDevice) + ", name=" + name);
        if (name.contains(getVideoDir(curDevice))) {
            if (isObserver) {
                if (observer != null) {
                    observer.stopWatching();
                    observer = null;
                }

                mHandler.removeMessages(MSG_VIDEO_OBVER);
                mHandler.sendEmptyMessageDelayed(MSG_VIDEO_OBVER, 2000);
            }

            try {
                if (name.contains(ConstantsData.FRONT)) {
                    DBUtil.deleteAllFontVideo();
                } else if (name.contains(ConstantsData.BACK)) {
                    DBUtil.deleteAllBackVideo();
                } else if (name.contains(ConstantsData.LEFT)) {
                    DBUtil.deleteAllLeftVideo();
                } else if (name.contains(ConstantsData.RIGHT)) {
                    DBUtil.deleteAllRightVideo();
                } else if (name.contains(ConstantsData.QUART)) {
                    DBUtil.deleteAllQuartVideo();
                } else if (name.contains(ConstantsData.PICTURES)) {
                    imageList.clear();
                } else if (name.contains(ConstantsData.DUAL)) {
                    DBUtil.deleteAllDualVideo();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isValidDvrDir(int deviceId, String path) {
        boolean ret = false;
        if (!TextUtils.isEmpty(path)) {
            String dir = getVideoDir(deviceId) + "/";
            if (path.equals(getVideoDir(deviceId)) || path.equals(dir + ConstantsData.FRONT) ||
                    path.equals(dir + ConstantsData.BACK) || path.equals(dir + ConstantsData.LEFT) ||
                    path.equals(dir + ConstantsData.RIGHT) || path.equals(dir + ConstantsData.QUART) ||
                    path.equals(dir + ConstantsData.DUAL) ||
                    path.equals(StorageDevice.getPath(deviceId) + ConstantsData.PICTURE_DIR)) {
                ret = true;
            }
        }
        return ret;
    }

    private  String getVideoDir(int location) {
        return StorageDevice.getPath(location) + ConstantsData.VIDEO_DIR;
    }

    private  String getImageDir(int location) {
        return StorageDevice.getPath(location) + ConstantsData.PICTURE_DIR;
    }

    String bakFileName = "";
    public void startImageFileObserver() {
        if (observerImage != null) {
            return;
        }
        final int location = recordData.mutableLocation;
        String path = getImageDir(location);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        LogUtils.d("startImageFileObserver， location=" + location + ",,path=" + path);
//        observerImage = new MultiFileObserver(location, path/*getVideoDir(location)*/, FileObserver.ALL_EVENTS);
        observerImage = new MultiFileObserver(location, path/*getVideoDir(location)*/,
                FileObserver.DELETE | FileObserver.CREATE | FileObserver.DELETE_SELF | FileObserver.MOVE_SELF);
        LogUtils.e(getImageDir(location));

        observerImage.setFileListener(new MultiFileObserver.FileListener() {

            @Override
            public void onFileCreated(int storageId, String name) {
                LogUtils.d("onFileCreated create file， name = " + name + ", bakFileName=" + bakFileName);
                if (!bakFileName.equals(name)) {
                    bakFileName = name;
                    updateDriveVideo(ConstantsData.TYPE_IMAGE, name, -1, false);//YDG
                }
            }

            @Override
            public void onFileDeleted(int storageId, String name) {
                LogUtils.e("onFileDeleted name:" + name);
                deleteDriveVideo(ConstantsData.TYPE_IMAGE, name, -1);
            }

            @Override
            public void onFileModified(int storageId, String name) {
                LogUtils.e("onFileModified name:" + name + ", bakFileName=" + bakFileName);
                //bug18913,没有走onFileCreated
                if (!bakFileName.equals(name)) {
                    bakFileName = name;
                    updateDriveVideo(ConstantsData.TYPE_IMAGE, name, -1, false);//YDG
                }
            }

            @Override
            public void onFileRenamed(int storageId, String oldName, String newName) {
                LogUtils.e("onFileRenamed oldName:" + oldName + " new name:" + newName);
            }

            @Override
            public void onFileDeleteSelf(int storgeId, String name) {
                LogUtils.e("onFileDeleteSelf----storgeId=" + storgeId + ", name=" + name + ", image=" + getImageDir(recordData.mutableLocation));
                if (name.equals(getImageDir(recordData.mutableLocation))) {
                    imageList.clear();
                    if (observerImage != null) {
                        LogUtils.d("startImageFileObserver---stop 2!");
                        observerImage.stopWatching();
                        observerImage = null;
                    }
                    mHandler.removeMessages(MSG_IMAGE_OBVER);
                    mHandler.sendEmptyMessageDelayed(MSG_IMAGE_OBVER, 2000);
//                    if (mHandler != null) {
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                //重新打开文件夹监听器，避免文件夹被删除
//                                startImageFileObserver();
//                            }
//                        }, 100);
//                    } else {
//                        LogUtils.e("onFileDeleteSelf---00-mHandler=null!");
//                    }
                } else {
                    LogUtils.e("onFileDeleteSelf---11-name not equal!");
                }
            }
        });
        observerImage.startWatching();
    }

    public boolean getSyncStatus(int type) {
        boolean ret = false;
        switch (type) {
            case ConstantsData.TYPE_IMAGE:
                ret = syncImage;
                break;
        }
        return ret;
    }

    public List<DriveVideo> getVideoList(int type) {
        switch (type) {
            case ConstantsData.TYPE_IMAGE:
                return imageList;
            default:
                break;
        }
        return null;
    }

    public synchronized void updateDriveVideo(int type, String name, int index, boolean lock) {
        switch (type) {
            case ConstantsData.TYPE_FRONT:
                DriveVideoFont driveVideoFont = DBUtil.getFontDriveVideoByName(name);
                if (driveVideoFont != null ) {
                    driveVideoFont.setLockStatus(lock);
                    DBUtil.updateFontDriveVideo(driveVideoFont);
                    //列表直接从数据库中读取，不需要更新缓存。
                }
                break;
            case ConstantsData.TYPE_BACK:
                DriveVideoBack driveVideoBack = DBUtil.getBackDriveVideoByName(name);
                if (driveVideoBack != null) {
                    driveVideoBack.setLockStatus(lock);
                    DBUtil.updateBackDriveVideo(driveVideoBack);
                    //列表直接从数据库中读取，不需要更新缓存。
                }
                break;
            case ConstantsData.TYPE_LEFT:
                DriveVideoLeft driveVideoLeft = DBUtil.getLeftDriveVideoByName(name);
                if (driveVideoLeft != null) {
                    driveVideoLeft.setLockStatus(lock);
                    DBUtil.updateLeftDriveVideo(driveVideoLeft);
                    //列表直接从数据库中读取，不需要更新缓存。
                }
                break;
            case ConstantsData.TYPE_RIGHT:
                DriveVideoRight driveVideoRight = DBUtil.getRightDriveVideoByName(name);
                if (driveVideoRight != null) {
                    driveVideoRight.setLockStatus(lock);
                    DBUtil.updateRightDriveVideo(driveVideoRight);
                    //列表直接从数据库中读取，不需要更新缓存。
                }
                break;
            case ConstantsData.TYPE_IMAGE:
                for (DriveVideo driveVideo : imageList) {
                    if (driveVideo.getName().equals(name)) {
                        LogUtils.d("ConstantsData.TYPE_IMAGE----name=" + name);
                        break;
                    }
                }
                syncImage = true;
                DriveVideo mDriveVideo = new DriveVideo();
                mDriveVideo.setName(name);
                imageList.add(0, mDriveVideo);
//                sortImage();
                syncImage = false;
                break;
            case ConstantsData.TYPE_QUART:
                DriveVideoQuart driveVideoQuart = DBUtil.getQuartDriveVideoByName(name);
                if (driveVideoQuart != null) {
                    driveVideoQuart.setLockStatus(lock);
                    DBUtil.updateQuartDriveVideo(driveVideoQuart);
                    //列表直接从数据库中读取，不需要更新缓存。
                }
                break;
            case ConstantsData.TYPE_DUAL:
                DriveVideoDual driveVideoDual = DBUtil.getDualDriveVideoByName(name);
                if (driveVideoDual != null) {
                    driveVideoDual.setLockStatus(lock);
                    DBUtil.updateDualDriveVideo(driveVideoDual);
                    //列表直接从数据库中读取，不需要更新缓存。
                }
                break;
            default:
                break;
        }
    }

    public synchronized  void deleteDriveVideo(int type, String name, int index) {
        switch (type) {
            case ConstantsData.TYPE_IMAGE:
                syncImage = true;
                LogUtils.e("deleteDriveVideo----size=" + imageList.size() + ", name=" + name);
                if (index < imageList.size() && index > 0) {
                    if (name.equals(imageList.get(index).getName())) {
                        imageList.remove(index);
                        syncImage = false;
                        return;
                    }
                }

                for (DriveVideo driveVideo : imageList) {
                    if (driveVideo.getName().equals(name)) {
                        imageList.remove(driveVideo);
                        break;
                    }
                }
                LogUtils.e("deleteDriveVideo----size=" + imageList.size());
                syncImage = false;
                break;
            default:
                break;
        }
    }

    public String getCurRecordFileName(int type) {
        String ret = "";
        switch (type) {
            case ConstantsData.TYPE_FRONT:
                ret = curRecordFileNameFront;
                break;
            case ConstantsData.TYPE_BACK:
                ret = curRecordFileNameBack;
                break;
            case ConstantsData.TYPE_LEFT:
                ret = curRecordFileNameLeft;
                break;
            case ConstantsData.TYPE_RIGHT:
                ret = curRecordFileNameRight;
                break;
            case ConstantsData.TYPE_QUART:
                ret = curRecordFileNameQuart;
                break;
            case ConstantsData.TYPE_DUAL:
                ret = curRecordFileNameDual;
                break;
        }
        return ret;
    }

    private boolean isSensor = false;
    public void setCurRecordFileName(int type, String name) {
        switch (type) {
            case ConstantsData.TYPE_FRONT:
                curRecordFileNameFront = name;
                break;
            case ConstantsData.TYPE_BACK:
                curRecordFileNameBack = name;
                break;
            case ConstantsData.TYPE_LEFT:
                curRecordFileNameLeft = name;
                break;
            case ConstantsData.TYPE_RIGHT:
                curRecordFileNameRight = name;
                break;
            case ConstantsData.TYPE_QUART:
                curRecordFileNameQuart = name;
                break;
            case ConstantsData.TYPE_DUAL:
                curRecordFileNameDual = name;
                break;
            case -1:
                isSensor = true;
                curRecordFileNameFront = name;
                curRecordFileNameBack = name;
                curRecordFileNameLeft = name;
                curRecordFileNameRight = name;
                curRecordFileNameQuart = name;
                curRecordFileNameDual = name;
                break;
        }
    }

    //刷新数据库
    private class UpdateThread extends Thread {
        List<DriveVideo> mCurList = new ArrayList<>();
        int mCurType = ConstantsData.TYPE_FRONT;
        boolean mStop = false;

        public UpdateThread(List<DriveVideo> list, int type) {
            mCurList.addAll(list);
            mCurType = type;
        }

        @Override
        public void run() {
            if (mCurList == null) {
                return;
            }
            if (mCurType == ConstantsData.TYPE_FRONT) {
                for (int position = 0; position < mCurList.size(); position++) {
                    if (mStop) {
                        break;
                    }

                    DriveVideo driveVideo = mCurList.get(position);
                    if (driveVideo != null) {
                        LogUtils.d("updataListLockStatus----position=" + position + ", status=" + driveVideo.getLockStatus());
                        DriveVideoFont font = DBUtil.getFontDriveVideoByName(driveVideo.getName());
                        if (font != null) {
                            font.setLockStatus(driveVideo.getLockStatus());
                            DBUtil.updateFontDriveVideo(font);
                        }
                    }
                }
            } else if (mCurType == ConstantsData.TYPE_BACK) {
                for (int position = 0; position < mCurList.size(); position++) {
                    if (mStop) {
                        break;
                    }

                    DriveVideo driveVideo = mCurList.get(position);
                    if (driveVideo != null) {
                        LogUtils.d("updataListLockStatus----position=" + position + ", status=" + driveVideo.getLockStatus());
                        DriveVideoBack back = DBUtil.getBackDriveVideoByName(driveVideo.getName());
                        if (back != null) {
                            back.setLockStatus(driveVideo.getLockStatus());
                            DBUtil.updateBackDriveVideo(back);
                        }
                    }
                }
            }else if (mCurType == ConstantsData.TYPE_LEFT){
                for (int position = 0; position < mCurList.size(); position++) {
                    if (mStop) {
                        break;
                    }

                    DriveVideo driveVideo = mCurList.get(position);
                    if (driveVideo != null) {
                        LogUtils.d("updataListLockStatus----position=" + position + ", status=" + driveVideo.getLockStatus());
                        DriveVideoLeft left = DBUtil.getLeftDriveVideoByName(driveVideo.getName());
                        if (left != null) {
                            left.setLockStatus(driveVideo.getLockStatus());
                            DBUtil.updateLeftDriveVideo(left);
                        }
                    }
                }
            }else if (mCurType == ConstantsData.TYPE_RIGHT){
                for (int position = 0; position < mCurList.size(); position++) {
                    if (mStop) {
                        break;
                    }

                    DriveVideo driveVideo = mCurList.get(position);
                    if (driveVideo != null) {
                        LogUtils.d("updataListLockStatus----position=" + position + ", status=" + driveVideo.getLockStatus());
                        DriveVideoRight right = DBUtil.getRightDriveVideoByName(driveVideo.getName());
                        if (right != null) {
                            right.setLockStatus(driveVideo.getLockStatus());
                            DBUtil.updateRightDriveVideo(right);
                        }
                    }
                }
            } else if (mCurType == ConstantsData.TYPE_QUART) {
                for (int position = 0; position < mCurList.size(); position++) {
                    if (mStop) {
                        break;
                    }

                    DriveVideo driveVideo = mCurList.get(position);
                    if (driveVideo != null) {
                        LogUtils.d("updataListLockStatus----position=" + position + ", status=" + driveVideo.getLockStatus());
                        DriveVideoQuart quart = DBUtil.getQuartDriveVideoByName(driveVideo.getName());
                        if (quart != null) {
                            quart.setLockStatus(driveVideo.getLockStatus());
                            DBUtil.updateQuartDriveVideo(quart);
                        }
                    }
                }
            } else if (mCurType == ConstantsData.TYPE_DUAL) {
                for (int position = 0; position < mCurList.size(); position++) {
                    if (mStop) {
                        break;
                    }

                    DriveVideo driveVideo = mCurList.get(position);
                    if (driveVideo != null) {
                        LogUtils.d("updataListLockStatus----position=" + position + ", status=" + driveVideo.getLockStatus());
                        DriveVideoDual dual = DBUtil.getDualDriveVideoByName(driveVideo.getName());
                        if (dual != null) {
                            dual.setLockStatus(driveVideo.getLockStatus());
                            DBUtil.updateDualDriveVideo(dual);
                        }
                    }
                }
            }
        }

        public void needCancel() {
            LogUtils.e("UpdateThread---needCancel!");
            mStop = true;
            interrupt();
        }
    }

    private UpdateThread frontThread = null;
    private UpdateThread backThread = null;
    private UpdateThread leftThread = null;
    private UpdateThread rightThread = null;
    private UpdateThread quartThread = null;
    private UpdateThread dualThread = null;
    public synchronized void updateDBUtilLockStatus(List<DriveVideo> list, int type) {
        LogUtils.i("updataListLockStatus----size=" + list.size() + ", type=" + type);
        switch (type) {
            case ConstantsData.TYPE_FRONT:
                if (frontThread != null) {
                    frontThread.needCancel();
                    frontThread = null;
                }
                frontThread = new UpdateThread(list, type);
                frontThread.start();
                break;
            case ConstantsData.TYPE_BACK:
                if (backThread != null) {
                    backThread.needCancel();
                    backThread = null;
                }
                backThread = new UpdateThread(list, type);
                backThread.start();
                break;
            case ConstantsData.TYPE_LEFT:
                if (leftThread != null) {
                    leftThread.needCancel();
                    leftThread = null;
                }
                leftThread = new UpdateThread(list, type);
                leftThread.start();
                break;
            case ConstantsData.TYPE_RIGHT:
                if (rightThread != null) {
                    rightThread.needCancel();
                    rightThread = null;
                }
                rightThread = new UpdateThread(list, type);
                rightThread.start();
                break;
            case ConstantsData.TYPE_QUART:
                if (quartThread != null) {
                    quartThread.needCancel();
                    quartThread = null;
                }
                quartThread = new UpdateThread(list, type);
                quartThread.start();
                break;
            case ConstantsData.TYPE_DUAL:
                if (dualThread != null) {
                    dualThread.needCancel();
                    dualThread = null;
                }
                dualThread = new UpdateThread(list, type);
                dualThread.start();
                break;
            default:
                break;
        }
    }

    boolean sdMounted = false;
    boolean usbMounted = false;
    boolean usb1Mounted = false;
    boolean usb2Mounted = false;
    boolean usb3Mounted = false;
    public void setUsbMounted(int location, boolean mounted) {
        LogUtils.d("setUsbMounted---location=" + location + ", mounted=" + mounted);
//        StorageDevice.parseFileOrDirName(context,path);
        switch (location) {
            case StorageDevice.MEDIA_CARD:
                SPUtils.setLocation(context, location);
                if (sdMounted != mounted) {
                    sdMounted = mounted;
                } else return;
                break;
            case StorageDevice.USB:
                if (usbMounted != mounted) {
                    usbMounted = mounted;
                } else return;
                break;
            case StorageDevice.USB1:
                if (usb1Mounted != mounted) {
                    usb1Mounted = mounted;
                } else return;
                break;
            case StorageDevice.USB2:
                if (usb2Mounted != mounted) {
                    usb2Mounted = mounted;
                } else return;
                break;
            case StorageDevice.USB3:
                if (usb3Mounted != mounted) {
                    usb3Mounted = mounted;
                } else return;
                break;
            default:
                return;
        }
        if (mounted) {
            //由于写存储路径方式改动，需底层处理，暂不处理。2020-06-12
            if (mHandler!=null){
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.d("setUsbMounted---startThread---location=" + location);
                        new CopyThread(location).start();
                    }
                }, 1000);
            }
        }
    }

    private String getTempUsbDir(int location) {
        String ret = Config.USB_PATH;
        switch (location) {
            case StorageDevice.USB:
                break;
            case StorageDevice.USB1:
            case StorageDevice.USB2:
            case StorageDevice.USB3:
                ret = ret + (location - 4);
                break;
            case StorageDevice.MEDIA_CARD:
                ret = Config.SD_PATH;
                break;
        }
        return ret;
    }

    private class CopyThread extends Thread {
        int location = StorageDevice.MEDIA_CARD;

        public CopyThread(int type) {
            location = type;
        }

        @Override
        public void run() {
            String path = StorageDevice.getPath(StorageDevice.NAND_FLASH) + getTempUsbDir(location) + "/pictures";
            File file = new File(path);
            LogUtils.d("CopyThread--location=" + location + ", path=" + path);
            if (file.exists() && file.isDirectory()) {
                File[] fileArray = file.listFiles();
                if (fileArray != null && fileArray.length > 0) {
                    LogUtils.d("CopyThread--location=" + location + ", path=" + path);
                    for (File f : fileArray) {
                        if (f.getName().endsWith("jpg")
                                || f.getName().endsWith("png")
                                || f.getName().endsWith("gif")) {
                            if (!isLocationMounted(location) || !RecordService.getCurAccStatus()) {
                                LogUtils.e("CopyThread--break!");
                                break;
                            }
                            String filepath = f.getAbsolutePath();
                            String name = filepath.substring(filepath.lastIndexOf("/")).replaceAll("/", "");
                            name = StorageDevice.getPath(location) + File.separator + ConstantsData.PICTURE_DIR + File.separator + name;
                            LogUtils.d("CopyThread--path=" + f.getPath() + ", getAbsolutePath=" + f.getAbsolutePath() + ", name=" + name);
                            try {
                                if (FileUtils.copyFile(f.getAbsolutePath(), name)) {
                                    FileUtils.deleteFile(filepath);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    LogUtils.e("The dir have no image file!");
                }
            } else {
                LogUtils.e("The dir not exist!");
            }

            //拷贝back视频
            copyVideo(location, ConstantsData.BACK);
            copyVideo(location, ConstantsData.FRONT);
            copyVideo(location, ConstantsData.LEFT);
            copyVideo(location, ConstantsData.RIGHT);
            copyVideo(location, ConstantsData.QUART);
            copyVideo(location, ConstantsData.DUAL);
        }
    }

    private synchronized void copyVideo(int location, String dir) {
        String path = StorageDevice.getPath(StorageDevice.NAND_FLASH) + getTempUsbDir(location) + "/" + dir;
        File fileVideo = new File(path);
        LogUtils.d("copyVideo-2-location=" + location + ", path=" + path);
        if (fileVideo.exists() && fileVideo.isDirectory()) {
            File[] fileArray = fileVideo.listFiles();
            if (fileArray != null && fileArray.length > 0) {
//                    LogUtils.d("CopyThread-2-location=" + location + ", path=" + path);
                for (File f : fileArray) {
                    if (f.getName().endsWith(Config.VIDEO_FLAG) || f.getName().endsWith(Config.VIDEO_FLAG_TS)) {
                        if (!isLocationMounted(location) || !RecordService.getCurAccStatus()) {
                            LogUtils.e("CopyThread--break2!");
                            break;
                        }

                        String filepath = f.getAbsolutePath();
                        String name = filepath.substring(filepath.lastIndexOf("/")).replaceAll("/", "");
//                            LogUtils.d("CopyThread-2-path=" + f.getPath() + ", getAbsolutePath=" + f.getAbsolutePath() + ", name=" + name);
                        if (f.getName().contains(dir)) {
                            name = getVideoDir(location) + "/" + dir + "/" + name;
                            LogUtils.d("copy---main--recordData.lockState-->" + recordData.lockState + ", dir=" + dir);
                            if (dir.equals(ConstantsData.BACK)) {
                                DriveVideoBack driveVideo = new DriveVideoBack(null, name, true, location);
                                DBUtil.addBackDriveVideo(driveVideo);
                            } else if (dir.equals(ConstantsData.FRONT)) {
                                DriveVideoFont driveVideo = new DriveVideoFont(null, name, true, location);
                                DBUtil.addFontDriveVideo(driveVideo);
                            } else if (dir.equals(ConstantsData.LEFT)) {
                                DriveVideoLeft driveVideo = new DriveVideoLeft(null, name, true, location);
                                DBUtil.addLeftDriveVideo(driveVideo);
                            } else if (dir.equals(ConstantsData.RIGHT)) {
                                DriveVideoRight driveVideo = new DriveVideoRight(null, name, true, location);
                                DBUtil.addRightDriveVideo(driveVideo);
                            } else if (dir.equals(ConstantsData.QUART)) {
                                DriveVideoQuart driveVideo = new DriveVideoQuart(null, name, true, location);
                                DBUtil.addQuartDriveVideo(driveVideo);
                            } else if (dir.equals(ConstantsData.DUAL)) {
                                DriveVideoDual driveVideo = new DriveVideoDual(null, name, true, location);
                                DBUtil.addDualDriveVideo(driveVideo);
                            }
                        }

                        try {
                            if (FileUtils.copyFile(f.getAbsolutePath(), name)) {
                                FileUtils.deleteFile(filepath);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        LogUtils.e("The file2 is:" + f.getName());
                    }
                }
            } else {
                LogUtils.e("The dir 2 have no video file!");
            }
        }
    }

    private boolean isLocationMounted(int type) {
        boolean ret = false;
        switch (type) {
            case StorageDevice.MEDIA_CARD:
                ret = sdMounted;
                break;
            case StorageDevice.USB:
                ret = usbMounted;
                break;
            case StorageDevice.USB1:
                ret = usb1Mounted;
                break;
            case StorageDevice.USB2:
                ret = usb2Mounted;
                break;
            case StorageDevice.USB3:
                ret = usb3Mounted;
                break;
        }
        LogUtils.d("isLocationMounted--location=" + type + ", ret=" + ret);
        return ret;
    }

    private boolean locationExist() {
        int location = recordData.mutableLocation;
        if (!FileUtils.fileIsExists(StorageDevice.getPath(location))) {
            return false;
        }
        return true;
    }
}
