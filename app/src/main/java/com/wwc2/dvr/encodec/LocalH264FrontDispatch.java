package com.wwc2.dvr.encodec;

import android.content.Context;

import com.wwc2.dvr.IRawDataCallback;

public class LocalH264FrontDispatch extends LocalH264Dispatch{

    private String TAG = "LocalH264FrontDispatch";

    private static final String H264_SOCKET = "h264StreamFrontSocket";
    private static final String H264_FILE = "/proc/h264/stream_front";

    public LocalH264FrontDispatch(IRawDataCallback mRawDataCallback, Context mCtx) {
        super(mRawDataCallback,mCtx);
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
