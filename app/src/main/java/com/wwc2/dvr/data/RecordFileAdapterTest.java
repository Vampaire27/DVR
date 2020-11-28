package com.wwc2.dvr.data;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.wwc2.dvr.R;
import com.wwc2.dvr.base.ListBaseAdapter;
import com.wwc2.dvr.base.SuperViewHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  user: wangpeng on 2020/03/28.
 *  emai: wpeng@waterworld.com.cn
 */
public class RecordFileAdapterTest extends ListBaseAdapter<DriveVideo> {
    private boolean isSelect = false;
    private boolean isImg =false;
    private int typeflag;

    //是否选择的存储集合,key 是 position , value 是该position是否选中
    public Map<Integer, Boolean> isCheckMap = new HashMap<>();

    public RecordFileAdapterTest(Context context, boolean isImg, int typeflag) {
        super(context);
        this.isImg = isImg;
        this.typeflag =typeflag;
    }

    @Override
    public int getLayoutId() {
        return R.layout.record_item;
    }

    @Override
    public void onBindItemHolder(SuperViewHolder holder, int position) {
        DriveVideo driveVideo = mDataList.get(position);
        ImageView  mVideoPlayImg = holder.getView(R.id.videoPlayImg);
        CheckBox mCheckbox = holder.getView(R.id.checkbox);
        TextView  mVideoName =  holder.getView(R.id.tv_videoName);
        ImageView  mLock =   holder.getView(R.id.iv_lock);

        String absluteName = driveVideo.getName();
//  /storage/emulated/0/recordVideo/main/main_1080P_2019-07-01_00-09-58.mp4
        String name = absluteName.substring(absluteName.lastIndexOf("/")).replaceAll("/","");

        //设置显示播放图标或选择图标
        if(isSelect){
            mCheckbox.setVisibility(View.VISIBLE);
            mVideoPlayImg.setVisibility(View.GONE);
        }else{
            mCheckbox.setVisibility(View.GONE);
            mVideoPlayImg.setVisibility(View.VISIBLE);
        }

        //设置单选按钮的选中
        mCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //将选择项加载到map里面寄存
                isCheckMap.put(position, isChecked);
            }
        });

        //初始化选择框状态
        if (isCheckMap.get(position) == null) {
            isCheckMap.put(position, false);
        }
        mCheckbox.setChecked(isCheckMap.get(position));

        if (isImg == true){
            mVideoPlayImg.setBackgroundResource(R.mipmap.card_photo);
        }else {
            mVideoPlayImg.setBackgroundResource(R.mipmap.icon_video);
        }

        //设置列表文件名及加锁图标
        boolean lock = driveVideo.getLockStatus();
        if (null != name && !TextUtils.isEmpty(name)) {
            mVideoName.setText(name);
            mLock.setVisibility(lock ? View.VISIBLE : View.INVISIBLE);
        } else {
            if (typeflag == ConstantsData.TYPE_FRONT) {
                DBUtil.deleteDriveFontVideoByName(absluteName);
            } else if (typeflag == ConstantsData.TYPE_BACK) {
                DBUtil.deleteDriveBackVideoByName(absluteName);
            } else if (typeflag == ConstantsData.TYPE_LEFT) {
                DBUtil.deleteDriveLeftVideoByName(absluteName);
            } else if (typeflag == ConstantsData.TYPE_RIGHT) {
                DBUtil.deleteDriveRightVideoByName(absluteName);
            } else if (typeflag == ConstantsData.TYPE_QUART) {
                DBUtil.deleteDriveQuartVideoByName(absluteName);
            }
        }
    }

    public void setIsImag(boolean isImg){
        this.isImg = isImg;
    }

    public boolean getIsImag(){
        return isImg;
    }

    public void setTypeflag(int  i){
        typeflag = i;
    }

    public int  getTypeflag(){
       return typeflag ;
    }

    /**
     * 是否处于选择列表界面
     * @param isSelect
     */
    public void selectImgState(boolean isSelect){
        this.isSelect = isSelect;
    }
}
