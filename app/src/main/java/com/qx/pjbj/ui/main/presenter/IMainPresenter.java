package com.qx.pjbj.ui.main.presenter;

import com.tencent.tauth.IUiListener;

/**
 * Create by QianXiao
 * On 2020/4/15
 */
public interface IMainPresenter {
    void OnRefresh();

    void LoginInit();

    void Login();

    boolean isQQInstalled();

    IUiListener getLoginIUiListener();

    void LoadMoreData(int start,int num);

    void SearchNote(String s);

    void loadMyShare();

    void loadMyLike();

    void getNoReadMsgNum();
}
