package com.qx.pjbj.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.qx.pjbj.R;
import com.qx.pjbj.base.BaseActivity;
import com.qx.pjbj.base.BasePresenter;
import com.qx.pjbj.checkupdate.CheckUpdateManager;
import com.qx.pjbj.data.AppConfig;

/**
 * Create by QianXiao
 * On 2020/7/24
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout ll_about;
    private TextView tv_currentversion;
    private CheckUpdateManager checkUpdateManager;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_about;
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
        ll_about = (LinearLayout) f(R.id.ll_about);
        tv_currentversion = (TextView) f(R.id.tv_currentversion);
    }

    @Override
    protected void initListener() {
        for (int i = 0; i < ll_about.getChildCount(); i++) {
            ll_about.getChildAt(i).setOnClickListener(this);
        }
    }

    @Override
    protected void initData() {
        showBackButton();
        setTitle("关于");
        tv_currentversion.setText("当前版本：".concat(AppUtils.getAppVersionName()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_checkupdate:
                if(checkUpdateManager == null){
                    checkUpdateManager = new CheckUpdateManager(context);
                }
                checkUpdateManager.check(false);
                break;
            case R.id.ll_share_pjbj:
                String ShareStr = "破解笔记，一款旨在用户共享逆向破解经验的软件。用户可以登录进行破解游戏经验的添加，也可以进行查看和搜索共享空间的破解笔记，互相学习，提高效率。\n" +
                        "下载地址："+ AppConfig.APP_URL;
                Intent StringIntent = new Intent(Intent.ACTION_SEND);
                StringIntent.setType("text/plain");
                StringIntent.putExtra(Intent.EXTRA_TEXT, ShareStr);
                startActivity(Intent.createChooser(StringIntent, "分享"));
                break;
            case R.id.ll_joinqqgroup_pjbj:
                String qqgroupkey = "sDa31yxhvp4gaivPdO0tnd4bzqjbSF1Q";
                Intent intent = new Intent();
                intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + qqgroupkey));
                // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    // 未安装手Q或安装的版本不支持
                    Toast("未安装手Q或安装的版本不支持");
                }
                break;
            case R.id.ll_privacypolicy_pjbj:
                //服务条款隐私政策
                Intent intent2 = new Intent();
                intent2.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://pjbj.qianxiao.fun/termsandprivacy/");
                intent2.setData(content_url);
                startActivity(intent2);
                break;
            case R.id.ll_contactdeveloper:
                String url = "mqqwpa://im/chat?chat_type=wpa&uin=1540223730";
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                break;
            case R.id.ll_supportdeveloper:
                Intent intent3 = new Intent();
                intent3.setAction("android.intent.action.VIEW");
                Uri content_url3 = Uri.parse("https://ko-fi.com/qx0657");
                intent3.setData(content_url3);
                startActivity(intent3);
                break;
            default:
                break;
        }
    }
}
