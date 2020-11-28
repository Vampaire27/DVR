package com.wwc2.dvr.ui.reversing;

import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.DvrApplication;
import com.wwc2.dvr.R;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.ui.record.RecordPresenter;
import com.wwc2.dvr.utils.ClickFilter;
import com.wwc2.dvr.utils.FileUtils;
import com.wwc2.dvr.utils.SPUtils;
import com.wwc2.dvr.widget.CustomPopWindow;

/**
 * user: wangpeng on 2019/7/26.
 * emai: wpeng@waterworld.com.cn
 * 倒车SeekBar
 */

public class ReversinPopupWindow implements SeekBar.OnSeekBarChangeListener {

    private Context context;
    private View contentView;
    private CustomPopWindow popWindow;
    private TextView tvBrightness,tvSaturation, tvContrast;
    private Button btn_show_check;
    private Handler mHandler;
    private CheckClickListener mCheckClickListener;

    //默认值
    private static int DEF_BRIGHTNESS = 120;
    private static int DEF_SATURATION = 128;
    private static int DEF_CONTRAST = 120;

    public ReversinPopupWindow(Context context,Handler mHandler,CheckClickListener mCheckClickListener) {
        this.context = context;
        this.mHandler = mHandler;
        this.mCheckClickListener = mCheckClickListener;
    }

    public void show(View  parent){
        if (contentView ==null){
            contentView =  View.inflate(context, R.layout.layout_set, null);
        }
        initSetState(contentView);
        ViewParent vp = parent.getParent();
        if(vp instanceof ViewGroup){
            ViewGroup view = (ViewGroup) vp;
            if(view != null){
                view.removeAllViews();
            }
        }
        CustomPopWindow.PopupWindowBuilder builder = new CustomPopWindow.PopupWindowBuilder(context);
        popWindow = builder.setView(contentView)
                .size(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                        .WRAP_CONTENT)
                .setFocusable(true)
                .setTouchable(true)
                .setFocusable(true)
                .setBackgroundDrawable(context.getResources().getDrawable(
                        R.drawable.bg_set_layout, context.getTheme()))
                .create()
                .showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    private void  initSetState(View view){
        final SeekBar rgBrightness = (SeekBar) view.findViewById(R.id.camera_brightness_progress);
        final SeekBar rgSaturation = (SeekBar) view.findViewById(R.id.camera_saturation_progress);
        final SeekBar rgContrast = (SeekBar) view.findViewById(R.id.camera_contrast_progress);
        tvBrightness = (TextView) view.findViewById(R.id.tv_brightness);
        tvSaturation = (TextView) view.findViewById(R.id.tv_saturation);
        tvContrast = (TextView) view.findViewById(R.id.tv_contrast);
        btn_show_check = (Button) view.findViewById(R.id.btn_show_check);
        rgBrightness.setOnSeekBarChangeListener(this);
        rgSaturation.setOnSeekBarChangeListener(this);
        rgContrast.setOnSeekBarChangeListener(this);
        btn_show_check.setVisibility(View.VISIBLE);

        rgBrightness.setProgress(RecordData.getInstance().brightness.getValue() - 80);
        rgSaturation.setProgress(RecordData.getInstance().saturation.getValue());
        rgContrast.setProgress(RecordData.getInstance().contrast.getValue() - 80);
        tvBrightness.setText(RecordData.getInstance().brightness.getValue() + "");
        tvSaturation.setText(RecordData.getInstance().saturation.getValue() + "");
        tvContrast.setText(RecordData.getInstance().contrast.getValue() + "");

        view.findViewById(R.id.btn_def).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClickFilter.filter(300L)) {
                    return;
                }

                RecordPresenter.readDefaultParam();
                DEF_BRIGHTNESS = RecordData.getInstance().brightness.getValue();
                DEF_SATURATION = RecordData.getInstance().saturation.getValue();
                DEF_CONTRAST = RecordData.getInstance().contrast.getValue();

                rgBrightness.setProgress(DEF_BRIGHTNESS - 80);
                rgSaturation.setProgress(DEF_SATURATION);
                rgContrast.setProgress(DEF_CONTRAST - 80);

                tvBrightness.setText(DEF_BRIGHTNESS + "");
                tvSaturation.setText(DEF_SATURATION + "");
                tvContrast.setText(DEF_CONTRAST + "");

                FileUtils.writeTextFile(Config.KEY_BRIGHTNESS + DEF_BRIGHTNESS, Config.CAMERA_PARAMS_NODE);
                FileUtils.writeTextFile(Config.KEY_SATURATION + DEF_SATURATION, Config.CAMERA_PARAMS_NODE);
                FileUtils.writeTextFile(Config.KEY_CONTRAST + DEF_CONTRAST, Config.CAMERA_PARAMS_NODE);

                SPUtils.putInt(context, ConstantsData.KEY_BRIGHTNESS, DEF_BRIGHTNESS);
                SPUtils.putInt(context, ConstantsData.KEY_SATURATION, DEF_SATURATION);
                SPUtils.putInt(context, ConstantsData.KEY_CONTRAST, DEF_CONTRAST);
            }
        });

        view.findViewById(R.id.btn_show_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClickFilter.filter(300L)) {
                    return;
                }
                switch (v.getId()) {
                    case R.id.btn_show_check:
                        mCheckClickListener.checkClick();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public interface  CheckClickListener{
        void checkClick();
    }

    public void dismissPop(){
        if(popWindow != null){
            if(popWindow.isShowing()){
                popWindow.dissmiss();
            }
        }
    }

    public boolean isShow(){
        if(popWindow != null){
            return popWindow.isShowing();
        }
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!seekBar.isPressed()) {
            return;
        }
        if (mHandler != null) {
            mHandler.removeMessages(Config.AUTO_CLOSE);
            mHandler.sendEmptyMessageDelayed(Config.AUTO_CLOSE, Config.DELAY_TIME);
        }
        switch (seekBar.getId()) {
            case R.id.camera_brightness_progress:
                FileUtils.writeTextFile(Config.KEY_BRIGHTNESS + (progress + 80), Config.CAMERA_PARAMS_NODE);
                tvBrightness.setText((progress + 80) + "");
//                LogUtils.e((progress + 80) + "");
                break;
            case R.id.camera_saturation_progress:
                FileUtils. writeTextFile(Config.KEY_SATURATION + progress, Config.CAMERA_PARAMS_NODE);
                tvSaturation.setText(progress + "");

//                LogUtils.e((progress) + "");
                break;
            case R.id.camera_contrast_progress:
                FileUtils.writeTextFile(Config.KEY_CONTRAST + (progress + 80), Config.CAMERA_PARAMS_NODE);
                tvContrast.setText((progress + 80) + "");
//                LogUtils.e((progress + 80) + "");
                break;
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.camera_brightness_progress:
                RecordData.getInstance().brightness.setValue(seekBar.getProgress() + 80);
                SPUtils.putInt(context, ConstantsData.KEY_BRIGHTNESS, seekBar.getProgress() + 80);
                break;
            case R.id.camera_saturation_progress:
                RecordData.getInstance().saturation.setValue(seekBar.getProgress());
                SPUtils.putInt(context, ConstantsData.KEY_SATURATION, seekBar.getProgress());
                break;
            case R.id.camera_contrast_progress:
                RecordData.getInstance().contrast.setValue(seekBar.getProgress() + 80);
                SPUtils.putInt(context, ConstantsData.KEY_CONTRAST, seekBar.getProgress() + 80);
                break;
            default:
                break;
        }
    }
}
