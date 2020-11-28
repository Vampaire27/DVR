package com.wwc2.dvr.encodec;

import android.content.Context;

import com.wwc2.dvr.IRawDataCallback;

public class LocalH264RightDispatch extends LocalH264Dispatch{

    private String TAG = "LocalH264RightDispatch";

    private static final String H264_SOCKET = "h264StreamRightSocket";
    private static final String H264_FILE = "/proc/h264/stream_right";

    public LocalH264RightDispatch(IRawDataCallback mRawDataCallback, Context mCtx) {
        super(mRawDataCallback,mCtx);;
    }

    @Override
    protected String getH264Socket() {
        return H264_SOCKET;
    }

    @Override
    protected  String getH264File() {
        return H264_FILE;
    }

}
