package com.wwc2.dvr.fourCamera;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.utils.Utils;

public class CameraBean {
    private String TAG = "CameraBean";
    private String node;
    private int mode;
    private int action;

 public CameraBean(String node, int mode, int action){
     this.node = node;
     this.mode = mode;
     this.action = action;
 }

 public void Action(){
     String value = mode + " " + action;
     Utils.writeTextFile(value, node);
     LogUtils.d(TAG,"set camera action =" + value);
 }

}
