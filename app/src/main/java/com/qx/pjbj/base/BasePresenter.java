package com.qx.pjbj.base;

import android.content.Context;

/**
 * Create by QianXiao
 * On 2020/4/15
 */
public abstract class BasePresenter<T,V> {
    public Context context;
    /**
     * View层接口对象
     */
    public T mView;
    /**
     * Model层接口对象
     */
    public V mModel;

    public BasePresenter(Context context) {
        this.context = context;
        mModel = initModel();
    }

    protected abstract V initModel();

    /**
     * 绑定view层接口
     * @param mView
     */
    public void attach(T mView){
        this.mView = mView;
    }


}
