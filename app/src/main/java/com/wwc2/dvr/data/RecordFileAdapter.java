package com.wwc2.dvr.data;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.wwc2.dvr.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecordFileAdapter extends BaseAdapter {
    private Context context;
    private List<DriveVideo> list;
    private boolean isSelect = false;
    private boolean isImg =false;
    private int typeflag;

    //是否选择的存储集合,key 是 position , value 是该position是否选中
    public Map<Integer, Boolean> isCheckMap = new HashMap<>();

    public RecordFileAdapter(Context context, List<DriveVideo> fileArrayList, boolean isImg,int typeflag) {
        this.context = context;
        this.list = fileArrayList;
        this.isImg = isImg;
        this.typeflag =typeflag;
    }

    /**
     * 更新列表数据
     * @param fileArrayList
     */
    public void updateListData(List<DriveVideo> fileArrayList){
        this.list = fileArrayList;
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

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(com.wwc2.dvr.R.layout.record_item, null, false);
            viewHolder = new ViewHolder();
            viewHolder.mVideoPlayImg = convertView.findViewById(com.wwc2.dvr.R.id.videoPlayImg);
            viewHolder.mCheckbox = convertView.findViewById(com.wwc2.dvr.R.id.checkbox);
            viewHolder.mVideoName = convertView.findViewById(com.wwc2.dvr.R.id.tv_videoName);
            viewHolder.mLock =  convertView.findViewById(com.wwc2.dvr.R.id.iv_lock);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DriveVideo driveVideo = list.get(position);
        String absluteName = driveVideo.getName();
//  /storage/emulated/0/recordVideo/main/main_1080P_2019-07-01_00-09-58.mp4
        String name = absluteName.substring(absluteName.lastIndexOf("/")).replaceAll("/","");


        //设置显示播放图标或选择图标
        if(isSelect){
            viewHolder.mCheckbox.setVisibility(View.VISIBLE);
            viewHolder.mVideoPlayImg.setVisibility(View.GONE);
        }else{
            viewHolder.mCheckbox.setVisibility(View.GONE);
            viewHolder.mVideoPlayImg.setVisibility(View.VISIBLE);
        }

        //设置单选按钮的选中
        viewHolder.mCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
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
        viewHolder.mCheckbox.setChecked(isCheckMap.get(position));

        if (isImg == true){
            viewHolder.mVideoPlayImg.setBackgroundResource(R.mipmap.card_photo);
        }else {
            viewHolder.mVideoPlayImg.setBackgroundResource(R.mipmap.icon_video);
        }

        //设置列表文件名及加锁图标
        boolean lock = driveVideo.getLockStatus();
        if (null != name && !TextUtils.isEmpty(name)) {
            viewHolder.mVideoName.setText(name);
            viewHolder.mLock.setVisibility(lock ? View.VISIBLE : View.INVISIBLE);
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
        return convertView;
    }

    public static class ViewHolder {
        public ImageView mVideoPlayImg;
        public CheckBox mCheckbox;
        public TextView mVideoName;
        public ImageView mLock;
    }
}
