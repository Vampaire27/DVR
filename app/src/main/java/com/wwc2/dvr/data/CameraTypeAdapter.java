package com.wwc2.dvr.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wwc2.dvr.R;

import java.util.List;

public class CameraTypeAdapter extends BaseAdapter {
    private Context context;
    private List<CameraTypeData> list;
    
    public CameraTypeAdapter(Context context, List<CameraTypeData> value) {
        this.context = context;
        this.list = value;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.camera_item, null, false);
            viewHolder = new ViewHolder();
            viewHolder.cameraName = convertView.findViewById(R.id.cameraName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.cameraName.setText(list.get(position).getKey());

        return convertView;
    }

    public static class ViewHolder {
        public TextView cameraName;
    }
}
