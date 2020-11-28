package com.wwc2.dvr.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.dvr.R;
import com.wwc2.dvr.bean.CommonBean;
import com.wwc2.dvr.data.CommonAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * description ： TODO:类的作用
 * user: wangpeng on 2020/4/29.
 * emai: wpeng@waterworld.com.cn
 */
public class CommonDialog extends Dialog implements OnItemClickListener {
    /**
     * 显示的图片
     */
    private ImageView imageIv ;

    /**
     * 显示的标题
     */
    private TextView titleTv ;

    /**
     * 显示的消息
     */
    private TextView messageTv ;

    /**
     * 确认和取消按钮
     */
    private TextView negtiveBn ,positiveBn;

    private LRecyclerView mRecyclerView;

    private CommonAdapter mCommonAdapter;
    private LRecyclerViewAdapter mLRecyclerViewAdapter;

    private Context context;
    /**
     * 按钮之间的分割线
     */
    private View columnLineView ;
    public static CommonDialog mCommonDialog = null;

    public CommonDialog(Context context) {
        super(context, R.style.CustomDialog);
        this.context = context;

        LogUtils.d("WPTAG", "CommonDialog----------------mHasList=" + mHasList);
        mHasList = true;
    }

    public static CommonDialog getInstance(Context context){
        if (mCommonDialog ==null){
            mCommonDialog = new CommonDialog(context);
        }
        return  mCommonDialog;
    }

    public boolean getIsShowing(){
        return isShowing();
    }

    /**
     * 都是内容数据
     */
    private String message;
    private String title;
    private String positive,negtive ;
    private int imageResId = -1 ;

    /**
     * 底部是否只有一个按钮
     */
    private boolean isSingle = false;

    private boolean mHasList = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        setContentView(R.layout.dialog_layout_main);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //初始化界面数据
        refreshView();
        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        positiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onPositiveClick(mCommonAdapter.getDataList());
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        negtiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( onClickBottomListener!= null) {
                    onClickBottomListener.onNegtiveClick();
                }
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void refreshView() {
        //如果用户自定了title和message
        if (!TextUtils.isEmpty(title)) {
            titleTv.setText(title);
            titleTv.setVisibility(View.VISIBLE);
        }else {
            titleTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(message)) {
            messageTv.setText(message);
        }
        //如果设置按钮的文字
        if (!TextUtils.isEmpty(positive)) {
            positiveBn.setText(positive);
        }else {
            positiveBn.setText(context.getString(R.string.ok));
        }
        if (!TextUtils.isEmpty(negtive)) {
            negtiveBn.setText(negtive);
        }else {
            negtiveBn.setText(context.getString(R.string.cancel));
        }

        if (imageResId!=-1){
            imageIv.setImageResource(imageResId);
            imageIv.setVisibility(View.VISIBLE);
        }else {
            imageIv.setVisibility(View.GONE);
        }
        /**
         * 只显示一个按钮的时候隐藏取消按钮，回掉只执行确定的事件
         */
        if (isSingle){
            columnLineView.setVisibility(View.GONE);
            negtiveBn.setVisibility(View.GONE);
        }else {
            negtiveBn.setVisibility(View.VISIBLE);
            columnLineView.setVisibility(View.VISIBLE);
        }

        mCommonAdapter = new CommonAdapter(context);
        mLRecyclerViewAdapter = new LRecyclerViewAdapter(mCommonAdapter);
        mRecyclerView.setAdapter(mLRecyclerViewAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setLoadMoreEnabled(false);
        LogUtils.d("WPTAG", "refreshView----------------mHasList=" + mHasList);
        if (mHasList) {
            mCommonAdapter.addAll(getList());
        }

        mLRecyclerViewAdapter.setOnItemClickListener(this);
    }

    private List<CommonBean> getList(){
        List<CommonBean> commonBeanList = new ArrayList<>();
        commonBeanList.add(new CommonBean(2,context.getString(R.string.common_dialog_str2),true));
        commonBeanList.add(new CommonBean(1,context.getString(R.string.common_dialog_str1),false));
        return commonBeanList;
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        negtiveBn = (TextView) findViewById(R.id.negtive);
        positiveBn = (TextView) findViewById(R.id.positive);
        titleTv = (TextView) findViewById(R.id.title);
        messageTv = (TextView) findViewById(R.id.message);
        imageIv = (ImageView) findViewById(R.id.image);
        columnLineView = findViewById(R.id.column_line);
        mRecyclerView = findViewById(R.id.recyclerview);
    }

    /**
     * 设置确定取消按钮的回调
     */
    public OnClickBottomListener onClickBottomListener;
    public CommonDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }

    @Override
    public void onItemClick(View view, int position) {
        LogUtils.d("WPTAG","onItemClick----------------");
        if (mCommonAdapter.getDataList().get(position).isCheck()){
            mCommonAdapter.getDataList().get(position).setCheck(false);
        }else {
            mCommonAdapter.getDataList().get(position).setCheck(true);
        }
        mCommonAdapter.notifyDataSetChanged();

    }

    public interface OnClickBottomListener{
        /**
         * 点击确定按钮事件
         */
        public void onPositiveClick(List<CommonBean> list);
        /**
         * 点击取消按钮事件
         */
        public void onNegtiveClick();
    }

    public String getMessage() {
        return message;
    }

    public CommonDialog setMessage(String message) {
        this.message = message;
        return this ;
    }

    public String getTitle() {
        return title;
    }

    public CommonDialog setTitle(String title) {
        this.title = title;
        return this ;
    }

    public String getPositive() {
        return positive;
    }

    public CommonDialog setPositive(String positive) {
        this.positive = positive;
        return this ;
    }

    public String getNegtive() {
        return negtive;
    }

    public CommonDialog setNegtive(String negtive) {
        this.negtive = negtive;
        return this ;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public CommonDialog setSingle(boolean single) {
        isSingle = single;
        return this ;
    }

    public CommonDialog setImageResId(int imageResId) {
        this.imageResId = imageResId;
        return this ;
    }

    public CommonDialog setBeanList(boolean isHasList) {
        LogUtils.d("WPTAG", "setBeanList----------------mHasList=" + mHasList);
        mHasList = isHasList;
        return this;
    }

    public void onDismiss() {
        LogUtils.d("WPTAG", "onDismiss----------------mHasList=" + mHasList);
        mHasList = true;
        this.dismiss();
    }
}