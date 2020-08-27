package com.qx.pjbj.ui.manage;

import com.qx.pjbj.data.PjNote;
import com.qx.pjbj.view.loading.ILoadingView;

import java.util.List;

/**
 * Create by QianXiao
 * On 2020/8/22
 */
public interface IManageView extends ILoadingView {
    void requestData(boolean showtip);
    void showData(List<PjNote> pjNotes);
}
