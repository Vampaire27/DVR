package com.wwc2.dvr.cameraevent;

import com.wwc2.dvr.ui.AppBaseUI;

import java.net.PortUnreachableException;

public abstract class BaseHandler {
    private BaseHandler mNextHandler;

    private AppBaseUI mAppBaseUI;

    abstract  public boolean needHandler(String code);

    abstract public void toDoHandler(String code );

    public BaseHandler(AppBaseUI mAppBaseUI) {
        this.mAppBaseUI = mAppBaseUI;
    }

    public void setNextHandler(BaseHandler nextHandler) {
        mNextHandler = nextHandler;
    }

    public void handlerRequest(String  code){
        if(needHandler(code)){
            toDoHandler(code);
        }else {
            if(mNextHandler != null){
               mNextHandler.handlerRequest(code);
            }
        }
    }

     public AppBaseUI getAppBaseUI(){
        return mAppBaseUI;
     }
}
