package com.wwc2.dvr.widget;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.wwc2.dvr.R;

public class CommonPopUpWindow extends PopupWindow {
    private ListView listView;
    public CommonPopUpWindow(View contentView, int width, int height, String[] strs, Drawable drawable, int selectIndex) {
        super(contentView, width, height);
        setFocusable(true);
        setBackgroundDrawable(drawable);
        listView = (ListView) contentView.findViewById(R.id.set_item_listview);
        listView.setAdapter(new setPopAdapter(contentView.getContext(),strs, selectIndex));
    }

    public void updateCurrentData(View contentView, String[] strs) {
        if (listView != null) {
            listView.setAdapter(new setPopAdapter(contentView.getContext(),strs, -1));
        }
    }

    public void setOnMyItemClickListener(AdapterView.OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    public void setListSelect(int index) {
        if (listView != null) {
            listView.setSelection(index);
        }
    }
}
