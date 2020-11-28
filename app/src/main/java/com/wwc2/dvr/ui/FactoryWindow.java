package com.wwc2.dvr.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.R;
import com.wwc2.dvr.data.CameraTypeData;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.widget.CustomPopWindow;

public class FactoryWindow implements PopupWindow.OnDismissListener, AdapterView.OnItemClickListener {
    private Context context;
    private View contentView;
    private CustomPopWindow popWindow;
    private OnFactoryDismissListener listener;
    private EditText pass;
    private InputMethodManager imm;

    private String curPasswd = "8888";

    public FactoryWindow(Context context, OnFactoryDismissListener settingDismissListener) {
        this.context = context;
        this.listener = settingDismissListener;
    }

    public void show(View parent) {
        if (contentView == null) {
            contentView = View.inflate(context, R.layout.factory_password_modify, null);
            pass = contentView.findViewById(R.id.editText_password);
        }

        Uri uri = Uri.parse("content://" + Config.AUTHORITY + "/" + Config.FACTORY_PASSWORD);
        curPasswd = context.getContentResolver().getType(uri);

        CustomPopWindow.PopupWindowBuilder builder = new CustomPopWindow.PopupWindowBuilder(context);
        popWindow = builder.setView(contentView)
                .size(324, 154)
                .setAnimationStyle(R.style.anim_set)
                .setTouchable(true)
                .setFocusable(true)
                .setOnDissmissListener(this)
                .setBackgroundDrawable(context.getResources().getDrawable(
                        R.mipmap.bg_factory_window, context.getTheme()))
                .create()
                .showAtLocation(parent, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);

        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        pass.setText("");
        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LogUtils.d("onTextChanged() 输入..." );
//                mHandler.removeCallbacks(mRunnable);
//                //800毫秒没有输入认为输入完毕
//                mHandler.postDelayed(mRunnable, 800);
            }

            @Override
            public void afterTextChanged(Editable s) {
                LogUtils.d("onTextChanged() 输入完成..." );
                mHandler.removeCallbacks(mRunnable);
                //800毫秒没有输入认为输入完毕
                mHandler.postDelayed(mRunnable, 80);
            }
        });
    }

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            String password = pass.getText().toString();

//            Uri uri = Uri.parse("content://" + AUTHORITY + "/" + FACTORY_PASSWORD);
//            String strPassword = context.getContentResolver().getType(uri);

//            LogUtils.d("handleMessage() returned:输入完成  password=" + password + ",,strPassword=" + strPassword);
            if (curPasswd.equals(password)) {
                dismiss();
                if (listener != null) {
                    listener.onFactoryDismiss(password);
                }
            } else {
                LogUtils.d("handleMessage() returned:..密码错误...");
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> listView, View itemLayout, int position, long id) {
        CameraTypeData data = (CameraTypeData) listView.getAdapter().getItem(position);
        LogUtils.d("FactoryWindow onItemClick!  data=" + data.toString());
    }

    public void dismiss() {
        popWindow.dissmiss();
    }

    @Override
    public void onDismiss() {
        LogUtils.d("FactoryWindow onDismiss!");
        if(imm != null){
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            imm = null;
        }
        dismiss();
    }

    public interface OnFactoryDismissListener {
        void onFactoryDismiss(String password);
    }
}
