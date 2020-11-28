package com.wwc2.dvr.ui.filemanager;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.jdsjlzx.ItemDecoration.DividerDecoration;
import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.R;
import com.wwc2.dvr.bean.CommonBean;
import com.wwc2.dvr.data.Config;
import com.wwc2.dvr.data.ConstantsData;
import com.wwc2.dvr.data.DBUtil;
import com.wwc2.dvr.data.DriveVideo;
import com.wwc2.dvr.data.DriveVideoBack;
import com.wwc2.dvr.data.DriveVideoDual;
import com.wwc2.dvr.data.DriveVideoFont;
import com.wwc2.dvr.data.DriveVideoLeft;
import com.wwc2.dvr.data.DriveVideoQuart;
import com.wwc2.dvr.data.DriveVideoRight;
import com.wwc2.dvr.data.RecordData;
import com.wwc2.dvr.data.RecordFileAdapterTest;
import com.wwc2.dvr.fourCamera.FourCameraProxy;
import com.wwc2.dvr.ui.AppBaseUI;
import com.wwc2.dvr.ui.ImgActivity;
import com.wwc2.dvr.utils.ClickFilter;
import com.wwc2.dvr.utils.Event;
import com.wwc2.dvr.utils.FileUtils;
import com.wwc2.dvr.utils.RxBus;
import com.wwc2.dvr.utils.SPUtils;
import com.wwc2.dvr.utils.ToastUtils;
import com.wwc2.dvr.utils.Utils;
import com.wwc2.dvr.widget.CommonDialog;
import com.wwc2.dvr.widget.CustomPopWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  录像预览
 *  user: wangpeng on 2019/12/12.
 *  emai: wpeng@waterworld.com.cn
 */
public class RecordFilePopWindowTest implements PopupWindow.OnDismissListener, View.OnClickListener, OnItemClickListener {
    private Context context;
    private View contentView;
    private CustomPopWindow popWindow;
    private final long FILTERTIME = 500L;
    private Button btnVideoSelect, tvAllSelected, tvAllSelected_no, tvAllUnselected, btnDelete, btnLock, btnFormat;
    private TextView tvCount;

    private RelativeLayout actionBar;
    private LinearLayout bottomMenu;
    private DividerDecoration itemDecoration;

    private LRecyclerView mRecyclerView;
    private LRecyclerViewAdapter mLRecyclerViewAdapter;
    private boolean isonLoadMore =false;

    private RecordFileAdapterTest mDataAdapter;
    private OnFileDismissListener listener;

    private ViewGroup cardFrontVG,cardPostVG,cardPhotoVG,cardLeftVG,cardRightVG,cardQuartVG,cardDualVG;
    private TextView recordBG;
    private ProgressBar  mProgressBar;
    private MediaMetadataRetriever mmr;

    public ObservableBoolean choiceState = new ObservableBoolean(false);
    public ObservableList<DriveVideo> newlist = new ObservableArrayList<>();
    private int  typeflag;
    private RecordData recordData;
    private RecordFileManager mRecordFile = null;
    private AppBaseUI mAppBaseUI;

    /**已经获取到多少条数据了*/
    private static int mCurrentCounter = 0;

    private  int page = 1; //第几页

    /**服务器端一共多少条数据*/
    private   int TOTAL_COUNTER =0;

    private  Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (ConstantsData.TYPE_FRONT == msg.what ||
                    ConstantsData.TYPE_BACK == msg.what ||
                    ConstantsData.TYPE_LEFT == msg.what ||
                    ConstantsData.TYPE_RIGHT == msg.what ||
                    ConstantsData.TYPE_IMAGE == msg.what ||
                    ConstantsData.TYPE_QUART == msg.what ||
                    ConstantsData.TYPE_DUAL == msg.what) {
//                mRecyclerView.refresh();
                typeflag = msg.what;
                LogUtils.d("WPTAGTest,-----typeflag:" + typeflag);
                setBackground(typeflag);
                handler.removeMessages(6);
                handler.sendEmptyMessageDelayed(6, 150);
            } else {
                switch (msg.what) {
                    case 4:
                        tvAllSelected.setVisibility(View.VISIBLE);
                        tvAllSelected_no.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);
                        break;
                    case 6:
                        handler.removeMessages(6);
                        mProgressBar.setVisibility(View.GONE);
                        updateItemSelected();
                        boolean isSelect = choiceState.get();

                        if (isSelect) {
                            LogUtils.d("WPTAGTest,cancelEvent");
                            cancelEvent();
                        }
//                        initListData(typeflag);
//                        updateFileList(typeflag);
                        LogUtils.d("WPTAGTest,mRecyclerView.refresh()");
                        mRecyclerView.refresh();
                        handler.sendEmptyMessage(4);
                        break;
                    case 7:
                        mDataAdapter.isCheckMap.clear();
                        tvAllSelected.setVisibility(View.VISIBLE);
                        tvAllSelected_no.setVisibility(View.GONE);
                        mDataAdapter.notifyDataSetChanged();
                        updateItemSelected();
                        handler.sendEmptyMessage(4);
                        break;
                }
            }
        }
    };

    private void updateFileList(int type) {
        LogUtils.d("updateFileList--start--" + SystemClock.currentThreadTimeMillis());
        ObservableList<DriveVideo> newlist = new ObservableArrayList<>();

        switch (type) {
            case ConstantsData.TYPE_FRONT:
                if (DBUtil.getAllDriveFontVideo() != null) {
                    List<DriveVideoFont> mDriveVideoFont = DBUtil.getLimitDriveFontVideo(page, 10);
                    for (DriveVideoFont data : mDriveVideoFont) {
//                        LogUtils.d("WPTAGTest,TYPE_FRONT,,,,dataName:" + data.getName());
                        if (!FileUtils.fileIsExists(data.getName())) {
                            LogUtils.d("updateFileList TYPE_FRONT,,,,dataName:" + data.getName()+" not exist!");
                            DBUtil.deleteDriveFontVideoByName(data.getName());
                        } else {
                            DriveVideo mDriveVideo = new DriveVideo();
                            mDriveVideo.setLockStatus(data.getLockStatus());
                            mDriveVideo.setId(data.getId());
                            mDriveVideo.setName(data.getName());
                            mDriveVideo.setLocaltion(data.getLocaltion());
                            newlist.add(mDriveVideo);
                        }
                    }
                } else {
                    setNodata();
                    return;
                }
                break;
            case ConstantsData.TYPE_BACK:
                if (DBUtil.getAllDriveBackVideo() != null) {
                    List<DriveVideoBack> mDriveVideoFont = DBUtil.getLimitDriveBackVideo(page, 10);
                    for (DriveVideoBack data : mDriveVideoFont) {
                        if (!FileUtils.fileIsExists(data.getName())) {
                            LogUtils.d("updateFileList TYPE_BACK,,,,dataName:" + data.getName()+" not exist!");
                            DBUtil.deleteDriveBackVideoByName(data.getName());
                        } else {
                            DriveVideo mDriveVideo = new DriveVideo();
                            mDriveVideo.setLockStatus(data.getLockStatus());
                            mDriveVideo.setId(data.getId());
                            mDriveVideo.setName(data.getName());
                            mDriveVideo.setLocaltion(data.getLocaltion());
                            newlist.add(mDriveVideo);
                        }
                    }
//                    LogUtils.e("WPTAGTest,TYPE_BACK--handler--start---size=" + newlist.size());
                } else {
                    LogUtils.d("WPTAGTest,TYPE_BACK----setNodata---");
                    setNodata();
                    return;
                }
                break;
            case ConstantsData.TYPE_LEFT:
                if (DBUtil.getAllDriveLeftVideo() != null) {
                    List<DriveVideoLeft> mDriveVideoLeft = DBUtil.getLimitDriveLeftVideo(page, 10);
                    for (DriveVideoLeft data : mDriveVideoLeft) {
                        if (!FileUtils.fileIsExists(data.getName())) {
                            LogUtils.d("updateFileList TYPE_LEFT,,,,dataName:" + data.getName()+" not exist!");
                            DBUtil.deleteDriveLeftVideoByName(data.getName());
                        } else {
                            DriveVideo mDriveVideo = new DriveVideo();
                            mDriveVideo.setLockStatus(data.getLockStatus());
                            mDriveVideo.setId(data.getId());
                            mDriveVideo.setName(data.getName());
                            mDriveVideo.setLocaltion(data.getLocaltion());
                            newlist.add(mDriveVideo);
                        }
                    }
//                    LogUtils.e("WPTAGTest,TYPE_LEFT--handler--start---size=" + newlist.size());
                } else {
                    setNodata();
                    return;
                }
                break;
            case ConstantsData.TYPE_RIGHT:
                if (DBUtil.getAllDriveRightVideo() != null) {
                    List<DriveVideoRight> mDriveVideoRight = DBUtil.getLimitDriveRightVideo(page, 10);
                    for (DriveVideoRight data : mDriveVideoRight) {
                        if (!FileUtils.fileIsExists(data.getName())) {
                            LogUtils.d("updateFileList TYPE_RIGHT,,,,dataName:" + data.getName()+" not exist!");
                            DBUtil.deleteDriveRightVideoByName(data.getName());
                        } else {
                            DriveVideo mDriveVideo = new DriveVideo();
                            mDriveVideo.setLockStatus(data.getLockStatus());
                            mDriveVideo.setId(data.getId());
                            mDriveVideo.setName(data.getName());
                            mDriveVideo.setLocaltion(data.getLocaltion());
                            newlist.add(mDriveVideo);
                        }
                    }
//                    LogUtils.e("WPTAGTest,TYPE_RIGHT--handler--start---size=" + newlist.size());
                } else {
                    LogUtils.d("WPTAGTest,TYPE_RIGHT----setNodata---");
                    setNodata();
                    return;
                }
                break;
            case ConstantsData.TYPE_IMAGE:
                List<DriveVideo> list = mRecordFile.getVideoList(ConstantsData.TYPE_IMAGE);
                mDataAdapter.setDataList(list);
                mRecyclerView.refreshComplete(list.size());
                mLRecyclerViewAdapter.notifyDataSetChanged();
                mRecyclerView.setNoMore(true);
                LogUtils.e("WPTAGTest,TYPE_IMAGE--handler--start---");
                return;
            case ConstantsData.TYPE_QUART:
                if (DBUtil.getAllDriveQuartVideo() != null) {
                    List<DriveVideoQuart> mDriveVideoQuart = DBUtil.getLimitDriveQuartVideo(page, 10);
                    for (DriveVideoQuart data : mDriveVideoQuart) {
                        if (!FileUtils.fileIsExists(data.getName())) {
                            LogUtils.d("updateFileList TYPE_QUART,,,,dataName:" + data.getName()+" not exist!");
                            DBUtil.deleteDriveQuartVideoByName(data.getName());
                        } else {
                            DriveVideo mDriveVideo = new DriveVideo();
                            mDriveVideo.setLockStatus(data.getLockStatus());
                            mDriveVideo.setId(data.getId());
                            mDriveVideo.setName(data.getName());
                            mDriveVideo.setLocaltion(data.getLocaltion());
                            newlist.add(mDriveVideo);
                        }
                    }
//                    LogUtils.e("WPTAGTest,TYPE_QUART--handler--start---size=" + newlist.size());
                } else {
                    LogUtils.d("WPTAGTest,TYPE_QUART----setNodata---");
                    setNodata();
                    return;
                }
                break;
            case ConstantsData.TYPE_DUAL:
                if (DBUtil.getAllDriveDualVideo() != null) {
                    List<DriveVideoDual> mDriveVideoDual = DBUtil.getLimitDriveDualtVideo(page, 10);
                    for (DriveVideoDual data : mDriveVideoDual) {
                        if (!FileUtils.fileIsExists(data.getName())) {
                            LogUtils.d("updateFileList TYPE_DUAL,,,,dataName:" + data.getName()+" not exist!");
                            DBUtil.deleteDriveDualVideoByName(data.getName());
                        } else {
                            DriveVideo mDriveVideo = new DriveVideo();
                            mDriveVideo.setLockStatus(data.getLockStatus());
                            mDriveVideo.setId(data.getId());
                            mDriveVideo.setName(data.getName());
                            mDriveVideo.setLocaltion(data.getLocaltion());
                            newlist.add(mDriveVideo);
                        }
                    }
//                    LogUtils.e("WPTAGTest,TYPE_DUAL--handler--start---size=" + newlist.size());
                } else {
                    LogUtils.d("WPTAGTest,TYPE_DUAL----setNodata---");
                    setNodata();
                    return;
                }
                break;
            default:
                return;
        }
//        mDataAdapter.setDataList(newlist);
        addItems(newlist);
        mRecyclerView.refreshComplete(newlist.size());
        mLRecyclerViewAdapter.notifyDataSetChanged();
        LogUtils.d("updateFileList--end--" + SystemClock.currentThreadTimeMillis());
    }

    public RecordFilePopWindowTest(Context context, OnFileDismissListener fileDismissListener) {
        this.context = context;
        this.listener = fileDismissListener;
    }

    public void show(View parent, RecordFileManager recordFileManager, AppBaseUI appBaseUI) {
        if (contentView == null) {
            contentView = View.inflate(context, R.layout.record_filemanager_test, null);
        }
        mAppBaseUI = appBaseUI;

        initView(contentView);
//       boolean isSelect =  SPUtils.getBoolean(context,Config.IS_SELECT,false);
        int height = (int) context.getResources().getDimension(R.dimen.record_file_window_height);
        LogUtils.d("WPTAGTest Recordfile  show height=" + height);
        CustomPopWindow.PopupWindowBuilder builder = new CustomPopWindow.PopupWindowBuilder(context);
        popWindow = builder.setView(contentView)
                .size(600, height)
                .setAnimationStyle(R.style.anim_file)
                .setTouchable(true)
                .setFocusable(true)
                .setOnDissmissListener(this)
                .setBackgroundDrawable(context.getResources().getDrawable(
                        R.mipmap.bg_file, context.getTheme()))
                .create()
                .showAtLocation(parent, Gravity.START, 0, 0);
//        if (isSelect){
//            selectEvent();
//        }
        LogUtils.d("Recordfile","Recordfile  showAtLocation");
        mRecordFile = recordFileManager;

        initListView();
    }

    private void initView(View view){
        if(view != null) {
            //选择按钮
            btnVideoSelect = view.findViewById(R.id.btn_videoSelect);
            btnVideoSelect.setOnClickListener(RecordFilePopWindowTest.this);
            //全选按钮
            tvAllSelected = view.findViewById(R.id.tv_all_selected);
            tvAllSelected.setOnClickListener(RecordFilePopWindowTest.this);
            //反按钮
            tvAllSelected_no = view.findViewById(R.id.tv_all_selected_no);
            tvAllSelected_no.setOnClickListener(RecordFilePopWindowTest.this);
            //取消按钮
            tvAllUnselected = view.findViewById(R.id.tv_all_unselected);
            tvAllUnselected.setOnClickListener(RecordFilePopWindowTest.this);
            //删除按钮
            btnDelete = view.findViewById(R.id.btn_delete);
            btnDelete.setOnClickListener(RecordFilePopWindowTest.this);
            //锁定、解锁按钮
            btnLock = view.findViewById(R.id.btn_lock);
            btnLock.setOnClickListener(RecordFilePopWindowTest.this);
            //格式化
            btnFormat = view.findViewById(R.id.btn_videoFormat);
            btnFormat.setOnClickListener(RecordFilePopWindowTest.this);
            //数据列表
            mRecyclerView = view.findViewById(R.id.file_listview);

//            mRecyclerView.setOnItemClickListener(RecordFilePopWindowTest.this);

            //顶部跟底部状态栏
            actionBar = view.findViewById(R.id.myActionBar);
            bottomMenu = view.findViewById(R.id.bottom_menu);
            tvCount = view.findViewById(R.id.tv_count);

            recordBG = view.findViewById(R.id.record_bg_tv);
            mProgressBar = view.findViewById(R.id.progressbar);

            //切换类型按钮
            cardFrontVG = view.findViewById(R.id.card_front);
            cardPostVG = view.findViewById(R.id.card_post);
            cardLeftVG = view.findViewById(R.id.card_left);
            cardRightVG = view.findViewById(R.id.card_right);

            cardPhotoVG = view.findViewById(R.id.card_photo);
            cardQuartVG = view.findViewById(R.id.card_quat);
            cardDualVG = view.findViewById(R.id.card_dual);

            cardFrontVG.setOnClickListener(this);
            cardPostVG.setOnClickListener(this);
            cardLeftVG.setOnClickListener(this);
            cardRightVG.setOnClickListener(this);

            cardPhotoVG.setOnClickListener(this);
            cardQuartVG.setOnClickListener(this);
            cardDualVG.setOnClickListener(this);

            recordData = RecordData.getInstance();

            String curRecordType = recordData.recordType.getValue();
            LogUtils.d("initView-----curRecordType=" + curRecordType);
            if (Config.TYPE_QUART_STREAM.equals(curRecordType)) {
                setViewVisibility(view.findViewById(R.id.rl_card_quart), true);
                setViewVisibility(view.findViewById(R.id.rl_card_post), false);
                setViewVisibility(view.findViewById(R.id.rl_card_leftright), false);
                setViewVisibility(view.findViewById(R.id.rl_card_dual), false);
            } else if (Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
                setViewVisibility(view.findViewById(R.id.rl_card_quart), false);
                setViewVisibility(view.findViewById(R.id.rl_card_post), false);
                setViewVisibility(view.findViewById(R.id.rl_card_leftright), false);
                setViewVisibility(view.findViewById(R.id.rl_card_dual), true);
            } else if (Config.TYPE_TWO_STREAM.equals(curRecordType)) {
                setViewVisibility(view.findViewById(R.id.rl_card_quart), false);
                setViewVisibility(view.findViewById(R.id.rl_card_post), true);
                setViewVisibility(view.findViewById(R.id.rl_card_back), true);
                setViewVisibility(view.findViewById(R.id.rl_card_leftright), false);
                setViewVisibility(view.findViewById(R.id.rl_card_dual), false);
            } else if (Config.TYPE_ONE_STREAM.equals(curRecordType)) {
                setViewVisibility(view.findViewById(R.id.rl_card_quart), false);
                setViewVisibility(view.findViewById(R.id.rl_card_post), true);
                setViewVisibility(view.findViewById(R.id.rl_card_back), false);
                setViewVisibility(view.findViewById(R.id.rl_card_leftright), false);
                setViewVisibility(view.findViewById(R.id.rl_card_dual), false);
            } else {
                setViewVisibility(view.findViewById(R.id.rl_card_quart), false);
                setViewVisibility(view.findViewById(R.id.rl_card_post), true);
                setViewVisibility(view.findViewById(R.id.rl_card_back), true);
                setViewVisibility(view.findViewById(R.id.rl_card_leftright), true);
                setViewVisibility(view.findViewById(R.id.rl_card_dual), false);
            }
        }
    }

    private void setViewVisibility(View view, boolean show) {
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    int index = -1;
    private void setBackground(int index){
        this.index = index;

        switch (index) {
            case ConstantsData.TYPE_FRONT:
                cardFrontVG.setSelected(true);
                cardPostVG.setSelected(false);
                cardLeftVG.setSelected(false);
                cardRightVG.setSelected(false);
                cardPhotoVG.setSelected(false);
                break;
            case ConstantsData.TYPE_BACK:
                cardFrontVG.setSelected(false);
                cardPostVG.setSelected(true);
                cardLeftVG.setSelected(false);
                cardRightVG.setSelected(false);
                cardPhotoVG.setSelected(false);
                break;
            case ConstantsData.TYPE_LEFT:
                cardFrontVG.setSelected(false);
                cardPostVG.setSelected(false);
                cardLeftVG.setSelected(true);
                cardRightVG.setSelected(false);
                cardPhotoVG.setSelected(false);
                break;
            case ConstantsData.TYPE_RIGHT:
                cardFrontVG.setSelected(false);
                cardPostVG.setSelected(false);
                cardLeftVG.setSelected(false);
                cardRightVG.setSelected(true);
                cardPhotoVG.setSelected(false);
                break;
            case ConstantsData.TYPE_IMAGE:
                cardFrontVG.setSelected(false);
                cardPostVG.setSelected(false);
                cardLeftVG.setSelected(false);
                cardRightVG.setSelected(false);
                cardPhotoVG.setSelected(true);
                cardQuartVG.setSelected(false);
                cardDualVG.setSelected(false);
                break;
            case ConstantsData.TYPE_QUART:
                cardPhotoVG.setSelected(false);
                cardQuartVG.setSelected(true);
                break;
            case ConstantsData.TYPE_DUAL:
                cardPhotoVG.setSelected(false);
                cardDualVG.setSelected(true);
                break;
            default:
                return;
        }
        LogUtils.e("setBackground---type=" + index + ", selectState=" + choiceState.get());
        if (choiceState.get()) {
            cancelEvent();
        }
    }

    private void initListView(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        if(context != null) {
            typeflag = ConstantsData.TYPE_FRONT;
            String curRecordType = RecordData.getInstance().recordType.getValue();
            if (Config.TYPE_QUART_STREAM.equals(curRecordType)) {
                typeflag = ConstantsData.TYPE_QUART;
            } else if (Config.TYPE_DUAL_STREAM.equals(curRecordType)) {
                typeflag = ConstantsData.TYPE_DUAL;
            }

            setBackground(typeflag);
            LogUtils.d("typeflag:" + typeflag);
            //初始化适配器
            mDataAdapter = new RecordFileAdapterTest( context,false,typeflag);
            mLRecyclerViewAdapter = new LRecyclerViewAdapter(mDataAdapter);

            LogUtils.e("initListView----start---" + SystemClock.currentThreadTimeMillis());
            mProgressBar.setVisibility(View.VISIBLE);
            newlist.clear();
            mRecyclerView.setAdapter(mLRecyclerViewAdapter);

            //设置头部加载颜色
            mRecyclerView.setHeaderViewColor(R.color.BLACK, R.color.dark , R.color.BLACK);
            //设置底部加载颜色
            mRecyclerView.setFooterViewColor(R.color.BLACK, R.color.dark , R.color.BLACK);
            //设置底部加载文字提示
            mRecyclerView.setFooterViewHint("拼命加载中","已经全部为你呈现了","网络不给力啊，点击再试一次吧");

             itemDecoration= new DividerDecoration.Builder(context).setHeight(R.dimen.default_divider_height)
                    .setPadding(R.dimen.default_divider_height)
                    .setColorResource(R.color.dark)
                    .build();
            LogUtils.d("mRecyclerView.getItemDecorationCount：" + mRecyclerView.getItemDecorationCount());
            if (mRecyclerView.getItemDecorationCount() == 0 ) {
                mRecyclerView.addItemDecoration(itemDecoration);
            }
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh() {
                    LogUtils.d("WPTAG,onRefresh,,,typeflag:" +typeflag);
                    mCurrentCounter = 0;
                    mDataAdapter.clear();
                    page =1;
                    requestData(page,typeflag);
                }
            });

            mRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    if (mCurrentCounter < TOTAL_COUNTER) {
                        // loading more
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                LogUtils.d("WPTAGTest,page++++++++++:" + page + ", isFileShowing=" + isFileShowing());
                                if (isFileShowing()) {
                                    isonLoadMore = true;
                                    page++;
                                    requestData(page, typeflag);
                                }
                            }
                        },500);
                    } else {
                        LogUtils.d("WPTAGTest,page+end++++:" + page + " size:" + mDataAdapter.getDataList().size());
                        if (mDataAdapter.getDataList().size()>0){
                            //the end
                            mRecyclerView.setNoMore(true);
                        }else {
                            setNodata();
                        }
                    }
                }
            });
//            mRecyclerView.refresh();
//            mDataAdapter.setIsImag(false);
//            mDataAdapter.notifyDataSetChanged();
            mLRecyclerViewAdapter.setOnItemClickListener(this);
//            handler.sendEmptyMessage(4);

            mDataAdapter.setIsImag(false);
            handler.sendEmptyMessage(typeflag);
            LogUtils.e("WPTAGTest,initListView----end---" + SystemClock.currentThreadTimeMillis());
        }
    }

    private void requestData(int page, int type) {
        if (page == 1) {
            if (type == ConstantsData.TYPE_FRONT) {
                List<DriveVideoFont> list = DBUtil.getAllDriveFontVideo();
                if (null != list) TOTAL_COUNTER = list.size();
            } else if (type == ConstantsData.TYPE_BACK) {
                List<DriveVideoBack> list = DBUtil.getAllDriveBackVideo();
                if (null != list) TOTAL_COUNTER = list.size();
            } else if (type == ConstantsData.TYPE_LEFT) {
                List<DriveVideoLeft> list = DBUtil.getAllDriveLeftVideo();
                if (null != list) TOTAL_COUNTER = list.size();
            } else if (type == ConstantsData.TYPE_RIGHT) {
                List<DriveVideoRight> list = DBUtil.getAllDriveRightVideo();
                if (null != list) TOTAL_COUNTER = list.size();
            } else if (type == ConstantsData.TYPE_IMAGE) {
                TOTAL_COUNTER = 100000;
            } else if (type == ConstantsData.TYPE_QUART) {
                List<DriveVideoQuart> list = DBUtil.getAllDriveQuartVideo();
                if (null != list) TOTAL_COUNTER = list.size();
            } else if (type == ConstantsData.TYPE_DUAL) {
                List<DriveVideoDual> list = DBUtil.getAllDriveDualVideo();
                if (null != list) TOTAL_COUNTER = list.size();
            }
        }

        LogUtils.d("WPTAGTest,TOTAL_COUNTER :" + TOTAL_COUNTER + "  page:" + page + " type:" + type);
        int location = recordData.mutableLocation;
        if (!FileUtils.isDiskMounted(context, StorageDevice.getPath(location))) {
            LogUtils.e("WPTAGTest,requestData--return location=" + location + " is not exist!");
            setNodata();
            return;
        }

        mDataAdapter.setTypeflag(typeflag);

        File dir = new File(getVideoDir(location));
        if (!dir.exists() || TOTAL_COUNTER == 0) {
            mDataAdapter.notifyDataSetChanged();
            try {
                DBUtil.deleteAllDriveVideo();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setNodata();
            return;
        }

        LogUtils.e("WPTAGTest,requestData----start---" + SystemClock.currentThreadTimeMillis() + ", type=" + type);
        updateFileList(typeflag);
        LogUtils.d("WPTAGTest ,requestData----end---" + SystemClock.currentThreadTimeMillis());
    }

    private void setNodata(){
        mRecyclerView.refreshComplete(0);
        mRecyclerView.setNoMore(true);
    }

    private void addItems( List<DriveVideo> list ) {
        LogUtils.d("addItems-----size=" + (list == null ? "null" : list.size()));
        mDataAdapter.addAll(list);
        mCurrentCounter += list.size();
        mRecyclerView.refreshComplete(list.size());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isonLoadMore = false;
            }
        },500);
    }

    private String getVideoDir(int location) {
        return StorageDevice.getPath(location) + ConstantsData.VIDEO_DIR;
    }

    private CommonDialog formatDialog;
    @Override
    public void onClick(View v) {
        LogUtils.d("WPTAGTest,11111111111111111111111111111");
      /*  if (ClickFilter.filter(400L)) {
            return;
        }
        if (mProgressBar.isShown()) {
            LogUtils.d("WPTAGTest,onClick-----return!");
            return;
        }*/
        switch (v.getId()){
            case R.id.btn_videoSelect://选择
                if (mDataAdapter.getDataList().size()== 0){
                 ToastUtils.showShort(context.getString(R.string.str_not_data));
                    return;
                }
                tvAllSelected.setVisibility(View.VISIBLE);
                tvAllSelected_no.setVisibility(View.GONE);
                selectEvent();
                break;
            case R.id.tv_all_selected://全选
                tvAllSelected.setVisibility(View.GONE);
                tvAllSelected_no.setVisibility(View.VISIBLE);
                selectAll();
                break;
            case R.id.tv_all_selected_no://反选
                tvAllSelected.setVisibility(View.VISIBLE);
                tvAllSelected_no.setVisibility(View.GONE);
                selectAllNo();
                break;
            case R.id.tv_all_unselected://取消
                cancelEvent();
                break;
            case R.id.btn_delete://删除
                if (tvCount.getText().toString().equals(context.getString(R.string.checked) + "0")) return;
                mProgressBar.setVisibility(View.VISIBLE);
                deleteSelected();
                btnVideoSelect.setVisibility(View.VISIBLE);
                btnFormat.setVisibility(View.VISIBLE);
                tvAllSelected.setVisibility(View.GONE);
                tvAllSelected_no.setVisibility(View.GONE);
                break;
            case R.id.btn_lock://锁定
                mProgressBar.setVisibility(View.VISIBLE);
                lockOrUnlock();
                break;
            case R.id.card_front: //前置
                if (ClickFilter.filter(FILTERTIME)) {
                    return;
                }
                mDataAdapter.setIsImag(false);
                LogUtils.d("WPTAGTest,-----前置:" );
                handler.sendEmptyMessage(ConstantsData.TYPE_FRONT);
//                mRecyclerView.setSelection(0);
                break;
            case  R.id.card_post://后置
                if (ClickFilter.filter(FILTERTIME)) {
                    return;
                }
                LogUtils.d("WPTAGTest,-----后置:"  + mDataAdapter);
                mDataAdapter.setIsImag(false);
                handler.sendEmptyMessage(ConstantsData.TYPE_BACK);
//                mRecyclerView.setSelection(0);
                break;
            case  R.id.card_left://左
                if (ClickFilter.filter(FILTERTIME)) {
                    return;
                }
                LogUtils.d("WPTAGTest,-----后置:"  + mDataAdapter);
                mDataAdapter.setIsImag(false);
                handler.sendEmptyMessage(ConstantsData.TYPE_LEFT);
//                mRecyclerView.setSelection(0);
                break;
            case  R.id.card_right://右
                if (ClickFilter.filter(FILTERTIME)) {
                    return;
                }
                LogUtils.d("WPTAGTest,-----后置:"  + mDataAdapter);
                mDataAdapter.setIsImag(false);
                handler.sendEmptyMessage(ConstantsData.TYPE_RIGHT);
//                mRecyclerView.setSelection(0);
                break;
            case R.id.card_photo://图片
                if (ClickFilter.filter(FILTERTIME)) {
                    return;
                }
                mDataAdapter.setIsImag(true);
                LogUtils.d("WPTAGTest,-----图片:" );
                handler.sendEmptyMessage(ConstantsData.TYPE_IMAGE);
//                mRecyclerView.setSelection(0);
                break;
            case R.id.card_quat://四合一
                if (ClickFilter.filter(FILTERTIME)) {
                    return;
                }
                mDataAdapter.setIsImag(false);
                handler.sendEmptyMessage(ConstantsData.TYPE_QUART);
                break;
            case R.id.card_dual://二合一
                if (ClickFilter.filter(FILTERTIME)) {
                    return;
                }
                mDataAdapter.setIsImag(false);
                handler.sendEmptyMessage(ConstantsData.TYPE_DUAL);
                break;
            case R.id.btn_videoFormat://格式化
                int deviceId = RecordData.getInstance().mutableLocation;//StorageDevice.MEDIA_CARD;//
                String path = StorageDevice.getPath(context, deviceId);//"/storage/sdcard1/";//
                LogUtils.d("Format---path=" + path);
                if (!FileUtils.isDiskMounted(context, path)) {
                    ToastUtils.showShort(context.getString(R.string.nodevice));
                } else {
                    formatDialog = CommonDialog.getInstance(context);
                    if (!formatDialog.getIsShowing()) {
                        formatDialog.setMessage(context.getString(R.string.str_format_info))
                                .setSingle(false)
                                .setBeanList(false)
                                .setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
                                    @Override
                                    public void onPositiveClick(List<CommonBean> list) {
                                        LogUtils.e("onPositiveClick");
                                        formatDialog.onDismiss();
                                        if (listener != null) {
                                            listener.onFormatClick(path);
                                        }
                                    }

                                    @Override
                                    public void onNegtiveClick() {
                                        formatDialog.onDismiss();
                                    }
                                })
                                .show();
                    }
                }
                break;
        }
    }

    private void selectAll (){
        if (mDataAdapter != null) {
            for (int position = 0; position < mDataAdapter.getDataList().size(); position++) {
                mDataAdapter.isCheckMap.put(position, true);
            }
            mDataAdapter.notifyDataSetChanged();
        }
        updateItemSelected();
    }

    private void selectAllNo (){
        if (mDataAdapter != null) {
            for (int position = 0; position < mDataAdapter.getDataList().size(); position++) {
                mDataAdapter.isCheckMap.put(position, false);
                mDataAdapter.notifyDataSetChanged();
            }
        }
        updateItemSelected();
    }

//    /**
//     * 显示已选择了多少个选项文本信息
//     */
//    private void updateItemSelected() {
//        if (context != null && mDataAdapter.isCheckMap != null) {
//            int count = 0;
//            for (Map.Entry<Integer, Boolean> entry : mDataAdapter.isCheckMap.entrySet()) {
//                if(entry.getValue()){
//                    count++;
//                }
//            }
//            tvCount.setText(context.getString(R.string.checked) + count);
//        }
//    }

    /**
     * 点击选择时的事件
     */
    private void selectEvent(){
//        SPUtils.putBoolean(context,Config.IS_SELECT,true);
        choiceState.set(true);

        if (mDataAdapter != null && mDataAdapter.getIsImag()) {
            btnLock.setVisibility(View.GONE);
            recordBG.setVisibility(View.GONE);
        } else {
            btnLock.setVisibility(View.VISIBLE);
            recordBG.setVisibility(View.VISIBLE);
        }
        btnDelete.setVisibility(View.VISIBLE);
        actionBar.setVisibility(View.VISIBLE);
        bottomMenu.setVisibility(View.VISIBLE);
        setPlayImgShow(true);
        updateItemSelected();
    }

    /**
     * 点击取消按钮事件
     */
    private void cancelEvent(){
        choiceState.set(false);
//        SPUtils.putBoolean(context,Config.IS_SELECT,false);
        if (mDataAdapter != null && mDataAdapter.getIsImag()) {
            btnLock.setVisibility(View.VISIBLE);
            recordBG.setVisibility(View.VISIBLE);
        } else {
            btnLock.setVisibility(View.GONE);
            recordBG.setVisibility(View.GONE);
        }
        actionBar.setVisibility(View.GONE);
        bottomMenu.setVisibility(View.GONE);
        setPlayImgShow(false);
        updateItemSelected();
        if (mDataAdapter != null && mDataAdapter.isCheckMap != null) {
            mDataAdapter.isCheckMap.clear();
            mDataAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        boolean isSelect = choiceState.get();
        LogUtils.d("WPTAG,onItemClick--->" + isSelect);

        if (!isSelect){
            String path = mDataAdapter.getDataList().get(position).getName();
            if (!Utils.isImageFile(path)) {
                if (mmr == null) {
                    mmr = new MediaMetadataRetriever();
                }

                try {
                    mmr.setDataSource(path);
                    // 播放时长单位为毫秒
                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                    LogUtils.d("WPTAG", "RecordFilePopWindow(onItemClick)----path-------------->" + path + " --------duration->" + duration);
                    if (duration != null && duration.equals("0")) {
                        ToastUtils.showShort(context.getString(R.string.video_creating));
                        return;
                    }

                    File file = new File(path);
                    if (file.exists()) {
                        long size = FileUtils.getFileSize(file);
                        LogUtils.d("WPTAG", "RecordFilePopWindow(onItemClick)------------------>size=" + size);
                        if (size == 0) {
                            ToastUtils.showShort(context.getString(R.string.file_size_error));
                            deleteFromDisk(path);
                            return;
                        }
                    }
                } catch (Exception e) {
                    LogUtils.e("WPTAG", "RecordFilePopWindow(onItemClick)----return error----->path=" + path);
                    e.printStackTrace();
                    return;
                }
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                try {
//                    Uri data = Uri.parse("file://" + path);
//                    intent.setDataAndType(data, "video/mp4");
//                    context.startActivity(intent);
//                } catch (android.content.ActivityNotFoundException e) {
//                    e.printStackTrace();
//                }

                listener.onVideoClick(position,mDataAdapter.getDataList());
            } else {
                String imgFile = mDataAdapter.getDataList().get(position).getName();
                LogUtils.d("WPTAG,跳轉至圖片預覽界面");
                if (FileUtils.fileIsExists(imgFile)) {
                    listener.onImgClick(imgFile);
                } else {
                    ToastUtils.showShort(context.getString(R.string.file_error_refresh));
                    mRecyclerView.refresh();
                }

//                Intent intent = new Intent(context, ImgActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra("imgpath", mDataAdapter.getDataList().get(position).getName());
//                context.startActivity(intent);
            }

            //通知停止录制并退出
//            RxBus.getDefault().post(new Event(Event.EVENT_STOP_RECORD_AND_EXIT));
        } else {
            if (mDataAdapter != null) {
                if (mDataAdapter.isCheckMap.get(position)){
                    mDataAdapter.isCheckMap.put(position, false);
                }else {
                    mDataAdapter.isCheckMap.put(position, true);
                }
                updateItemSelected();
                mDataAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 锁定或解锁按钮事件
     */
    public void lockOrUnlock() {
        if (mDataAdapter != null && mDataAdapter.isCheckMap != null && mDataAdapter.getDataList() != null) {
            LogUtils.d("lockOrUnlock----size=" + mDataAdapter.isCheckMap.size());
            List<DriveVideo> updateList = new ArrayList<>();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int type = mDataAdapter.getTypeflag();
                        for (Map.Entry<Integer, Boolean> entry : mDataAdapter.isCheckMap.entrySet()) {
                            //选项是否被选中的
                            if (entry.getValue()) {
                                final int position = entry.getKey();
                                DriveVideo driveVideo = mDataAdapter.getDataList().get(position);
                                if (driveVideo != null) {
                                    final boolean lock = driveVideo.getLockStatus();
                                    driveVideo.setLockStatus(!lock);
                                    mRecordFile.updateDriveVideo(type, driveVideo.getName(), position, !lock);
                                    updateList.add(driveVideo);
                                }
                            }
                        }

                        mRecordFile.updateDBUtilLockStatus(updateList, type);//更新数据库

                        handler.sendEmptyMessage(7);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * 删除按钮事件
     */
    private void deleteSelected() {
        LogUtils.d("WPTAG，deleteSelected ---size=" + mDataAdapter.isCheckMap.size());
        List<String> pathList = new ArrayList<String>();
        if (mDataAdapter!=null && mDataAdapter.isCheckMap!=null){
            for (Map.Entry<Integer,Boolean> entry: mDataAdapter.isCheckMap.entrySet()){
                if (entry.getValue()){
                    LogUtils.d("WPTAG，已经被选中，开始准备执行删除。");
                    int position = entry.getKey();
                    boolean lock = mDataAdapter.getDataList().get(position).getLockStatus();
                    if (lock && typeflag != ConstantsData.TYPE_IMAGE) {
                        //加锁视频不删除
                        continue;
                    }
                    String path = mDataAdapter.getDataList().get(position).getName();
                    LogUtils.d("WPTAG，删除：" + path + "  type:"+ typeflag + " position:" + position);
                    if(path != null) {
                        pathList.add(path);
                        if(typeflag == ConstantsData.TYPE_IMAGE){
                            mRecordFile.deleteDriveVideo(ConstantsData.TYPE_IMAGE, path, position);
                        }
                    }
                }
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (pathList.size() > 0) {
                        for (String name : pathList) {
                            deleteFromDisk(name);
                        }
                    }
                }
            }).start();
            LogUtils.d("WPTAG,再更新列表数据............");
            handler.sendEmptyMessageDelayed(6,2000);
        }
    }

    /**
     * 删除指定路径下的文件
     * @param path
     */
    private void deleteFromDisk(String path) {
        LogUtils.d("WPTAG，deleteFromDisk 删除：" + path + "  type:"+ typeflag);
        if (path.contains(Config.VIDEO_FLAG) || path.contains(Config.VIDEO_FLAG_TS)) {
            if (typeflag == ConstantsData.TYPE_FRONT) {
                DBUtil.deleteDriveFontVideoByName(path);
            } else if (typeflag == ConstantsData.TYPE_BACK) {
                DBUtil.deleteDriveBackVideoByName(path);
            } else if (typeflag == ConstantsData.TYPE_LEFT) {
                DBUtil.deleteDriveLeftVideoByName(path);
            } else if (typeflag == ConstantsData.TYPE_RIGHT) {
                DBUtil.deleteDriveRightVideoByName(path);
            } else if (typeflag == ConstantsData.TYPE_QUART) {
                DBUtil.deleteDriveQuartVideoByName(path);
            } else if (typeflag == ConstantsData.TYPE_DUAL) {
                DBUtil.deleteDriveDualVideoByName(path);
            }
        }
        FileUtils.deleteFile(path);
    }

    /**
     * 设置显示播放图标或选择图标
     * @param isSelect
     */
    private void setPlayImgShow(boolean isSelect){
        if (mDataAdapter != null) {
            mDataAdapter.selectImgState(isSelect);
            mDataAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 显示已选择了多少个选项文本信息
     */
    private void updateItemSelected() {
        if (context != null && mDataAdapter != null && mDataAdapter.isCheckMap != null) {
            int count = 0;
            for (Map.Entry<Integer, Boolean> entry : mDataAdapter.isCheckMap.entrySet()) {
                if(entry.getValue()){
                    count++;
                }
            }
            tvCount.setText(context.getString(R.string.checked) + count);
        }
    }

    public void dismiss() {
        LogUtils.d("FilePopWindow dismiss!");
        if(popWindow != null){
            if(popWindow.isShowing()){
                popWindow.dissmiss();
            }
        }
    }

    public boolean isFileShowing() {
        if (popWindow != null && popWindow.isShowing()) {
            return true;
        }

        return false;
    }

    @Override
    public void onDismiss() {
        LogUtils.d("FilePopWindow onDismiss!");
//        setNodata();//解决在刷新列表时马上切换界面，会出现列表一直为空的情况。
        if (mRecyclerView != null) {
            mRecyclerView.refreshComplete(0);
        }

        if (mDataAdapter != null && mDataAdapter.isCheckMap != null) {
            mDataAdapter.isCheckMap.clear();
        }
        cancelEvent();

        if (formatDialog != null && formatDialog.isShowing()) {
            formatDialog.onDismiss();
        }

        if(listener != null){
            listener.onFileDismiss();
        }
    }

    public interface OnFileDismissListener {
        void onFileDismiss();
        void onVideoClick(int position, List<DriveVideo> list);
        void onImgClick(String path);
        void onFormatClick(String path);
    }
}
