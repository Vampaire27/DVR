package com.wwc2.dvr.ui;

import android.content.Context;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.utils.Utils;


public class GestureSurfaceView extends SurfaceView {
    private float mXDistance;
    private float mYDistance;
    private float mFlingDistance;

    private GestureListener mListener;
    private float mDownX;
    private float mDownY;
    private long beginTime;
    private Context mContext = null;

    public interface GestureListener {
        void onClickFront();

        void onClickBack();

        void onClickLeft();

        void onClickRight();

        void onSwipeUp();

        void onSwipeDown();

    }


    public GestureSurfaceView(Context context) {
        this(context, null);
    }

    public GestureSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
    }

    public void setGestureListener(GestureListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                beginTime = System.currentTimeMillis();
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if(mListener != null) {
                    detectGesture(mDownX, event.getX(), mDownY, event.getY());
                }
                break;
        }
        return true;
    }

    private void detectGesture(float downX, float upX, float downY, float upY) {
        String curRecordType = RecordData.getInstance().recordType.getValue();
        if (Config.TYPE_ONE_STREAM.equals(curRecordType) || mContext == null) {
            return;
        }

        Point point = Utils.getDisplaySize(mContext);
        if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            mXDistance = point.y;
        } else {
            mXDistance = point.x / 2;
        }
        if (Config.TYPE_DUAL_STREAM.equals(curRecordType) ||
                Config.TYPE_TWO_STREAM.equals(curRecordType) ||
                Config.TYPE_ONE_STREAM.equals(curRecordType)) {
            mYDistance = point.y;
        } else {
            mYDistance = point.y / 2;
        }
        mFlingDistance = point.y / 20;

        float distanceY = upY - downY;
        float duration = System.currentTimeMillis() - beginTime;
         if (Math.abs(distanceY) > mFlingDistance ) {
             if (distanceY > 0) {
                 mListener.onSwipeDown();
             } else {
                 mListener.onSwipeUp();
             }
         } else if (upX > mXDistance && upY > mYDistance) {
             mListener.onClickRight();
         } else if (upX > mXDistance && upY < mYDistance) {
             mListener.onClickBack();
         } else if (upX < mXDistance && upY > mYDistance) {
             mListener.onClickLeft();
         } else {
             mListener.onClickFront();
         }
    }
}


