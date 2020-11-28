package com.wwc2.dvr.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.bumptech.glide.Glide;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.R;
import com.wwc2.dvr.widget.CustomPopWindow;
import com.wwc2.dvr.widget.ZoomImageView;

/**
 * description ： TODO:类的作用
 * user: wangpeng on 2019/9/16.
 * emai: wpeng@waterworld.com.cn
 */

public class ImgPopupWindow {
    private ZoomImageView imageView;

    private Context context;
    private String path = "";
    private CustomPopWindow popWindow;

    private View contentView;
    public ImgPopupWindow(Context context, String path) {
        this.context = context;
        this.path = path;

    }

    public void show(View parent){
        if (contentView ==null){
            contentView =  View.inflate(context, R.layout.img_layout_main, null);
        }
        initView(contentView);

        ViewParent vp = parent.getParent();
        if(vp instanceof ViewGroup){
            ViewGroup view = (ViewGroup) vp;
            if(view != null){
                view.removeAllViews();
            }
        }
        CustomPopWindow.PopupWindowBuilder builder = new CustomPopWindow.PopupWindowBuilder(context);
        popWindow = builder.setView(contentView)
                .size(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                        .MATCH_PARENT)
                .setFocusable(true)
                .setTouchable(true)
                .setFocusable(true)
                .setBackgroundDrawable(context.getResources().getDrawable(
                        R.drawable.bg_set_layout, context.getTheme()))
                .create()
                .showAtLocation(parent, Gravity.CENTER, 0, 0);


    }
    private void initView(View contentView){
        imageView = (ZoomImageView)contentView.findViewById(R.id.img_details);
        LogUtils.d("imageView" + imageView  + " context:" + context + " path:" + path);
        Glide.with(context).load(path).into(imageView);

    }
    public void dismiss() {
        // 打印调用堆栈信息
        RuntimeException e = new RuntimeException("Log: stack info");
        e.fillInStackTrace();
        LogUtils.i("ImgPopupWindow", "dismiss stack, ", e);

        popWindow.dissmiss();
    }

    public boolean isImgShowing() {
        if (popWindow != null) {
            return popWindow.isShowing();
        }

        return false;
    }
}
