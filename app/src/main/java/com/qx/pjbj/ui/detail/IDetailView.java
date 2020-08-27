package com.qx.pjbj.ui.detail;

import com.qx.pjbj.view.loading.ILoadingView;

/**
 * Create by QianXiao
 * On 2020/7/28
 */
public interface IDetailView extends ILoadingView {
    void onRefresh(boolean isshowloadingdialog);
}
