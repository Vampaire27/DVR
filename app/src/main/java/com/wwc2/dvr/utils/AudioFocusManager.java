package com.wwc2.dvr.utils;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import com.wwc2.dvr.DvrApplication;

/**
 * description ： 音频焦点管理类
 * user: wangpeng on 2020/5/9.
 * emai: wpeng@waterworld.com.cn
 */
public class AudioFocusManager {
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;

    //zxzhong 请求音频焦点 设置监听
    public int requestTheAudioFocus(final AudioListener audioListener) {
        if (Build.VERSION.SDK_INT < 8) {//Android 2.2开始(API8)才有音频焦点机制
            return 0;
        }
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) DvrApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioFocusChangeListener == null) {
            mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//监听器
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                            //播放操作
                            audioListener.start();
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            //暂停操作
                            audioListener.pause();
                            break;
                        default:
                            break;
                    }
                }
            };
        }
        //下面两个常量参数试过很多 都无效，最终反编译了其他app才搞定，汗~
        int requestFocusResult = mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        return requestFocusResult;
    }

    //zxzhong 暂停、播放完成或退到后台释放音频焦点
    public void releaseTheAudioFocus() {
        if (mAudioManager != null && mAudioFocusChangeListener != null) {
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        }
    }

    public interface AudioListener{
        void start();
        void pause();
    }

}
