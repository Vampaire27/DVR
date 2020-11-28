package com.wwc2;

import android.content.Context;
import android.os.Bundle;

import io.reactivex.disposables.CompositeDisposable;

/**
 * @author huwei
 * @time 2019/03/18  16:20
 * @desc ${TODD}
 */


public abstract class BasePresenter<T extends IBaseView> {
    protected T mView;
    protected Context mContext;
    protected CompositeDisposable mCompositeDisposable;

    /**
     * 绑定View
     */
    public void onAttach(T view) {
        this.mView = view;
        mContext = mView.getContext();
        mCompositeDisposable = new CompositeDisposable();
        onCreate();
    }

    /**
     * 做初始化的操作,需要在V的视图初始化完成之后才能调用
     * presenter进行初始化.
     */
    public abstract void onCreate();

    /**
     * 在这里结束异步操作
     */
    public void onDestroy() {
        mCompositeDisposable.dispose();//结束异步请求.
    }

    /**
     * 在V销毁的时候调用,解除绑定
     */
    public void onDetach() {
        mView = null;
        mContext = null;
    }

    /**
     * 容易被回收掉时保存数据
     */
    public void onSaveInstanceState(Bundle outState){}



}
