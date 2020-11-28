package com.wwc2.dvr.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wwc2.dvr.R;

public class setPopAdapter extends BaseAdapter {
    private Context context;
    private String[] strs;

    public setPopAdapter(Context content, String[] strs, int selectIndex) {
        this.strs = strs;
        this.context = content;
        this.selectItem = selectIndex;
    }

    @Override
    public int getCount() {
        return strs.length;
    }

    @Override
    public Object getItem(int position) {
        return strs[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.pop_item_layout, null);
            holder = new ViewHolder();
            holder.textview = (TextView) convertView.findViewById(R.id.tv_pop_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textview.setText(strs[position]);

        if (selectItem == position) {
            holder.textview.setTextColor(convertView.getResources().getColor(R.color.c_green_light));
        } else {
            holder.textview.setTextColor(convertView.getResources().getColor(R.color.black));
        }
        return convertView;
    }

    class ViewHolder{
        TextView textview;
    }

    private int selectItem = -1;
    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }
}
