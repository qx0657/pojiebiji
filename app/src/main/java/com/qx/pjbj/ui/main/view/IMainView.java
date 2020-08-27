package com.qx.pjbj.ui.main.view;

import com.qx.pjbj.data.PjNote;
import com.qx.pjbj.view.loading.ILoadingView;

import java.util.List;

/**
 * Create by QianXiao
 * On 2020/4/15
 */
public interface IMainView extends ILoadingView {
    void Toast(String s);

    void RefreshDate(List<PjNote> data);

    void addData(List<PjNote> data);

    void loginOut();

    void loginSuccess(String nick,String head);

    void ktVipSuccess();

    void changeState(int s);

    void setTile(String title);

    void showNoReadMsgNum(int num);
}
