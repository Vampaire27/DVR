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
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.widget.CustomPopWindow;

import java.util.ArrayList;
import java.util.List;

public class RecordTypeWindow implements PopupWindow.OnDismissListener, AdapterView.OnItemClickListener {
    private Context context;
    private View contentView;
    private CustomPopWindow popWindow;
    private List<CameraTypeData> camera_value;
    private ListView listView;
    private CameraTypeAdapter mAdapter;
    private OnRecordTypeDismissListener listener;

    private int cameraChannel = ConstantsData.TYPE_BACK;
    private String cameraType = "3";//720p(HD)

    public RecordTypeWindow(Context context, OnRecordTypeDismissListener recordTypeDismissListener) {
        this.context = context;
        this.listener = recordTypeDismissListener;
    }

    public void show(View parent, int channel) {
        if (contentView == null) {
            contentView = View.inflate(context, R.layout.record_camera, null);
        }
        cameraChannel = channel;
        cameraType = getCameraType(channel);

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
            listView.setOnItemClickListener(RecordTypeWindow.this);
        }
        camera_value = new ArrayList<>();

        switch (cameraType) {
            case "4"://1080p
                break;
//            case "3"://720p
            default:

                break;
        }
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
            listener.onRecordTypeDismiss(data, cameraChannel);
        }
    }

    public void dismiss() {
        popWindow.dissmiss();
    }

    @Override
    public void onDismiss() {
        LogUtils.d("VideoWindow onDismiss!");
    }

    public interface OnRecordTypeDismissListener {
        void onRecordTypeDismiss(CameraTypeData value, int channel);
    }

    private String getCameraType(int channel) {
        String camType = RecordData.getInstance().cameraType.getValue();
        String cam = "";
        if (camType != null) {
            LogUtils.d("getCameraType camType = " + camType);
            switch (channel){
                case ConstantsData.TYPE_BACK:
                    cam = camType.substring(0, 1);
                    break;
                case ConstantsData.TYPE_FRONT:
                    cam = camType.substring(1);
                    break;
            }
            LogUtils.d("cam = " + cam);
        }
        return cam;
    }
}
