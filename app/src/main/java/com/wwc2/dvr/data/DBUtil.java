package com.wwc2.dvr.data;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.fourCamera.FourCameraProxy;
import com.wwc2.dvr.ui.filemanager.RecordFileDataBase;
import com.wwc2.dvr.utils.FileUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by huwei on 2016/9/12 0012.
 */
public class DBUtil {

    /**添加数据库*/
    // Add new DriveVideoFont
    public static long addFontDriveVideo(DriveVideoFont driveVideo) {
        LogUtils.d("addFontDriveVideo--------------------"+driveVideo.getName());
        if (RecordFileDataBase.getDaoSession() == null) {
            LogUtils.d("addFontDriveVideo===font===null" + driveVideo.getName());
            return 0;
        }
        return RecordFileDataBase.getDaoSession().getDriveVideoFontDao().insertOrReplace(driveVideo);
    }
  // Add new DriveVideoBack
    public static long addBackDriveVideo(DriveVideoBack driveVideo) {
        LogUtils.d("addBackDriveVideo--------------------" + driveVideo.getName());
        if (RecordFileDataBase.getDaoSession() == null) {
            LogUtils.d("addBackDriveVideo==back==null" + driveVideo.getName());
            return 0;
        }
        return RecordFileDataBase.getDaoSession().getDriveVideoBackDao().insertOrReplace(driveVideo);
    }
     // Add new DriveVideoleft
    public static long addLeftDriveVideo(DriveVideoLeft videoLeft){
        if (RecordFileDataBase.getDaoSession() == null) {
            return 0;
        }
        return RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().insertOrReplace(videoLeft);
    }
    public static long addRightDriveVideo(DriveVideoRight videoRight){
        if (RecordFileDataBase.getDaoSession() == null) {
            return 0;
        }
        return RecordFileDataBase.getDaoSession().getDriveVideoRightDao().insertOrReplace(videoRight);
    }
    public static long addQuartDriveVideo(DriveVideoQuart videoQuart){
        if (RecordFileDataBase.getDaoSession() == null) {
            return 0;
        }
        return RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().insertOrReplace(videoQuart);
    }
    public static long addDualDriveVideo(DriveVideoDual videoDual){
        if (RecordFileDataBase.getDaoSession() == null) {
            return 0;
        }
        return RecordFileDataBase.getDaoSession().getDriveVideoDualDao().insertOrReplace(videoDual);
    }

    /**从数据库获取*/
    // Get DriveVideoFont By Name
    public static DriveVideoFont getFontDriveVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoFont driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoFontDao().queryBuilder().where(DriveVideoFontDao
                .Properties.Name.eq(name)).unique();

        return driveVideo;
    }
    // Get DriveVideoBack By Name
    public static DriveVideoBack getBackDriveVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoBack driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoBackDao().queryBuilder().where(DriveVideoBackDao
                .Properties.Name.eq(name)).unique();
        return driveVideo;
    }
    public static DriveVideoLeft getLeftDriveVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoLeft driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().queryBuilder().where(DriveVideoLeftDao
                .Properties.Name.eq(name)).unique();

        return driveVideo;
    }
    public static DriveVideoRight getRightDriveVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoRight driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoRightDao().queryBuilder().where(DriveVideoRightDao
                .Properties.Name.eq(name)).unique();

        return driveVideo;
    }
    public static DriveVideoQuart getQuartDriveVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoQuart driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().queryBuilder().where(DriveVideoQuartDao
                .Properties.Name.eq(name)).unique();

        return driveVideo;
    }
    public static DriveVideoDual getDualDriveVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoDual driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoDualDao().queryBuilder().where(DriveVideoDualDao
                .Properties.Name.eq(name)).unique();

        return driveVideo;
    }

   /* public static boolean isVideoExist(String name) {
        long count = DvrApplication.getDaoSession().getDriveVideoDao().queryBuilder().where(DriveVideoDao.Properties
                .Name.eq(name)).list().size();
        return count > 0;
    }*/

    /**
     * 根据视频名查询 前置 是否加锁
     *
     * @param name
     * @return
     */
    public static boolean getLockFontStateByVideoName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return false;
        }

        List<DriveVideoFont> list = RecordFileDataBase.getDaoSession().getDriveVideoFontDao().queryBuilder().where(DriveVideoFontDao
                .Properties
                .Name.eq(name)).list();
        if (list.isEmpty()) {
            return false;
        }
        return list.get(0).getLockStatus();
    }

    /**
     * 根据视频名查询 后置 是否加锁
     *
     * @param name
     * @return
     */
    public static boolean getLockBackStateByVideoName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return false;
        }
        List<DriveVideoBack> list = RecordFileDataBase.getDaoSession().getDriveVideoBackDao().queryBuilder().where(DriveVideoBackDao
                .Properties
                .Name.eq(name)).list();
        if (list.isEmpty()) {
            return false;
        }
        return list.get(0).getLockStatus();
    }

    /**
     * 获取所有前置视频信息
     *
     * @return
     */
    public static List<DriveVideoFont> getAllDriveFontVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoFontDao().loadAll();
    }
    /**
     * 获取所有后置视频信息
     *
     * @return
     */
    public static List<DriveVideoBack> getAllDriveBackVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoBackDao().loadAll();
    }
    public static List<DriveVideoLeft> getAllDriveLeftVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().loadAll();
    }
    public static List<DriveVideoRight> getAllDriveRightVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoRightDao().loadAll();
    }
    public static List<DriveVideoQuart> getAllDriveQuartVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().loadAll();
    }
    public static List<DriveVideoDual> getAllDriveDualVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoDualDao().loadAll();
    }

    /**
     * 分页处理
     * @param pageNum 页数
     * @param pageSize 每页多少条
     * @return
     */
    public static List<DriveVideoFont>  getLimitDriveFontVideo(int pageNum,int pageSize) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoFontDao mDriveVideoFontDao =    RecordFileDataBase.getDaoSession().getDriveVideoFontDao();
        int startIndex=0;
        if(pageNum>1){
            startIndex=(pageNum-1)*pageSize;
        }
        List<DriveVideoFont>  list =   mDriveVideoFontDao.queryBuilder().offset(startIndex).limit(pageSize).orderDesc(DriveVideoFontDao.Properties.Name)
                .list();
        return list;
    }
    /**
     * 分页处理
     * @param pageNum 页数
     * @param pageSize 每页多少条
     * @return
     */
    public static List<DriveVideoBack> getLimitDriveBackVideo(int pageNum,int pageSize) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoBackDao mDriveVideoBackDao = RecordFileDataBase.getDaoSession().getDriveVideoBackDao();
        int startIndex=0;
        if(pageNum>1){
            startIndex=(pageNum-1)*pageSize;
        }
        List<DriveVideoBack> list =  mDriveVideoBackDao.queryBuilder().offset(startIndex).limit(pageSize).orderDesc(DriveVideoBackDao.Properties.Name).list();
        return list;
    }
    public static List<DriveVideoLeft>  getLimitDriveLeftVideo(int pageNum,int pageSize) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoLeftDao mDriveVideoLeftDao = RecordFileDataBase.getDaoSession().getDriveVideoLeftDao();
        int startIndex=0;
        if(pageNum>1){
            startIndex=(pageNum-1)*pageSize;
        }
        List<DriveVideoLeft>  list =   mDriveVideoLeftDao.queryBuilder().offset(startIndex).limit(pageSize).orderDesc(DriveVideoLeftDao.Properties.Name)
                .list();
        return list;
    }
    public static List<DriveVideoRight>  getLimitDriveRightVideo(int pageNum,int pageSize) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoRightDao mDriveVideoRightDao =  RecordFileDataBase.getDaoSession().getDriveVideoRightDao();
        int startIndex=0;
        if(pageNum>1){
            startIndex=(pageNum-1)*pageSize;
        }
        List<DriveVideoRight>  list =   mDriveVideoRightDao.queryBuilder().offset(startIndex).limit(pageSize).orderDesc(DriveVideoRightDao.Properties.Name)
                .list();
        return list;
    }
    public static List<DriveVideoQuart>  getLimitDriveQuartVideo(int pageNum,int pageSize) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoQuartDao mDriveVideoQuartDao =  RecordFileDataBase.getDaoSession().getDriveVideoQuartDao();
        int startIndex=0;
        if(pageNum>1){
            startIndex=(pageNum-1)*pageSize;
        }
        List<DriveVideoQuart>  list =   mDriveVideoQuartDao.queryBuilder().offset(startIndex).limit(pageSize).orderDesc(DriveVideoQuartDao.Properties.Name)
                .list();
        return list;
    }
    public static List<DriveVideoDual>  getLimitDriveDualtVideo(int pageNum,int pageSize) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        DriveVideoDualDao mDriveVideoDualDao =  RecordFileDataBase.getDaoSession().getDriveVideoDualDao();
        int startIndex=0;
        if(pageNum>1){
            startIndex=(pageNum-1)*pageSize;
        }
        List<DriveVideoDual>  list =   mDriveVideoDualDao.queryBuilder().offset(startIndex).limit(pageSize).orderDesc(DriveVideoDualDao.Properties.Name)
                .list();
        return list;
    }


    /**
     * 获取所有前置未加锁视频
     *
     * @return
     */
    public static List<DriveVideoFont> getAllUnlockVideoFont() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoFontDao().queryBuilder().where(DriveVideoFontDao.Properties
                .LockStatus.eq(0)).list();
    }
    public static List<DriveVideoBack> getAllUnlockVideoBack() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoBackDao().queryBuilder().where(DriveVideoBackDao.Properties
                .LockStatus.eq(0)).list();
    }
    public static List<DriveVideoLeft> getAllUnlockVideoLeft() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().queryBuilder().where(DriveVideoLeftDao.Properties
                .LockStatus.eq(0)).list();
    }
    public static List<DriveVideoRight> getAllUnlockVideoRight() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoRightDao().queryBuilder().where(DriveVideoRightDao.Properties
                .LockStatus.eq(0)).list();
    }
    public static List<DriveVideoQuart> getAllUnlockVideoQuart() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().queryBuilder().where(DriveVideoQuartDao.Properties
                .LockStatus.eq(0)).list();
    }
    public static List<DriveVideoDual> getAllUnlockVideoDual() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoDualDao().queryBuilder().where(DriveVideoDualDao.Properties
                .LockStatus.eq(0)).list();
    }

    public static List<DriveVideoFont> getAlllockVideoFont() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoFontDao().queryBuilder().where(DriveVideoFontDao.Properties
                .LockStatus.eq(1)).list();
    }
    public static List<DriveVideoBack> getAlllockVideoBack() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoBackDao().queryBuilder().where(DriveVideoBackDao.Properties
                .LockStatus.eq(1)).list();
    }
    public static List<DriveVideoLeft> getAlllockVideoLeft() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().queryBuilder().where(DriveVideoLeftDao.Properties
                .LockStatus.eq(1)).list();
    }
    public static List<DriveVideoRight> getAlllockVideoRight() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoRightDao().queryBuilder().where(DriveVideoRightDao.Properties
                .LockStatus.eq(1)).list();
    }
    public static List<DriveVideoQuart> getAlllockVideoQuart() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().queryBuilder().where(DriveVideoQuartDao.Properties
                .LockStatus.eq(1)).list();
    }
    public static List<DriveVideoDual> getAlllockVideoDual() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }

        return RecordFileDataBase.getDaoSession().getDriveVideoDualDao().queryBuilder().where(DriveVideoDualDao.Properties
                .LockStatus.eq(1)).list();
    }

//    /**
//     * 根据ID升序排序未加锁视频 删除最新视频9倍大小的文件
//     * @param deleteFilelength
//     * @return
//     */
//    public  static List<String>  getOldestUnlockVideoId(long deleteFilelength) {
//        long delete4File = deleteFilelength * 9;
//        long  oldFile  = 0;
//        List<String>  listname = new ArrayList<>();
//
//        List<DriveVideoFont> unlockList = RecordFileDataBase.getDaoSession().getDriveVideoDao().queryBuilder().where
//                (DriveVideoDao.Properties.LockStatus.eq
//                        (0)).orderAsc(DriveVideoDao.Properties.Id).list();
//        if (unlockList.isEmpty()) {
//            return  null;
//        }
//
//        for (int i = 0; i <unlockList.size() ; i++ ){
//            String path = unlockList.get(i).getName();
//            File  file = new File(path);
//            oldFile = oldFile + file.length();
//
//             if (oldFile != delete4File &&  oldFile < delete4File){
//                 String path1 = unlockList.get(i).getName();
//                 listname.add(path1);
//            }
//        }
//     return listname;
//    }

    public static int div(long d1, long d2, int len) {// 进行除法运算,
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2, len, BigDecimal.ROUND_UP).intValue();//ROUND_UP非0时，舍弃小数后（整数部分）加1
    }

    /**
     * 获取前置 最旧且未加锁视频ID
     *
     * @return
     */
    public  static long getFontOldestUnlockVideoId() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return -1;
        }
        List<DriveVideoFont> unlockList = RecordFileDataBase.getDaoSession().getDriveVideoFontDao().queryBuilder().where
                (DriveVideoFontDao.Properties.LockStatus.eq
                        (0)).orderAsc(DriveVideoFontDao.Properties.Id).list();
        if (unlockList.isEmpty()) {
            return -1;
        }
        return unlockList.get(0).getId();
    }

    /**
     * 获取后置 最旧且未加锁视频ID
     *
     * @return
     */
    public  static long getBackOldestUnlockVideoId() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return -1;
        }

        List<DriveVideoBack> unlockList = RecordFileDataBase.getDaoSession().getDriveVideoBackDao().queryBuilder().where
                (DriveVideoBackDao.Properties.LockStatus.eq
                        (0)).orderAsc(DriveVideoBackDao.Properties.Id).list();
        if (unlockList.isEmpty()) {
            return -1;
        }
        return unlockList.get(0).getId();
    }

    /**
     * 获取前置 最旧的一条未加锁视频
     * @return
     */
    public static DriveVideoFont getFontOldestUnlockVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        List<DriveVideoFont> unlockList = RecordFileDataBase.getDaoSession().getDriveVideoFontDao().queryBuilder().where
                (DriveVideoFontDao.Properties.LockStatus.eq
                        (0)).orderAsc(DriveVideoFontDao.Properties.Id).list();//须根据ID排序，避免出现换制式后1080与720的顺序会变。
        if (unlockList.isEmpty()) {
            return null;
        }

        LogUtils.d("--Font----getFontOldestUnlockVideo----unlockList.size()=" + unlockList.size());
        if(unlockList.size() >= 3){
            return unlockList.get(0);
        }
        return null;
    }
    /**
     * 获取后置 最旧的一条未加锁视频
     * @return
     */
    public static DriveVideoBack getBackOldestUnlockVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        List<DriveVideoBack> unlockList = RecordFileDataBase.getDaoSession().getDriveVideoBackDao().queryBuilder().where
                (DriveVideoBackDao.Properties.LockStatus.eq
                        (0)).orderAsc(DriveVideoBackDao.Properties.Id).list();//须根据ID排序，避免出现换制式后1080与720的顺序会变。
        if (unlockList.isEmpty()) {
            return null;
        }

        LogUtils.d("--Back----getBackOldestUnlockVideo----unlockList.size()=" + unlockList.size());
        if(unlockList.size() >= 3){
            return unlockList.get(0);
        }
        return null;
    }
    public static DriveVideoLeft getLeftOldestUnlockVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        List<DriveVideoLeft> unlockList = RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().queryBuilder().where
                (DriveVideoLeftDao.Properties.LockStatus.eq
                        (0)).orderAsc(DriveVideoLeftDao.Properties.Id).list();//须根据ID排序，避免出现换制式后1080与720的顺序会变。
        if (unlockList.isEmpty()) {
            return null;
        }

        LogUtils.d("--Left----getLeftOldestUnlockVideo----unlockList.size()=" + unlockList.size());
        if(unlockList.size() >= 3){
            return unlockList.get(0);
        }
        return null;
    }
    public static DriveVideoRight getRightOldestUnlockVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        List<DriveVideoRight> unlockList = RecordFileDataBase.getDaoSession().getDriveVideoRightDao().queryBuilder().where
                (DriveVideoRightDao.Properties.LockStatus.eq
                        (0)).orderAsc(DriveVideoRightDao.Properties.Id).list();//须根据ID排序，避免出现换制式后1080与720的顺序会变。
        if (unlockList.isEmpty()) {
            return null;
        }

        LogUtils.d("--Right----getRightOldestUnlockVideo----unlockList.size()=" + unlockList.size());
        if(unlockList.size() >= 3){
            return unlockList.get(0);
        }
        return null;
    }
    public static DriveVideoQuart getQuartOldestUnlockVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        List<DriveVideoQuart> unlockList = RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().queryBuilder().where
                (DriveVideoQuartDao.Properties.LockStatus.eq
                        (0)).orderAsc(DriveVideoQuartDao.Properties.Id).list();//须根据ID排序，避免出现换制式后1080与720的顺序会变。
        if (unlockList.isEmpty()) {
            return null;
        }

        LogUtils.d("--Right----getQuartOldestUnlockVideo----unlockList.size()=" + unlockList.size());
        if(unlockList.size() >= 3){
            return unlockList.get(0);
        }
        return null;
    }
    public static DriveVideoDual getDualOldestUnlockVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return null;
        }
        List<DriveVideoDual> unlockList = RecordFileDataBase.getDaoSession().getDriveVideoDualDao().queryBuilder().where
                (DriveVideoDualDao.Properties.LockStatus.eq
                        (0)).orderAsc(DriveVideoDualDao.Properties.Id).list();//须根据ID排序，避免出现换制式后1080与720的顺序会变。
        if (unlockList.isEmpty()) {
            return null;
        }

        LogUtils.d("--Right----getDualOldestUnlockVideo----unlockList.size()=" + unlockList.size());
        if(unlockList.size() >= 3){
            return unlockList.get(0);
        }
        return null;
    }

    public static String getVideNameById(long id) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return "";
        }
        DriveVideoFont driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoFontDao().queryBuilder().where(DriveVideoFontDao
                .Properties
                .Id.eq(id)).unique();
        if (driveVideo == null) {
            return "";
        }
        return driveVideo.getName();
    }

    public static void updateFontDriveVideo(DriveVideoFont mDriveVideoFont) {

        if (RecordFileDataBase.getDaoSession() == null) {
            LogUtils.d("RecordFileDataBase.getDaoSession()====null");
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoFontDao().update(mDriveVideoFont);
    }
    public static void updateBackDriveVideo(DriveVideoBack mDriveVideoBack) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoBackDao().update(mDriveVideoBack);
    }
    public static void updateLeftDriveVideo(DriveVideoLeft mDriveVideoLeft) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().update(mDriveVideoLeft);
    }
    public static void updateRightDriveVideo(DriveVideoRight mDriveVideoRight) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoRightDao().update(mDriveVideoRight);
    }
    public static void updateQuartDriveVideo(DriveVideoQuart mDriveVideo) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().update(mDriveVideo);
    }
    public static void updateDualDriveVideo(DriveVideoDual mDriveVideo) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoDualDao().update(mDriveVideo);
    }

    public static void deleteDriveFontVideoById(long driveVideoId) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoFontDao().deleteByKey(driveVideoId);
    }

    public static void deleteDriveBackVideoById(long driveVideoId) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoBackDao().deleteByKey(driveVideoId);
    }

    public static void deleteDriveFontVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }

        List<DriveVideoFont> driveVideoList = RecordFileDataBase.getDaoSession().getDriveVideoFontDao().queryBuilder().where(DriveVideoFontDao
                .Properties.Name.eq(name)).list();
        if (driveVideoList != null && driveVideoList.size() > 0) {//解决获取数据库记录2个或以上时会报错。2020-06-28
            LogUtils.d("deleteDriveLeft---size=" + driveVideoList.size());
            for (DriveVideoFont driveVideo : driveVideoList) {
                RecordFileDataBase.getDaoSession().getDriveVideoFontDao().delete(driveVideo);
            }
        } else {
            DriveVideoFont driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoFontDao().queryBuilder().where(DriveVideoFontDao
                    .Properties.Name.eq(name)).unique();
            if (driveVideo != null) {
                RecordFileDataBase.getDaoSession().getDriveVideoFontDao().delete(driveVideo);
            }
        }
    }
    public static void deleteDriveBackVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }

        List<DriveVideoBack> driveVideoList = RecordFileDataBase.getDaoSession().getDriveVideoBackDao().queryBuilder().where(DriveVideoBackDao
                .Properties.Name.eq(name)).list();
        if (driveVideoList != null && driveVideoList.size() > 0) {
            LogUtils.d("deleteDriveBack----size=" + driveVideoList.size());
            for (DriveVideoBack driveVideo : driveVideoList) {
                RecordFileDataBase.getDaoSession().getDriveVideoBackDao().delete(driveVideo);
            }
        } else {
            DriveVideoBack driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoBackDao().queryBuilder().where(DriveVideoBackDao
                    .Properties.Name.eq(name)).unique();
            if (driveVideo != null) {
                RecordFileDataBase.getDaoSession().getDriveVideoBackDao().delete(driveVideo);
            }
        }
    }
    public static void deleteDriveLeftVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }

        List<DriveVideoLeft> driveVideoList = RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().queryBuilder().where(DriveVideoLeftDao
                .Properties.Name.eq(name)).list();
        if (driveVideoList != null && driveVideoList.size() > 0) {
            LogUtils.d("deleteDriveLeft---size=" + driveVideoList.size());
            for (DriveVideoLeft driveVideo : driveVideoList) {
                RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().delete(driveVideo);
            }
        } else {
            DriveVideoLeft driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().queryBuilder().where(DriveVideoLeftDao
                    .Properties.Name.eq(name)).unique();
            if (driveVideo != null) {
                RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().delete(driveVideo);
            }
        }
    }
    public static void deleteDriveRightVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }

        List<DriveVideoRight> driveVideoList = RecordFileDataBase.getDaoSession().getDriveVideoRightDao().queryBuilder().where(DriveVideoRightDao
                .Properties.Name.eq(name)).list();
        if (driveVideoList != null && driveVideoList.size() > 0) {
            LogUtils.d("deleteDriveRight---size=" + driveVideoList.size());
            for (DriveVideoRight driveVideo : driveVideoList) {
                RecordFileDataBase.getDaoSession().getDriveVideoRightDao().delete(driveVideo);
            }
        } else {
            DriveVideoRight driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoRightDao().queryBuilder().where(DriveVideoRightDao
                    .Properties.Name.eq(name)).unique();
            if (driveVideo != null) {
                RecordFileDataBase.getDaoSession().getDriveVideoRightDao().delete(driveVideo);
            }
        }
    }
    public static void deleteDriveQuartVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }

        List<DriveVideoQuart> driveVideoList = RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().queryBuilder().where(DriveVideoQuartDao
                .Properties.Name.eq(name)).list();
        if (driveVideoList != null && driveVideoList.size() > 0) {
            LogUtils.d("deleteDriveQuart---size=" + driveVideoList.size());
            for (DriveVideoQuart driveVideo : driveVideoList) {
                RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().delete(driveVideo);
            }
        } else {
            DriveVideoQuart driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().queryBuilder().where(DriveVideoQuartDao
                    .Properties.Name.eq(name)).unique();
            if (driveVideo != null) {
                RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().delete(driveVideo);
            }
        }
    }
    public static void deleteDriveDualVideoByName(String name) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }

        List<DriveVideoDual> driveVideoList = RecordFileDataBase.getDaoSession().getDriveVideoDualDao().queryBuilder().where(DriveVideoDualDao
                .Properties.Name.eq(name)).list();
        if (driveVideoList != null && driveVideoList.size() > 0) {
            LogUtils.d("deleteDriveQuart---size=" + driveVideoList.size());
            for (DriveVideoDual driveVideo : driveVideoList) {
                RecordFileDataBase.getDaoSession().getDriveVideoDualDao().delete(driveVideo);
            }
        } else {
            DriveVideoDual driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoDualDao().queryBuilder().where(DriveVideoDualDao
                    .Properties.Name.eq(name)).unique();
            if (driveVideo != null) {
                RecordFileDataBase.getDaoSession().getDriveVideoDualDao().delete(driveVideo);
            }
        }
    }

    public static void deleteDriveFontVideoByListName(List<String> listname) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        for (String name:listname) {
            DriveVideoFont driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoFontDao().queryBuilder().where(DriveVideoFontDao
                    .Properties.Name.eq(name)).unique();
            if (driveVideo != null) {
                RecordFileDataBase.getDaoSession().getDriveVideoFontDao().delete(driveVideo);
            }
        }
    }

    public static void deleteDriveBackVideoByListName(List<String> listname) {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        for (String name:listname) {
            DriveVideoBack driveVideo = RecordFileDataBase.getDaoSession().getDriveVideoBackDao().queryBuilder().where(DriveVideoBackDao
                    .Properties.Name.eq(name)).unique();
            if (driveVideo != null) {
                RecordFileDataBase.getDaoSession().getDriveVideoBackDao().delete(driveVideo);
            }
        }
    }

    public static void deleteAllDriveVideo() {
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_QUART_STREAM.equals(curRecordType)) {
            RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().deleteAll();
        } else if (Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
            RecordFileDataBase.getDaoSession().getDriveVideoDualDao().deleteAll();
        } else {
            RecordFileDataBase.getDaoSession().getDriveVideoFontDao().deleteAll();

            if (!Config.TYPE_ONE_STREAM.equals(curRecordType)) {//不是单录时
                RecordFileDataBase.getDaoSession().getDriveVideoBackDao().deleteAll();

                if (!Config.TYPE_TWO_STREAM.equals(curRecordType)) {//不是双录时
                    RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().deleteAll();
                    RecordFileDataBase.getDaoSession().getDriveVideoRightDao().deleteAll();
                }
            }
        }
    }

    public static void deleteAllFontVideo(){
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoFontDao().deleteAll();
    }
    public static void deleteAllBackVideo(){
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoBackDao().deleteAll();
    }
    public static void deleteAllLeftVideo(){
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoLeftDao().deleteAll();
    }
    public static void deleteAllRightVideo(){
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoRightDao().deleteAll();
    }
    public static void deleteAllQuartVideo(){
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoQuartDao().deleteAll();
    }
    public static void deleteAllDualVideo(){
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }
        RecordFileDataBase.getDaoSession().getDriveVideoDualDao().deleteAll();
    }

    public static boolean deleteOldestVideoFile(){
        boolean ret = false;
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_QUART_STREAM.equals(curRecordType)) {
            DriveVideoQuart quart = DBUtil.getQuartOldestUnlockVideo();
            if (quart != null) {
                if (FileUtils.fileIsExists(quart.getName())) {
                    boolean b_ret = FileUtils.deleteFile(quart.getName());
                    if (b_ret) {
                        DBUtil.deleteDriveQuartVideoByName(quart.getName());
                        ret = true;
                    }
                } else {
                    LogUtils.e("---22---quart--name=" + quart.getName() + ",,文件不存在，清理数据库，继续...");
                    DBUtil.deleteDriveQuartVideoByName(quart.getName());
                }
            }
        } else if (Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
            DriveVideoDual dual = DBUtil.getDualOldestUnlockVideo();
            if (dual != null) {
                if (FileUtils.fileIsExists(dual.getName())) {
                    boolean b_ret = FileUtils.deleteFile(dual.getName());
                    if (b_ret) {
                        DBUtil.deleteDriveDualVideoByName(dual.getName());
                        ret = true;
                    }
                } else {
                    LogUtils.e("---22---dual--name=" + dual.getName() + ",,文件不存在，清理数据库，继续...");
                    DBUtil.deleteDriveDualVideoByName(dual.getName());
                }
            }
        } else {
            DriveVideoFont font = DBUtil.getFontOldestUnlockVideo();
            if (font != null) {
                if (FileUtils.fileIsExists(font.getName())) {
                    boolean f_ret = FileUtils.deleteFile(font.getName());
                    if (f_ret) {
                        DBUtil.deleteDriveFontVideoByName(font.getName());
                        ret = true;
                    }
                } else {
                    LogUtils.e("-----Font--name=" + font.getName() + ",,文件不存在，清理数据库，继续...");
                    DBUtil.deleteDriveFontVideoByName(font.getName());
                }
            }

            if (!Config.TYPE_ONE_STREAM.equals(curRecordType)) {//不是单录时
                DriveVideoBack back = DBUtil.getBackOldestUnlockVideo();
                if (back != null) {
                    if (FileUtils.fileIsExists(back.getName())) {
                        boolean b_ret = FileUtils.deleteFile(back.getName());
                        if (b_ret) {
                            DBUtil.deleteDriveBackVideoByName(back.getName());
                            ret = true;
                        }
                    } else {
                        LogUtils.e("---22---checkRemainSpace--Back--name=" + back.getName() + ",,文件不存在，清理数据库，继续...");
                        DBUtil.deleteDriveBackVideoByName(back.getName());
                    }
                }

                if (!Config.TYPE_TWO_STREAM.equals(curRecordType)) {//不是双录时
                    DriveVideoLeft left = DBUtil.getLeftOldestUnlockVideo();
                    if (left != null) {
                        if (FileUtils.fileIsExists(left.getName())) {
                            boolean b_ret = FileUtils.deleteFile(left.getName());
                            if (b_ret) {
                                DBUtil.deleteDriveLeftVideoByName(left.getName());
                                ret = true;
                            }
                        } else {
                            LogUtils.e("---22---checkRemainSpace--left--name=" + left.getName() + ",,文件不存在，清理数据库，继续...");
                            DBUtil.deleteDriveLeftVideoByName(left.getName());
                        }
                    }

                    DriveVideoRight right = DBUtil.getRightOldestUnlockVideo();
                    if (right != null) {
                        if (FileUtils.fileIsExists(right.getName())) {
                            boolean b_ret = FileUtils.deleteFile(right.getName());
                            if (b_ret) {
                                DBUtil.deleteDriveRightVideoByName(right.getName());
                                ret = true;
                            }
                        } else {
                            LogUtils.e("---22---right--name=" + right.getName() + ",,文件不存在，清理数据库，继续...");
                            DBUtil.deleteDriveRightVideoByName(right.getName());
                        }
                    }
                }
            }
        }

       return ret;
    }

    /**
     * 一键删除未加锁
     */
    public static void deleteUnlockVideo(){
        if (RecordFileDataBase.getDaoSession() == null) {
            return;
        }

        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_QUART_STREAM.equals(curRecordType)) {
            List<DriveVideoQuart> quartList = getAllUnlockVideoQuart();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (DriveVideoQuart quart : quartList) {
//                        deleteDriveQuartVideoByName(quart.getName());
                        FileUtils.deleteFile(quart.getName());
                    }
                }
            }).start();
        } else if (Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
            List<DriveVideoDual> dualList = getAllUnlockVideoDual();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (DriveVideoDual dual : dualList) {
//                        deleteDriveQuartVideoByName(quart.getName());
                        FileUtils.deleteFile(dual.getName());
                    }
                }
            }).start();
        } else {
            List<DriveVideoFont> fontList = getAllUnlockVideoFont();
            List<DriveVideoBack> backList = getAllUnlockVideoBack();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (DriveVideoFont font : fontList) {
//                        deleteDriveFontVideoByName(font.getName());
                        FileUtils.deleteFile(font.getName());
                    }

                    if (!Config.TYPE_ONE_STREAM.equals(curRecordType)) {//不是单录时
                        for (DriveVideoBack back : backList) {
//                        deleteDriveBackVideoByName(back.getName());
                            FileUtils.deleteFile(back.getName());
                        }

                        String tmpRecordType = RecordData.getInstance().recordType.getValue();
                        if (!Config.TYPE_TWO_STREAM.equals(tmpRecordType)) {//不是双录时
                            List<DriveVideoLeft> leftList = getAllUnlockVideoLeft();
                            List<DriveVideoRight> rightList = getAllUnlockVideoRight();
                            for (DriveVideoLeft left : leftList) {
//                        deleteDriveLeftVideoByName(left.getName());
                                FileUtils.deleteFile(left.getName());
                            }
                            for (DriveVideoRight right : rightList) {
//                        deleteDriveRightVideoByName(right.getName());
                                FileUtils.deleteFile(right.getName());
                            }
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * 一键删除加锁文件
     */
    public static void deletelockVideo(){
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_QUART_STREAM.equals(curRecordType)) {
            List<DriveVideoQuart> quarts = getAlllockVideoQuart();
            for (DriveVideoQuart quart : quarts) {
//                deleteDriveQuartVideoByName(quart.getName());
                FileUtils.deleteFile(quart.getName());
            }
        } else if (Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
            List<DriveVideoDual> duals = getAlllockVideoDual();
            for (DriveVideoDual dual : duals) {
//                deleteDriveDualVideoByName(dual.getName());
                FileUtils.deleteFile(dual.getName());
            }
        } else {
            List<DriveVideoBack> backs = getAlllockVideoBack();
            List<DriveVideoFont> fonts = getAlllockVideoFont();
            for (DriveVideoFont font : fonts) {
//                LogUtils.d("WPTAG", "------------删除已加锁-----font-------" + font.getName());
//                deleteDriveFontVideoByName(font.getName());
                FileUtils.deleteFile(font.getName());
            }

            if (!Config.TYPE_ONE_STREAM.equals(curRecordType)) {//不是单录时
                for (DriveVideoBack back : backs) {
//                LogUtils.d("WPTAG", "------------删除已加锁-----back-------" + back.getName());
//                deleteDriveFontVideoByName(back.getName());
                    FileUtils.deleteFile(back.getName());
                }

                if (!Config.TYPE_TWO_STREAM.equals(curRecordType)) {
                    List<DriveVideoLeft> lefts = getAlllockVideoLeft();
                    List<DriveVideoRight> rights = getAlllockVideoRight();
                    for (DriveVideoLeft left : lefts) {
//                LogUtils.d("WPTAG", "------------删除已加锁-----font-------" + font.getName());
//                deleteDriveLeftVideoByName(left.getName());
                        FileUtils.deleteFile(left.getName());
                    }
                    for (DriveVideoRight right : rights) {
//                LogUtils.d("WPTAG", "------------删除已加锁-----font-------" + font.getName());
//                deleteDriveRightVideoByName(right.getName());
                        FileUtils.deleteFile(right.getName());
                    }
                }
            }
        }
    }
}

