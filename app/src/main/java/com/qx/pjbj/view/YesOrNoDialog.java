package com.qx.pjbj.view;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ScreenUtils;
import com.qx.pjbj.R;
import com.qx.pjbj.base.BaseAlertDialog;

/**
 * Create by QianXiao
 * On 2020/7/24
 */
public class YesOrNoDialog extends BaseAlertDialog implements View.OnClickListener{
    private TextView tv_yes, tv_no, tv_content;
    private String yes_text = "确定", no_text = "取消", content_text = "";
    private YesOrNoDialogInterface mYesOrNoDialogInterface;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_yes_inyesornodialog:
                if(mYesOrNoDialogInterface!=null){
                    mYesOrNoDialogInterface.yesOnClick(this);
                }
                break;
            case R.id.tv_no_inyesornodialog:
                if(mYesOrNoDialogInterface!=null){
                    mYesOrNoDialogInterface.noOnClick(this);
                }
                break;
        }
    }

    public interface YesOrNoDialogInterface {
        void yesOnClick(YesOrNoDialog d);
        void noOnClick(YesOrNoDialog d);
    }

    public YesOrNoDialog(@NonNull Context context) {
        super(context);
    }

    public YesOrNoDialog(Context context, YesOrNoDialogInterface i) {
        super(context);
        this.mYesOrNoDialogInterface = i;
    }

    public YesOrNoDialog setmYesOrNoDialogInterface(
            YesOrNoDialogInterface mYesOrNoDialogInterface) {
        this.mYesOrNoDialogInterface = mYesOrNoDialogInterface;
        return this;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.dialog_yesorno;
    }

    public YesOrNoDialog setContent(String c) {
        this.content_text = c;
        tv_content.setText(c);
        return this;
    }

    public YesOrNoDialog setYesText(String c) {
        this.yes_text = c;
        tv_yes.setText(c);
        return this;
    }

    public YesOrNoDialog setNoText(String c) {
        this.no_text = c;
        tv_no.setText(c);
        return this;
    }
    public YesOrNoDialog setOnlyYesBtn(){
        tv_no.setVisibility(View.GONE);
        tv_yes.setBackground(ContextCompat.getDrawable(context,
                R.drawable.btn_bg_middle));
        return this;
    }

    @Override
    protected void initView() {
        tv_yes = f(R.id.tv_yes_inyesornodialog);
        tv_no = f(R.id.tv_no_inyesornodialog);
        tv_content = f(R.id.tv_content_inyesornodialog);
    }

    @Override
    protected void initListener() {
        tv_yes.setOnClickListener(this);
        tv_no.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        assert window != null;
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (ScreenUtils.getAppScreenWidth() * 0.85);
        window.setAttributes(params);
    }

    public YesOrNoDialog Show() {
        super.show();
        return this;
    }
}
