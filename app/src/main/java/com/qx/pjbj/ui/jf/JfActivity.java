package com.qx.pjbj.ui.jf;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.qx.pjbj.MyApplication;
import com.qx.pjbj.R;
import com.qx.pjbj.base.BaseActivity;
import com.qx.pjbj.base.BasePresenter;
import com.qx.pjbj.data.QQLoginConfig;
import com.qx.pjbj.utils.ClipboardUtils;
import com.qx.pjbj.utils.HttpConnectionUtil;
import com.qx.pjbj.utils.ImeiUtils;
import com.qx.pjbj.utils.MySpUtils;
import com.qx.pjbj.view.YesOrNoDialog;
import com.qx.pjbj.view.loading.MyLoadingDialog;
import com.tencent.tauth.Tencent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Create by QianXiao
 * On 2020/8/1
 */
public class JfActivity extends BaseActivity implements IJfView{
    private TextView tv_myjf_jfa,tv_vip_jf;
    private LinearLayout ll_exchangevip_jfa,
            ll_exchangecjq_jfa;

    private int user_jf = 0;
    private boolean hassignin = false;
    private int vip_jf = 0;
    private Tencent tencent;
    private MyLoadingDialog loadingDialog;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_jf;
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
        tv_myjf_jfa = (TextView) f(R.id.tv_myjf_jfa);
        tv_vip_jf = (TextView) f(R.id.tv_vip_jf);
        ll_exchangevip_jfa = (LinearLayout) f(R.id.ll_exchangevip_jfa);
        ll_exchangecjq_jfa = (LinearLayout) f(R.id.ll_exchangecjq_jfa);
    }

    @Override
    protected void initListener() {
        ll_exchangevip_jfa.setOnClickListener(v -> {
            if(MyApplication.myUserInfo!=null&&MyApplication.myUserInfo.isVip()){
                Toast("您已是VIP用户");
            }else if(user_jf<vip_jf){
                Toast("积分不足，赶快去发布笔记转积分吧");
            }else{
                ExchangeVip();
            }
        });
        ll_exchangecjq_jfa.setOnClickListener(v -> {
            if(user_jf<25){
                Toast("积分不足，赶快去发布笔记转积分吧");
            }else{
                ExchangeCjq();
            }
        });
    }

    @Override
    protected void initData() {
        showBackButton();
        setTitle("积分中心");
        if(MyApplication.myUserInfo!=null&&MyApplication.myUserInfo.isVip()){
            ll_exchangevip_jfa.setVisibility(View.GONE);
        }
        RefreshJF();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 0:
                RefreshJF();
                break;
            case 1:
                if(hassignin){
                    Toast("您今日已签到，明日再来签到吧");
                }else{
                    Signin();
                }
                break;
            case 2:
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://pjbj.qianxiao.fun/cj");
                intent.setData(content_url);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,"刷新");
        menu.add(0,1,0,"签到得积分");
        menu.add(0,2,0,"幸运抽奖");
        return true;
    }

    @Override
    public void RefreshJF() {
        if(tencent==null){
            tencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
        }
        JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null|| TextUtils.isEmpty(token)){
            Toast("未登录");
        }else{
            final String imei = ImeiUtils.getImei(context);
            if(TextUtils.isEmpty(imei)){
                Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                return;
            }
            openLoadingDialog("获取积分中");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String access_token = session.getString("access_token");
                        final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                        final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                "imei=%s&access_token=%s&token=%s&timestamp=%spjbj",
                                imei,access_token,token,timestamp));
                        Map<String, String> parms = new HashMap<>();
                        parms.put("imei", imei);
                        parms.put("access_token", access_token);
                        parms.put("token", token);
                        parms.put("timestamp", timestamp);
                        parms.put("parsign", parsign);
                        String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/getJF.php",parms);
                        LogUtils.i(res);
                        JSONObject jsonObject = new JSONObject(res);
                        final String msg = jsonObject.getJSONObject("data").getString("msg");
                        user_jf = jsonObject.getJSONObject("data").optInt("jf",0);
                        hassignin = jsonObject.getJSONObject("data").optInt("jq",0)==1;
                        vip_jf = jsonObject.getJSONObject("data").optInt("vipjf",100);
                        final int code = jsonObject.getInt("code");
                        ThreadUtils.runOnUiThread(() -> {
                            closeLoadingDialog();
                            if(code==1){
                                tv_myjf_jfa.setText("我的积分："+user_jf);
                                tv_vip_jf.setText(vip_jf+"积分");
                                if(!hassignin){
                                    Toast("今日未签到，立即去签到获取积分吧");
                                }
                            }else{
                                tv_myjf_jfa.setText("获取积分失败");
                                Toast(msg);
                            }
                        });

                    } catch (JSONException e) {
                        LogUtils.e(e.toString());
                        ThreadUtils.runOnUiThread(() -> closeLoadingDialog());
                    }

                }
            }).start();
        }
    }

    @Override
    public void ExchangeVip() {
        if(tencent==null){
            tencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
        }
        JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null|| TextUtils.isEmpty(token)){
            Toast("未登录");
        }else{
            final String imei = ImeiUtils.getImei(context);
            if(TextUtils.isEmpty(imei)){
                Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                return;
            }
            openLoadingDialog("兑换中");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String access_token = session.getString("access_token");
                        final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                        final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                "imei=%s&access_token=%s&token=%s&timestamp=%spjbj",
                                imei,access_token,token,timestamp));
                        Map<String, String> parms = new HashMap<>();
                        parms.put("imei", imei);
                        parms.put("access_token", access_token);
                        parms.put("token", token);
                        parms.put("timestamp", timestamp);
                        parms.put("parsign", parsign);
                        String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/exchangeVip.php",parms);
                        LogUtils.i(res);
                        JSONObject jsonObject = new JSONObject(res);
                        final String msg = jsonObject.getJSONObject("data").getString("msg");
                        final int code = jsonObject.getInt("code");
                        final String vipkttime = jsonObject.getJSONObject("data").optString("vipkttime","");
                        ThreadUtils.runOnUiThread(() -> {
                            closeLoadingDialog();
                            if(code==1){
                                //兑换VIP成功
                                Toast(msg);
                                user_jf-=100;
                                tv_myjf_jfa.setText("我的积分："+(user_jf));
                                ll_exchangevip_jfa.setVisibility(View.GONE);
                                MyApplication.myUserInfo.setVip(true);
                                MyApplication.myUserInfo.setVipkttime(vipkttime);
                                setResult(RESULT_OK);
                            }else{
                                Toast(msg);
                            }
                        });

                    } catch (JSONException e) {
                        LogUtils.e(e.toString());
                        ThreadUtils.runOnUiThread(() -> closeLoadingDialog());
                    }

                }
            }).start();
        }
    }

    @Override
    public void ExchangeCjq() {
        if(tencent==null){
            tencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
        }
        JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null|| TextUtils.isEmpty(token)){
            Toast("未登录");
        }else{
            final String imei = ImeiUtils.getImei(context);
            if(TextUtils.isEmpty(imei)){
                Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                return;
            }
            openLoadingDialog("兑换中");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String access_token = session.getString("access_token");
                        final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                        final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                "imei=%s&access_token=%s&token=%s&timestamp=%spjbj",
                                imei,access_token,token,timestamp));
                        Map<String, String> parms = new HashMap<>();
                        parms.put("imei", imei);
                        parms.put("access_token", access_token);
                        parms.put("token", token);
                        parms.put("timestamp", timestamp);
                        parms.put("parsign", parsign);
                        String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/exchangeCjq.php",parms);
                        LogUtils.i(res);
                        JSONObject jsonObject = new JSONObject(res);
                        final String msg = jsonObject.getJSONObject("data").getString("msg");
                        final int code = jsonObject.getInt("code");
                        final String cjq = jsonObject.getJSONObject("data").optString("cjq","");
                        ThreadUtils.runOnUiThread(() -> {
                            closeLoadingDialog();
                            if(code==1){
                                //兑换抽奖券成功
                                Toast(msg);
                                user_jf-=25;
                                tv_myjf_jfa.setText("我的积分："+(user_jf));
                                YesOrNoDialog yesOrNoDialog = new YesOrNoDialog(context,new YesOrNoDialog.YesOrNoDialogInterface() {
                                    @Override
                                    public void yesOnClick(YesOrNoDialog d) {
                                        ClipboardUtils.Copy2Clipboard(cjq);
                                        Toast("抽奖券已复制至剪贴板");
                                        d.dismiss();
                                    }

                                    @Override
                                    public void noOnClick(YesOrNoDialog d) {

                                    }
                                });
                                yesOrNoDialog.setCancelable(false);
                                yesOrNoDialog.Show().setYesText("复制抽奖券至剪贴板")
                                        .setOnlyYesBtn()
                                        .setContent("抽奖券："+cjq+"\n请您牢记抽奖券码，抽奖地址：http://pjbj.qianxiao.fun/cj");
                            }else{
                                Toast(msg);
                            }
                        });

                    } catch (JSONException e) {
                        LogUtils.e(e.toString());
                        ThreadUtils.runOnUiThread(() -> closeLoadingDialog());
                    }

                }
            }).start();
        }
    }

    @Override
    public void Signin() {
        if(tencent==null){
            tencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
        }
        JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null|| TextUtils.isEmpty(token)){
            Toast("未登录");
        }else{
            final String imei = ImeiUtils.getImei(context);
            if(TextUtils.isEmpty(imei)){
                Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                return;
            }
            openLoadingDialog("签到中");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String access_token = session.getString("access_token");
                        final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                        final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                "imei=%s&access_token=%s&token=%s&timestamp=%spjbj",
                                imei,access_token,token,timestamp));
                        Map<String, String> parms = new HashMap<>();
                        parms.put("imei", imei);
                        parms.put("access_token", access_token);
                        parms.put("token", token);
                        parms.put("timestamp", timestamp);
                        parms.put("parsign", parsign);
                        String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/Signin.php",parms);
                        LogUtils.i(res);
                        JSONObject jsonObject = new JSONObject(res);
                        final String msg = jsonObject.getJSONObject("data").getString("msg");
                        final int code = jsonObject.getInt("code");
                        final int addjf = jsonObject.getJSONObject("data").optInt("addjf",0);
                        ThreadUtils.runOnUiThread(() -> {
                            closeLoadingDialog();
                            if(code==1){
                                //签到成功
                                hassignin = true;
                                Toast(msg);
                                user_jf+=addjf;
                                tv_myjf_jfa.setText("我的积分："+(user_jf));
                            }else{
                                Toast(msg);
                            }
                        });

                    } catch (JSONException e) {
                        LogUtils.e(e.toString());
                        ThreadUtils.runOnUiThread(() -> closeLoadingDialog());
                    }

                }
            }).start();
        }
    }

    @Override
    public void openLoadingDialog(String msg) {
        if(loadingDialog == null){
            loadingDialog = new MyLoadingDialog(context);
        }
        if(!loadingDialog.isShowing()){
            loadingDialog.setMessage(msg);
            loadingDialog.show();
        }
    }

    @Override
    public void closeLoadingDialog() {
        if(loadingDialog!=null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }
}
