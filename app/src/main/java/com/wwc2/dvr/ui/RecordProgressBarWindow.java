package com.wwc2.dvr.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.R;
import com.wwc2.dvr.widget.CustomPopWindow;

public class RecordProgressBarWindow implements PopupWindow.OnDismissListener {
    private Context context;
    private View contentView;
    private CustomPopWindow popWindow;
    public RecordProgressBarWindow(Context context) {
        this.context = context;
    }

    public void show(View parent) {
        if (contentView == null) {
            contentView = View.inflate(context, R.layout.record_progress, null);
        }
        CustomPopWindow.PopupWindowBuilder builder = new CustomPopWindow.PopupWindowBuilder(context);
        popWindow = builder.setView(contentView)
                .size(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                .create()
                .showAtLocation(parent, Gravity.CENTER_HORIZONTAL, 0, 0);
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
