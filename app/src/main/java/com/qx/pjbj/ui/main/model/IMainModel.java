package com.qx.pjbj.ui.main.model;

import com.qx.pjbj.data.PjNote;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import java.util.List;

/**
 * Create by QianXiao
 * On 2020/4/15
 */
public interface IMainModel {


    void OnRefreshData(RefreshCallback callback);

    interface RefreshCallback{
        /**
         *
         * @param a 如果a=1,为初始加载数据 。a=2，为我的分享。a=3，为我的收藏。
         * @param data
         */
        void RefreshSuccess(int a,List<PjNote> data);
        void RefreshError(String e);
    }

    void QQLogin(Tencent tencent);

    IUiListener getIUiListener();

    void LoadMore(int start,int count,LoadMoreCallback callback);

    interface LoadMoreCallback{
        void onLoadMoreDataSuccess(List<PjNote> data);
        void onLoadMoreDataFail(int code,String e);
    }

    void SearchGame(String key,SearchGameCallback callback);

    interface SearchGameCallback{
        void onSearchGameSuccess(List<PjNote> data);
        void onSearchGameFail(String e);
    }

    void LoadMyShare(RefreshCallback callback);

    void LoadMyLike(RefreshCallback callback);

    interface GetNoReadMsgNumCallBack{
        void ongetNoReadMsgNumSuccess(int noReadMsgNum);
    }
    void getNoReadMsgNum(GetNoReadMsgNumCallBack callBack);
}
