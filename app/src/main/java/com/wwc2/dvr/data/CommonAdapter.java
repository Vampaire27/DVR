package com.wwc2.dvr.data;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.wwc2.dvr.R;
import com.wwc2.dvr.base.ListBaseAdapter;
import com.wwc2.dvr.base.SuperViewHolder;
import com.wwc2.dvr.bean.CommonBean;

/**
 *  user: wangpeng on 2020/03/28.
 *  emai: wpeng@waterworld.com.cn
 */
public class CommonAdapter extends ListBaseAdapter<CommonBean> {

    public CommonAdapter(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.common_layout_item;
    }

    @Override
    public void onBindItemHolder(SuperViewHolder holder, int position) {
        TextView name = holder.getView(R.id.name);
        CheckBox mCheckbox = holder.getView(R.id.checkbox);
        name.setText(mDataList.get(position).getName());
        mCheckbox.setChecked(mDataList.get(position).isCheck());

        //设置单选按钮的选中
        mCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDataList.get(position).setCheck(isChecked);
            }
        });

    }


}
