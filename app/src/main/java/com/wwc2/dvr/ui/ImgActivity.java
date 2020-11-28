package com.wwc2.dvr.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wwc2.dvr.R;
import com.wwc2.dvr.widget.ZoomImageView;

/**
 * description ： TODO:类的作用
 * user: wangpeng on 2019/9/16.
 * emai: wpeng@waterworld.com.cn
 */

public class ImgActivity extends Activity {
    private ZoomImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_layout_main);
      String imgpath = (String) getIntent().getExtras().get("imgpath");

        imageView = findViewById(R.id.img_details);
        Glide.with(this).load(imgpath).into(imageView);
    }
}
