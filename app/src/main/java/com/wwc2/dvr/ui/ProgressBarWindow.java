package com.wwc2.dvr.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.R;
import com.wwc2.dvr.data.CameraTypeData;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.databinding.LayoutSettingBinding;
import com.wwc2.dvr.ui.settings.SettingContract;
import com.wwc2.dvr.ui.settings.SettingPresenter;
import com.wwc2.dvr.utils.FileUtils;
import com.wwc2.dvr.utils.SPUtils;
import com.wwc2.dvr.utils.Utils;
import com.wwc2.dvr.widget.CustomPopWindow;

public class ProgressBarWindow implements PopupWindow.OnDismissListener {
    private Context context;
    private View contentView;
    private CustomPopWindow popWindow;
    public ProgressBarWindow(Context context) {
        this.context = context;
    }

    public void show(View parent, String text) {
        if (contentView == null) {
            contentView = View.inflate(context, R.layout.layout_progress, null);
        }
        CustomPopWindow.PopupWindowBuilder builder = new CustomPopWindow.PopupWindowBuilder(context);
        popWindow = builder.setView(contentView)
                .size(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                .create()
                .showAtLocation(parent, Gravity.CENTER_HORIZONTAL, 0, 0);
        TextView textView = contentView.findViewById(R.id.tv_progress_text);
        if (textView != null) {
            textView.setText(text);
        }
        LogUtils.d("-------ProgressBar-----------show Create.....------------------text=" + text);
    }

    public void dismiss() {
        if(popWindow != null){
            popWindow.dissmiss();
        }
    }

    public boolean isShowing() {
        if(popWindow != null){
            return popWindow.isShowing();
        }
        return false;
    }

    @Override
    public void onDismiss() {
        LogUtils.d("ProgressBar onDismiss!");
    }
}
