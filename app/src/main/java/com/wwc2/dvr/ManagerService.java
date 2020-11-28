package com.wwc2.dvr;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.bean.CommonBean;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.DBUtil;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.utils.SPUtils;

import java.util.ArrayList;

/**
 * 任务管理类
 * user: wangpeng on 2020/4/29.
 * emai: wpeng@waterworld.com.cn
 */
public class ManagerService extends IntentService {
    public static final String TASK_KEY = "task_key";
    public static final String TASK_VALUE = "task_value";
    public static final String TASK_EXTRA = "task_extra";
    public static final int TASK_DELETA_FILE = 1;//一键删除任务
    public static final int TASK_SENSOR_COLSE = 2;//碰撞体验模式关闭任务

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

        }
    };

    public ManagerService() {
        super("ManagerService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            processOnHandleIntent(intent);
        }
    }

    private void processOnHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;
        int task = bundle.getInt(TASK_KEY);
        switch (task) {
            case TASK_DELETA_FILE:
//                LogUtils.d("WPTAG", "------------processOnHandleIntent------------");
                ArrayList<CommonBean> mlist = bundle.getParcelableArrayList(TASK_EXTRA);
                for (CommonBean bean : mlist) {
                    LogUtils.d("WPTAG", "index :" + bean.getIndex() + " ischeck:" + bean.isCheck());
                    //bean.getIndex() 1:删除未加锁 2：删除已加锁
                    if(bean.isCheck()){
                        if (bean.getIndex() == 1) {
                            LogUtils.d("WPTAG", "------------删除未加锁------------");
                            DBUtil.deleteUnlockVideo();
                        } else if (bean.getIndex() == 2) {
                            LogUtils.d("WPTAG", "------------删除已加锁------------");
                            DBUtil.deletelockVideo();
                        }
                    }
                }
                break;
            case TASK_SENSOR_COLSE :
                LogUtils.d("WPTAG","TASK_SENSOR_COLSE 开始计时");
                if (runnable!=null){
                    handler.removeCallbacks(runnable);
                }
                handler.postDelayed(runnable,1000*60*5);//五分钟后恢复中等默认碰撞设置
                break;
        }
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            LogUtils.d("WPTAG","TASK_SENSOR_COLSE 时间到，开始重置为默认碰撞值");
//            RecordData.getInstance().testSensor.setValue(false);
//            SPUtils.setTestSensor(DvrApplication.getContext(), false);
//
//            RecordData.getInstance().sensor.postValue(Config.SENSOR_KEY_2);
//            SPUtils.setSensor(DvrApplication.getContext(), Config.SENSOR_KEY_2);
//
//            Intent intent = new Intent(Config.SENSOR_TYPE);
//            intent.putExtra(Config.SENSOR_TYPE_VALUE, 2);
//            sendBroadcast(intent);
        }
    };
}
