package com.wwc2.dvr.binding.viewadapter.image;


import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * Created by goldze on 2017/6/18.
 */
public final class ViewAdapter {
    @BindingAdapter(value = {"url", "placeholderRes"}, requireAll = false)
    public static void setImageUri(ImageView imageView, String url, int placeholderRes) {
        if (!TextUtils.isEmpty(url)) {
            //使用Glide框架加载图片
            //fixme 暂时用不上图片库
//            Glide.with(imageView.getContext())
//                    .load(url)
//                    .apply(new RequestOptions().placeholder(placeholderRes))
//                    .into(imageView);
        }
    }
}

