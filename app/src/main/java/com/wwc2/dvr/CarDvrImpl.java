package com.wwc2.dvr;

import android.content.Context;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.bean.DriveVideoBean;
import com.wwc2.dvr.bean.ResultDriveVideoBean;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.DBUtil;
import com.wwc2.dvr.data.DriveVideoBack;
import com.wwc2.dvr.data.DriveVideoDual;
import com.wwc2.dvr.data.DriveVideoFont;
import com.wwc2.dvr.data.DriveVideoLeft;
import com.wwc2.dvr.data.DriveVideoQuart;
import com.wwc2.dvr.data.DriveVideoRight;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huwei on 19-3-4.
 */
public class CarDvrImpl extends IWCarDvr.Stub {
    private RecordService recordService;
    private Context mContext;

    public CarDvrImpl(RecordService recordService, Context context) {
        this.recordService = recordService;
        this.mContext = context;
    }

    @Override
    public void createDvr() throws RemoteException {
        recordService.createDvr();
    }

    @Override
    public boolean getDvrStatus(int id) throws RemoteException {
        LogUtils.d("CarDvrImpl---getDvrStatus-->");
        return false;
    }

    @Override
    public boolean stopDvr() throws RemoteException {
        LogUtils.d("CarDvrImpl---stopDvr-->");
        recordService.stopRecording();
        return true;
    }

    @Override
    public String takePicture(boolean isOff, int channel) throws RemoteException {
        LogUtils.d("CarDvrImpl---takePicture  isOff-->" + isOff);
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_DUAL_STREAM.equals(curRecordType) || Config.TYPE_TWO_STREAM.equals(curRecordType)) {
            if (channel == Config.DISABLE_CAPTURE) {
                channel = Config.TWO_CAPTURE;
            }
        }
        return recordService.takePicture(isOff,channel, true);
    }

    @Override
    public String getAllDriveVideo(int fromType ,int pageNum , int pageSize) throws RemoteException {
        LogUtils.d("CarDvrImpl---getAllDriveFontVideo fromType=" + fromType + ", pageNum-->" + pageNum + "    pageSize-->" + pageSize);
        int location = RecordData.getInstance().mutableLocation;
        if (!FileUtils.isDiskMounted(mContext, StorageDevice.getPath(location))) {
            LogUtils.e("CarDvrImpl---getAllDriveFontVideo return device is not exist!");
            return null;
        }

        Gson gson=new Gson();
        if (fromType ==1){
            List<DriveVideoFont> list =  DBUtil.getAllDriveFontVideo();
            List<DriveVideoFont> limitList =  DBUtil.getLimitDriveFontVideo(pageNum,pageSize);

            if (limitList == null || list == null) {
                return null;
            }

            ResultDriveVideoBean fontVideoBean = new ResultDriveVideoBean();
            List<DriveVideoBean> mDriveVideoBeanList = new ArrayList<>();


                for (DriveVideoFont font:limitList){
                    DriveVideoBean mDriveVideoBean =new DriveVideoBean();
                    mDriveVideoBean.setLockStatus(font.getLockStatus());
                    mDriveVideoBean.setUrl(font.getName());
                    mDriveVideoBean.setName( font.getName().substring(font.getName().lastIndexOf("/")).replaceAll("/",""));
                    mDriveVideoBeanList.add(mDriveVideoBean);
                }
                fontVideoBean.setTotal(list.size());
                fontVideoBean.setList(mDriveVideoBeanList);

              String fontJson =  gson.toJson(fontVideoBean);
                LogUtils.d("CarDvrImpl---getAllDriveFontVideo  fontJson-->" + fontJson);
                return fontJson;

        }else if (fromType ==2){
            List<DriveVideoBack> limitList =  DBUtil.getLimitDriveBackVideo(pageNum,pageSize);
                ResultDriveVideoBean backVideoBean = new ResultDriveVideoBean();
                List<DriveVideoBean> mDriveVideoBeanList = new ArrayList<>();

                List<DriveVideoBack> list =  DBUtil.getAllDriveBackVideo();

            if (limitList == null || list == null) {
                return null;
            }

                for (DriveVideoBack back:limitList){
                    DriveVideoBean mDriveVideoBean =new DriveVideoBean();
                    mDriveVideoBean.setLockStatus(back.getLockStatus());
                    mDriveVideoBean.setUrl(back.getName());
                    mDriveVideoBean.setName( back.getName().substring(back.getName().lastIndexOf("/")).replaceAll("/",""));
                    mDriveVideoBeanList.add(mDriveVideoBean);
                }
                backVideoBean.setTotal(list.size());
                backVideoBean.setList(mDriveVideoBeanList);

                String backJson =  gson.toJson(backVideoBean);
                LogUtils.d("CarDvrImpl---getAllDriveBackVideo  backJson-->" + backJson);

                return backJson;

        }else if (fromType == 3){
            List<DriveVideoLeft> limitList =  DBUtil.getLimitDriveLeftVideo(pageNum,pageSize);
            ResultDriveVideoBean VideoBean = new ResultDriveVideoBean();
            List<DriveVideoBean> mDriveVideoBeanList = new ArrayList<>();

            List<DriveVideoLeft> list =  DBUtil.getAllDriveLeftVideo();
            if (limitList == null || list == null) {
                return null;
            }

            for (DriveVideoLeft left:limitList){
                DriveVideoBean mDriveVideoBean =new DriveVideoBean();
                mDriveVideoBean.setLockStatus(left.getLockStatus());
                mDriveVideoBean.setUrl(left.getName());
                mDriveVideoBean.setName( left.getName().substring(left.getName().lastIndexOf("/")).replaceAll("/",""));
                mDriveVideoBeanList.add(mDriveVideoBean);
            }
            VideoBean.setTotal(list.size());
            VideoBean.setList(mDriveVideoBeanList);

            String backJson =  gson.toJson(VideoBean);
            LogUtils.d("CarDvrImpl---getLimitDriveLeftVideo  backJson-->" + backJson);

            return backJson;

        }else if (fromType == 4){
            List<DriveVideoRight> limitList =  DBUtil.getLimitDriveRightVideo(pageNum,pageSize);
            ResultDriveVideoBean VideoBean = new ResultDriveVideoBean();
            List<DriveVideoBean> mDriveVideoBeanList = new ArrayList<>();
            List<DriveVideoRight> list =  DBUtil.getAllDriveRightVideo();

            if (limitList == null || list == null) {
             return null;
            }

            for (DriveVideoRight right:limitList){
                DriveVideoBean mDriveVideoBean =new DriveVideoBean();
                mDriveVideoBean.setLockStatus(right.getLockStatus());
                mDriveVideoBean.setUrl(right.getName());
                mDriveVideoBean.setName( right.getName().substring(right.getName().lastIndexOf("/")).replaceAll("/",""));
                mDriveVideoBeanList.add(mDriveVideoBean);
            }

            VideoBean.setTotal(list.size());
            VideoBean.setList(mDriveVideoBeanList);

            String backJson =  gson.toJson(VideoBean);
            LogUtils.d("CarDvrImpl---getLimitDriveRightVideo  backJson-->" + backJson);
            return backJson;
         }else if (fromType == 5){
            List<DriveVideoQuart> limitList =  DBUtil.getLimitDriveQuartVideo(pageNum,pageSize);
            ResultDriveVideoBean VideoBean = new ResultDriveVideoBean();
            List<DriveVideoBean> mDriveVideoBeanList = new ArrayList<>();
            List<DriveVideoQuart> list =  DBUtil.getAllDriveQuartVideo();

            if (limitList == null || list == null) {
                return null;
            }

            for (DriveVideoQuart quart:limitList){
                DriveVideoBean mDriveVideoBean =new DriveVideoBean();
                mDriveVideoBean.setLockStatus(quart.getLockStatus());
                mDriveVideoBean.setUrl(quart.getName());
                mDriveVideoBean.setName( quart.getName().substring(quart.getName().lastIndexOf("/")).replaceAll("/",""));
                mDriveVideoBeanList.add(mDriveVideoBean);
            }

            VideoBean.setTotal(list.size());
            VideoBean.setList(mDriveVideoBeanList);

            String backJson =  gson.toJson(VideoBean);
            LogUtils.d("CarDvrImpl---getLimitDriveQuartVideo  backJson-->" + backJson);
            return backJson;
        } else if (fromType == 7) {
            List<DriveVideoDual> limitList =  DBUtil.getLimitDriveDualtVideo(pageNum,pageSize);
            ResultDriveVideoBean VideoBean = new ResultDriveVideoBean();
            List<DriveVideoBean> mDriveVideoBeanList = new ArrayList<>();
            List<DriveVideoDual> list =  DBUtil.getAllDriveDualVideo();

            if (limitList == null || list == null) {
                return null;
            }

            for (DriveVideoDual dual:limitList){
                DriveVideoBean mDriveVideoBean =new DriveVideoBean();
                mDriveVideoBean.setLockStatus(dual.getLockStatus());
                mDriveVideoBean.setUrl(dual.getName());
                mDriveVideoBean.setName( dual.getName().substring(dual.getName().lastIndexOf("/")).replaceAll("/",""));
                mDriveVideoBeanList.add(mDriveVideoBean);
            }

            VideoBean.setTotal(list.size());
            VideoBean.setList(mDriveVideoBeanList);

            String backJson =  gson.toJson(VideoBean);
            LogUtils.d("CarDvrImpl---getLimitDriveQuartVideo  dualJson-->" + backJson);
            return backJson;
        }
        return null;
    }

    @Override
    public void close() throws RemoteException {
        recordService.doclose();
    }

    @Override
    public int getTakeCameraStatus() throws RemoteException {
        return recordService.getTakeCameraStatus();
    }

  /*  @Override
    public boolean canTakeVideo(int channel) throws RemoteException {
        return recordService.canTakeVideo(channel);
    }

    @Override
    public boolean canTakePickture(int channel) throws RemoteException {

        return recordService.canTakePickture(channel);
    }*/

    @Override
    public String postSensor(boolean isOff, boolean syncStatus) throws RemoteException {
        return recordService.postSensor(isOff, syncStatus);
    }

    @Override
    public void updateSensor() throws RemoteException {
        recordService.updateSensor();
    }

    @Override
    public void setH264Mode(int mode) throws RemoteException {
        recordService.setH264StreamMode(mode);
    }

    @Override
    public void deleteFile(String path, DeleteVideoCallBack callBack) throws RemoteException {
        recordService.deleteFileFromNetwork(path, callBack);
    }
}
