package com.qx.pjbj.ui.message.view;

import com.qx.pjbj.ui.message.data.Message;
import com.qx.pjbj.view.loading.ILoadingView;

import java.util.List;

/**
 * Create by QianXiao
 * On 2020/8/24
 */
public interface IMessageView extends ILoadingView {
    void onRefresh();
    void showMessage(List<Message> messages);
}
