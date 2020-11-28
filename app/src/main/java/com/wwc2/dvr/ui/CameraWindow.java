package com.wwc2.dvr.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.R;
import com.wwc2.dvr.data.CameraTypeAdapter;
import com.wwc2.dvr.data.CameraTypeData;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.widget.CustomPopWindow;

import java.util.ArrayList;
import java.util.List;

public class CameraWindow implements PopupWindow.OnDismissListener, AdapterView.OnItemClickListener {
    private Context context;
    private View contentView;
    private CustomPopWindow popWindow;
    private List<CameraTypeData> camera_value;
    private ListView listView;
    private CameraTypeAdapter mAdapter;
    private OnCameraDismissListener listener;

    public CameraWindow(Context context, OnCameraDismissListener settingDismissListener) {
        this.context = context;
        this.listener = settingDismissListener;
    }

    public void show(View parent) {
        if (contentView == null) {
            contentView = View.inflate(context, R.layout.record_camera, null);
        }
        initView(contentView);
        initListViewData();
        CustomPopWindow.PopupWindowBuilder builder = new CustomPopWindow.PopupWindowBuilder(context);
        popWindow = builder.setView(contentView)
                .size(650, 450)
                .setAnimationStyle(R.style.anim_set)
                .setTouchable(true)
                .setFocusable(true)
                .setOnDissmissListener(this)
                .setBackgroundDrawable(context.getResources().getDrawable(
                        R.mipmap.bg_file, context.getTheme()))
                .create()
                .showAtLocation(parent, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void initView(View view){
        if(view != null) {
            //数据列表
            listView = view.findViewById(R.id.camera_list);
            listView.setOnItemClickListener(CameraWindow.this);
        }
        camera_value = new ArrayList<>();

    }

    private void initListViewData(){
        if(context != null) {
            //初始化适配器
            mAdapter = new CameraTypeAdapter(context, camera_value);
            //适配器加入列表里
            listView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View itemLayout, int position, long id) {
        CameraTypeData data = (CameraTypeData) listView.getAdapter().getItem(position);
        LogUtils.d("CameraWindow onItemClick!  data=" + data.toString());

        popWindow.dissmiss();

        if(listener != null){
            listener.onCameraDismiss(data);
        }
    }

    public void dismiss() {
        popWindow.dissmiss();
    }

    @Override
    public void onDismiss() {
        LogUtils.d("CameraWindow onDismiss!");
    }

    public interface OnCameraDismissListener {
        void onCameraDismiss(CameraTypeData value);
    }
}
