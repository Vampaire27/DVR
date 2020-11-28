package com.wwc2.dvr.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.wwc2.dvr.R;
import com.wwc2.dvr.utils.SPUtils;
import com.wwc2.dvr.widget.CustomPopWindow;

import wang.relish.widget.vehicleedittext.VehicleKeyboardHelper;


public class CarNumberWindow implements PopupWindow.OnDismissListener,View.OnClickListener {
    private Context context;
    private View contentView;
    private CustomPopWindow popWindow;
    private EditText pass;
    private Button buttonOk;
    private Button buttonCancel;
    private InputMethodManager imm;
    private onDismiss onDismissLister;

    public CarNumberWindow(Context mContext,CarNumberWindow.onDismiss onDismissLister ){
        this.context = mContext;
        this.onDismissLister =onDismissLister;
    }


    public void show(View parent) {
        if (contentView == null) {
            contentView = View.inflate(context, R.layout.car_number_layout, null);
            pass = contentView.findViewById(R.id.editText_password);
            buttonOk = contentView.findViewById(R.id.btn_ok);
            buttonCancel = contentView.findViewById(R.id.btn_can);
            buttonOk.setOnClickListener(this);
            buttonCancel.setOnClickListener(this);
        }

        int windowHeight = (int) context.getResources().getDimension(R.dimen.car_number_hegith);
        CustomPopWindow.PopupWindowBuilder builder = new CustomPopWindow.PopupWindowBuilder(context);
        popWindow = builder.setView(contentView)
                .size(324, windowHeight)
                .setAnimationStyle(R.style.anim_set)
                .setTouchable(true)
                .setFocusable(true)
                .setOnDissmissListener(this)
                .setBackgroundDrawable(context.getResources().getDrawable(
                        R.mipmap.bg_car_number, context.getTheme()))
                .create()
                .showAtLocation(parent, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 80);

        pass.setText("");

        VehicleKeyboardHelper.bind(pass,parent);
    }

    @Override
    public void onDismiss() {
        popWindow.dissmiss();
    }


    @Override
    public void onClick(View v) {
      if(v.getId() == R.id.btn_ok){
          SPUtils.setCarNumber(context,pass.getText().toString());
          onDismissLister.onDismissCallBack(pass.getText().toString());
          popWindow.dissmiss();
      }else if(v.getId() == R.id.btn_can){
            popWindow.dissmiss();
      }
    }



    public interface  onDismiss{
       void onDismissCallBack(String number);
    }
}
