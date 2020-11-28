package com.wwc2.dvr.ui.filemanager;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;

import com.github.jdsjlzx.interfaces.OnItemClickListener;

public class FileListPopWindow implements PopupWindow.OnDismissListener, View.OnClickListener, OnItemClickListener {
     Context mCxt;
     OnFileDismissListener mOnFileDismissListener;


    public FileListPopWindow(Context context, OnFileDismissListener fileDismissListener) {
        this.mCxt = context;
        this.mOnFileDismissListener = fileDismissListener;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onDismiss() {

    }

    @Override
    public void onItemClick(View view, int position) {

    }

    public interface OnFileDismissListener {
        void onFileDismiss();
    }
}
