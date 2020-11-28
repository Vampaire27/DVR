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
import com.wwc2.dvr.widget.CustomPopWindow;

import java.util.ArrayList;
import java.util.List;

public class VideoWindow implements PopupWindow.OnDismissListener, AdapterView.OnItemClickListener {
    private Context context;
    private View contentView;
    private CustomPopWindow popWindow;
    private List<CameraTypeData> camera_value;
    private ListView listView;
    private CameraTypeAdapter mAdapter;
    private OnVideoDismissListener listener;

    public VideoWindow(Context context, OnVideoDismissListener settingDismissListener) {
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
            listView = view.findViewById(R.id.camera_list);
            listView.setOnItemClickListener(VideoWindow.this);
        }
        camera_value = new ArrayList<>();

        camera_value.add(new CameraTypeData("MAIN_A_SUB_A", "11"));
        camera_value.add(new CameraTypeData("MAIN_A_SUB_B", "21"));
        camera_value.add(new CameraTypeData("MAIN_B_SUB_A", "12"));
        camera_value.add(new CameraTypeData("MAIN_B_SUB_B", "22"));
    }

    private void initListViewData(){
        if(context != null) {
            mAdapter = new CameraTypeAdapter(context, camera_value);
            listView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View itemLayout, int position, long id) {
        CameraTypeData data = (CameraTypeData) listView.getAdapter().getItem(position);
        LogUtils.d("VideoWindow onItemClick!  data=" + data.toString());
        popWindow.dissmiss();
        if(listener != null){
            listener.onVideoDismiss(data);
        }
    }

    public void dismiss() {
        popWindow.dissmiss();
    }

    @Override
    public void onDismiss() {
        LogUtils.d("VideoWindow onDismiss!");
    }

    public interface OnVideoDismissListener {
        void onVideoDismiss(CameraTypeData value);
    }
}
