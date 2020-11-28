package com.wwc2.dvr.encodec;

import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;

import com.wwc2.dvr.IRawDataCallback;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BaseDispatch  {

    IRawDataCallback mRawDataCallback;

    public BaseDispatch(IRawDataCallback mRawDataCallback) {
        this.mRawDataCallback = mRawDataCallback;
    }

    public abstract  void dispatchData(byte[] data);
    public abstract void start();
    public abstract  void destroy();

    protected IRawDataCallback getRawDataCallback(){
        return mRawDataCallback;
    }

    protected ParcelFileDescriptor copyAndPost(MemoryFile memoryFile, byte[] data, int size) throws IOException,
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException {
        memoryFile.writeBytes(data, 0, 0, size);
        Method method = MemoryFile.class.getDeclaredMethod("getFileDescriptor");
        FileDescriptor fd = (FileDescriptor) method.invoke(memoryFile);
        ParcelFileDescriptor pfd = ParcelFileDescriptor.dup(fd);
        return pfd;
    }
}
