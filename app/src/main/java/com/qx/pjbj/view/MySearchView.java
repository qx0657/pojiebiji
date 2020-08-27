package com.qx.pjbj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import com.blankj.utilcode.util.ToastUtils;

/**
 * Create by QianXiao
 * 实现展开监听
 * com.qx.pjbj.view.MySearchView
 * On 2020/7/24
 */
public class MySearchView extends SearchView {

    public interface OnSearchViewActionExpandListener{
        void onActionExpand();
        void onActionCollapse();
    }

    public MySearchView(Context context) {
        super(context);
    }

    public MySearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setActionExpandListener(final OnSearchViewActionExpandListener actionExpandListener) {
        this.setOnSearchClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                actionExpandListener.onActionExpand();
            }
        });
        this.setOnCloseListener(new OnCloseListener() {
            @Override
            public boolean onClose() {
                actionExpandListener.onActionCollapse();
                return false;
            }
        });
    }

}
