package com.wwc2.dvr.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.R;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.DriveVideo;
import com.wwc2.dvr.utils.AudioFocusManager;
import com.wwc2.dvr.utils.FileUtils;
import com.wwc2.dvr.widget.CustomPopWindow;

import java.io.File;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.wwc2.dvr.utils.DateUtil.parseTime;

/**
 * description ：本地視頻播放器
 * user: wangpeng on 2020/5/6.
 * emai: wpeng@waterworld.com.cn
 */
public class VideoPopupWindow implements PopupWindow.OnDismissListener, View.OnClickListener {
    private Context context;
    private int position;
    private SurfaceView surfaceView;
    private TextView playTV,tvTotalTime,videoNameTV;
    private SeekBar timeSeekBar;
    private IMediaPlayer mediaPlayer;
    private ImageView btnPlayOrPause,btnPrevious,btnNext, btnBack;
    private static final int INTERNAL_TIME = 1000;// 进度间隔时间
    private MediaMetadataRetriever mmr;
    private AudioFocusManager audioFocusManager;

    private AppBaseUI mAppBaseUI;

    private CustomPopWindow popWindow;
    private View contentView ;

    private LinearLayout rlBottomLayout;

    // 记录当前的位置
    public int mCurrentPosition = 0;

    private List<DriveVideo> list ;
   private Handler mHandler = new Handler(new Handler.Callback() {
       @Override
       public boolean handleMessage(Message message) {
           // 展示给进度条和当前时间
           if (message.what ==100){
               if (mediaPlayer!=null){
                   int progress = (int) mediaPlayer.getCurrentPosition();
                   timeSeekBar.setProgress(progress);
                   playTV.setText(parseTime(progress));
                   // 继续定时发送数据
                   updateProgress();
               }
           } else if (message.what == 101) {
               if (rlBottomLayout != null) {
                   rlBottomLayout.setVisibility(View.INVISIBLE);
               }
               if (videoNameTV != null) {
                   videoNameTV.setVisibility(View.INVISIBLE);
               }
               if (btnBack != null) {
                   btnBack.setVisibility(View.INVISIBLE);
               }
           }
           return true;
       }
   });

    public VideoPopupWindow(Context context, int position, List<DriveVideo> list, AppBaseUI appBaseUI) {
//        super(context, R.style.ImgPopupWindow);
        this.context = context;
        this.list = list;
        this.position = position;
        this.mAppBaseUI = appBaseUI;
    }

    public void show(View parent) {
        if (contentView == null) {
            contentView = View.inflate(context, R.layout.video_layou_main, null);
        }
        initView(contentView);

        ViewParent vp = parent.getParent();
        if (vp instanceof ViewGroup) {
            ViewGroup view = (ViewGroup) vp;
            if (view != null) {
                view.removeAllViews();
            }
        }
        CustomPopWindow.PopupWindowBuilder builder = new CustomPopWindow.PopupWindowBuilder(context);
        popWindow = builder.setView(contentView)
                .size(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                        .MATCH_PARENT)
                .setFocusable(true)
                .setTouchable(true)
                .setFocusable(true)
                .setOnDissmissListener(this)
                .setBackgroundDrawable(context.getResources().getDrawable(
                        R.drawable.bg_set_layout, context.getTheme()))
                .create()
                .showAtLocation(parent, Gravity.CENTER, 0, 0);

        mHandler.sendEmptyMessageDelayed(101, INTERNAL_TIME * 5);
    }

    private void initView(View view){
        surfaceView =view. findViewById(R.id.surfaceview);
        playTV = view.findViewById(R.id.tv_play_time);
        tvTotalTime = view.findViewById(R.id.tv_total_time);
        timeSeekBar = view.findViewById(R.id.time_seekBar);
        videoNameTV = view.findViewById(R.id.videp_name_tv);

        btnPlayOrPause = view.findViewById(R.id.btn_play_or_pause);
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnNext = view.findViewById(R.id.btn_next);
        btnBack = view.findViewById(R.id.btn_bottom_back);

        rlBottomLayout = view.findViewById(R.id.rl_bottom_layout);

        timeSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);//滑动条监听

        btnPlayOrPause.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        surfaceView.setOnClickListener(this);

        initData();
    }

    private void initData() {
        audioFocusManager = new AudioFocusManager();

        //使用第三方播放器，解决原生播放器播放.ts视频，拖动进度条时会停止。
        IjkMediaPlayer ijkMediaPlayer = null;
        ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_FATAL);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);//解码方式 1硬解码 0软解码
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);

        mediaPlayer = ijkMediaPlayer;
        mediaPlayer.setOnPreparedListener(mPreparedListener);
        mediaPlayer.setOnCompletionListener(mCompletionListener);
        mediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);

//        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                LogUtils.d("onCompletion。。。" + mediaPlayer.isPlaying());
//                mHandler.removeCallbacks(runnable);
//                mHandler.removeMessages(100);
//                changeVideo(++mCurrentPosition);
//            }
//        });//监听播放完毕事件，自动下一個
//        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//            @Override
//            public void onSeekComplete(MediaPlayer mp) {
//                int progress = mediaPlayer.getCurrentPosition();
//                LogUtils.d("setOnSeekCompleteListener。。。" + mediaPlayer.isPlaying() + ", progress=" + progress);
//            }
//        });
//        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//                LogUtils.e("onError。。。what=" + what + ", extra=" + extra);
//                return false;
//            }
//        });
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置视频流类型
        if (audioFocusManager != null) {
            int requestCode = audioFocusManager.requestTheAudioFocus(new AudioFocusManager.AudioListener() {
                @Override
                public void start() {
                    LogUtils.d("焦点抢占完毕1。。。" + mediaPlayer.isPlaying());
                    if (!mediaPlayer.isPlaying()) {
//                        playVideo();
                        updateProgress();
                        mediaPlayer.start();
                    }
                }

                @Override
                public void pause() {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                }
            });
            if (requestCode == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                LogUtils.d("焦点抢占完毕2。。。" + mediaPlayer.isPlaying());
                if (!mediaPlayer.isPlaying()) {
                    playVideo();
                }
            }
        }
    }

    private void playVideo(){
//        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                mediaPlayer.start();
//                btnPlayOrPause.setBackgroundResource(R.drawable.btn_pause_selector);
//            }
//        });
        mHandler.postDelayed(runnable,200);

        Intent intent = new Intent(Config.ACTION_DVR_VIDEO);
        intent.putExtra(Config.KEY_DVR_VIDEO, true);
        context.sendBroadcast(intent);
    }

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            LogUtils.d("onPrepared");
            mediaPlayer.start();
            btnPlayOrPause.setBackgroundResource(R.drawable.btn_pause_selector);
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {
        public void onCompletion(IMediaPlayer mp) {
            LogUtils.d("onCompletion。。。" + mediaPlayer.isPlaying());
            mHandler.removeCallbacks(runnable);
            mHandler.removeMessages(100);
            changeVideo(++mCurrentPosition);
        }
    };

    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            long progress = mediaPlayer.getCurrentPosition();
            LogUtils.d("setOnSeekCompleteListener。。。" + mediaPlayer.isPlaying() + ", progress=" + progress);
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            changeVideo(position);
        }
    };

    private void changeVideo(int position) {
        if (position < 0) {
            mCurrentPosition = position = list.size() - 1;
            LogUtils.d("mList.size:" + list.size());
        } else if (position > list.size() - 1) {
            mCurrentPosition = position = 0;
        }
        LogUtils.d("mList.position:" + position);

        String path = list.get(position).getName();

        File file = new File(path);
        if (file.exists()) {
            long size = 0;
            try {
                size = FileUtils.getFileSize(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogUtils.d("VideoPopupWindow------------------>size=" + size);
            if (size == 0) {
                FileUtils.deleteFile(path);
                changeVideo(++mCurrentPosition);
                return;
            }
        }

        try {
            // 先重置，释放掉之前的资源
            mediaPlayer.reset();
            mediaPlayer.setDisplay(surfaceView.getHolder());
            LogUtils.d("当前播放视频为:" + list.get(position).getName());
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();

            if (mmr == null) {
                mmr = new MediaMetadataRetriever();
            }
            mmr.setDataSource(path);
            // 播放时长单位为毫秒
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            LogUtils.d("总时间----path-------------->" + path + " --------duration->" + duration);
            LogUtils.d("总时间：" + parseTime(Integer.parseInt(duration)));
            timeSeekBar.setProgress(0);
            timeSeekBar.setMax(Integer.parseInt(duration));
            tvTotalTime.setText(parseTime(Integer.parseInt(duration)));
            String name = path.substring(path.lastIndexOf("/")).replaceAll("/", "");
            videoNameTV.setText(name);
            updateProgress();
        } catch (Exception e) { ///在这里增加播放失败.
            mediaPlayer.release();
            if (mediaPlayer != null)
                LogUtils.d("eeeeeeeeeeeeerrormediaPlayer!=null");
            e.printStackTrace();
        }
    }

    private void updateProgress() {
        // 使用Handler每间隔1s发送一次空消息，通知进度条更新
        Message msg = Message.obtain();// 获取一个现成的消息
        // 使用MediaPlayer获取当前播放时间除以总时间的进度
        int progress = (int) mediaPlayer.getCurrentPosition();
        msg.arg1 = (int) progress;
        msg.what = 100;
        mHandler.sendMessageDelayed(msg, INTERNAL_TIME);
    }

    //滑动条监听
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        // 当手停止拖拽进度条时执行该方法
        // 获取拖拽进度
        // 将进度对应设置给MediaPlayer
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            mediaPlayer.seekTo(progress);
        }
    };

    public void dismiss() {
        LogUtils.d("dismiss------------");

        Intent intent = new Intent(Config.ACTION_DVR_VIDEO);
        intent.putExtra(Config.KEY_DVR_VIDEO, false);
        context.sendBroadcast(intent);

//        mHandler.postDelayed(new Runnable() {
//           @Override
//           public void run() {
               if (mHandler!=null && mediaPlayer !=null){
                   mHandler.removeMessages(100);
                   mHandler.removeCallbacks(runnable);
                   try {
                       mediaPlayer.stop();
                       mediaPlayer.release();
                       mediaPlayer = null;
                       if (audioFocusManager != null) {
                           LogUtils.d("releaseTheAudioFocus------------");
                           audioFocusManager.releaseTheAudioFocus();
                       }
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
                   popWindow.dissmiss();
               }
//           }
//       },1000);
    }

    @Override
    public void onDismiss() {
        LogUtils.d("onDismiss------------");
        dismiss();
    }

    public boolean isVideoShowing() {
        if (popWindow != null) {
            return popWindow.isShowing();
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play_or_pause:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlayOrPause.setBackgroundResource(R.drawable.btn_playvd_selector);
                } else {
                    updateProgress();
                    mediaPlayer.start();
                    btnPlayOrPause.setBackgroundResource(R.drawable.btn_pause_selector);
                }
                break;
            case R.id.btn_previous:
                mHandler.removeCallbacks(runnable);
                mHandler.removeMessages(100);
                changeVideo(mCurrentPosition--);
                break;
            case R.id.btn_next:
                mHandler.removeCallbacks(runnable);
                mHandler.removeMessages(100);
                changeVideo(mCurrentPosition ++);
                break;
            case R.id.surfaceview:
                if (rlBottomLayout != null) {
                    rlBottomLayout.setVisibility(rlBottomLayout.isShown() ? View.INVISIBLE : View.VISIBLE);
                }
                if (videoNameTV != null) {
                    videoNameTV.setVisibility(videoNameTV.isShown() ? View.INVISIBLE : View.VISIBLE);
                }
                if (btnBack != null) {
                    btnBack.setVisibility(btnBack.isShown() ? View.INVISIBLE : View.VISIBLE);
                }
                break;
            case R.id.btn_bottom_back:
                if (mAppBaseUI != null) {
                    mAppBaseUI.sendBack(context);
                }
                break;
            default:
                return;
        }
        mHandler.removeMessages(101);
        mHandler.sendEmptyMessageDelayed(101, INTERNAL_TIME * 5);
    }
}
