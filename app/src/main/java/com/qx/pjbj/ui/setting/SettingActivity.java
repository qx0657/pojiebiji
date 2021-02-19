package com.qx.pjbj.ui.setting;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.qx.pjbj.R;
import com.qx.pjbj.base.BaseActivity;
import com.qx.pjbj.base.BasePresenter;
import com.qx.pjbj.utils.MySpUtils;

/**
 * Create by QianXiao
 * On 2020/7/29
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener{
    private LinearLayout ll_setting;
    private Switch switch_autoopenselectapp_setting,switch_autotipwelcome_setting,switch_backgroundrun_setting,switch_nosigntip_setting;
    private RelativeLayout rv_backgroundrun_setting;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_setting;
    }

    @Override
    protected int getToolBarID() {
        return R.id.toolbar;
    }

    @Override
    protected BasePresenter<?, ?> initPresenter() {
        return null;
    }

    @Override
    protected void initView() {
        ll_setting = (LinearLayout) f(R.id.ll_setting);
        switch_autoopenselectapp_setting = (Switch) f(R.id.switch_autoopenselectapp_setting);
        switch_autotipwelcome_setting = (Switch) f(R.id.switch_autotipwelcome_setting);
        switch_backgroundrun_setting = (Switch) f(R.id.switch_backgroundrun_setting);
        rv_backgroundrun_setting = (RelativeLayout) f(R.id.rv_backgroundrun_setting);
        switch_nosigntip_setting = (Switch) f(R.id.switch_nosigntip_setting);
    }

    @Override
    protected void initListener() {
        for (int i = 0; i < ll_setting.getChildCount(); i++) {
            ll_setting.getChildAt(i).setOnClickListener(this);
        }

        switch_autoopenselectapp_setting.setOnCheckedChangeListener((buttonView, isChecked) -> MySpUtils.save("switch_autoopenselectapp_setting",isChecked));
        switch_autotipwelcome_setting.setOnCheckedChangeListener((buttonView, isChecked) -> MySpUtils.save("switch_autotipwelcome_setting",isChecked));
        switch_backgroundrun_setting.setOnCheckedChangeListener((buttonView, isChecked) -> MySpUtils.save("switch_backgroundrun_setting",isChecked));
        switch_nosigntip_setting.setOnCheckedChangeListener((compoundButton, b) -> MySpUtils.save("switch_nosigntip_setting",b));
    }

    @Override
    protected void initData() {
        showBackButton();
        setTitle("设置");
        switch_autoopenselectapp_setting.setChecked(MySpUtils.getBoolean("switch_autoopenselectapp_setting"));
        if(!MySpUtils.contain("switch_autotipwelcome_setting")){
            MySpUtils.save("switch_autotipwelcome_setting",true);
            switch_autotipwelcome_setting.setChecked(true);
        }else{
            switch_autotipwelcome_setting.setChecked(MySpUtils.getBoolean("switch_autotipwelcome_setting"));
        }
        switch_backgroundrun_setting.setChecked(MySpUtils.getBoolean("switch_backgroundrun_setting"));
        if(!MySpUtils.contain("switch_nosigntip_setting")){
            switch_nosigntip_setting.setChecked(true);
        }else{
            switch_nosigntip_setting.setChecked(MySpUtils.getBoolean("switch_nosigntip_setting"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rv_backgroundrun_setting:
                switch_backgroundrun_setting.toggle();
                break;
            default:
                break;
        }
    }
}
