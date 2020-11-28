package com.wwc2.dvr.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * user: wangpeng on 2019/7/16.
 * emai: wpeng@waterworld.com.cn
 */

public  abstract  class ListBaseAdapter<T> extends RecyclerView.Adapter<SuperViewHolder> {

    protected Context mContext;
    private LayoutInflater mInflater;
    protected ArrayList<T> mDataList = new ArrayList<>();


    public ListBaseAdapter(Context mContext) {
        this.mContext = mContext;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public SuperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(getLayoutId(), parent, false);
        return new SuperViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SuperViewHolder holder, int position) {
        onBindItemHolder(holder, position);
    }

    //局部刷新关键：带payload的这个onBindViewHolder方法必须实现
    @Override
    public void onBindViewHolder(SuperViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            onBindItemHolder(holder, position, payloads);
        }
    }

    public abstract int getLayoutId();

    public abstract void onBindItemHolder(SuperViewHolder holder, int position);

    public void onBindItemHolder(SuperViewHolder holder, int position, List<Object> payloads){}

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    /**
     * 获得数据集合对象
     * @return
     */
    public List<T> getDataList() {
        return mDataList;
    }

    /**
     * 重新输入集合数据
     * @param list
     */
    public void setDataList(Collection<T> list) {
        this.mDataList.clear();
        this.mDataList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * 添加一个集合的数据
     * @param list
     */
    public void addAll(Collection<T> list) {
        int lastIndex = this.mDataList.size();
        if (this.mDataList.addAll(list)) {
            notifyItemRangeInserted(lastIndex, list.size());
        }
    }

    /**
     * 添加一条数据
     * @param item
     */
    public void add(T item) {
        int lastIndex = this.mDataList.size();
        if (this.mDataList.add(item)) {
            notifyItemRangeInserted(lastIndex, 1);
        }
    }

    /**
     * 删除一条数据
     * @param position
     */
    public void remove(int position) {
        this.mDataList.remove(position);
        notifyItemRemoved(position);
        if(position != (getDataList().size())){ // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(position,this.mDataList.size()-position);
        }
    }

    /**
     * 清除数据
     */
    public void clear() {
        mDataList.clear();
        notifyDataSetChanged();
    }
}