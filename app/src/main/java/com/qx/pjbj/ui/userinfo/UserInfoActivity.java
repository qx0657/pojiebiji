package com.qx.pjbj.ui.userinfo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.qx.pjbj.R;
import com.qx.pjbj.base.BaseActivity;
import com.qx.pjbj.base.BasePresenter;
import com.qx.pjbj.data.QQLoginConfig;
import com.qx.pjbj.ui.message.MessageActivity;
import com.qx.pjbj.ui.userinfo.view.IUserInfoView;
import com.qx.pjbj.utils.ClipboardUtils;
import com.qx.pjbj.utils.HttpConnectionUtil;
import com.qx.pjbj.utils.ImeiUtils;
import com.qx.pjbj.utils.LoadNetPicUtil;
import com.qx.pjbj.utils.MySpUtils;
import com.qx.pjbj.view.OvalImageView;
import com.qx.pjbj.view.loading.MyLoadingDialog;
import com.tencent.tauth.Tencent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.qx.pjbj.MyApplication.myUserInfo;

/**
 * Create by QianXiao
 * On 2020/7/24
 */
public class UserInfoActivity extends BaseActivity implements View.OnClickListener, IUserInfoView {
    private LinearLayout ll_userinfo;
    private OvalImageView iv_user_head_userinfo;
    private TextView tv_user_uid_userinfo,tv_user_nick_userinfo,tv_dhvip,tv_myvipinfo;

    private MyLoadingDialog loadingDialog;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_userinfo;
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
        ll_userinfo = (LinearLayout) f(R.id.ll_userinfo);
        iv_user_head_userinfo = (OvalImageView) f(R.id.iv_user_head_userinfo);
        tv_user_uid_userinfo = (TextView) f(R.id.tv_user_uid_userinfo);
        tv_user_nick_userinfo = (TextView) f(R.id.tv_user_nick_userinfo);
        tv_dhvip = (TextView) f(R.id.tv_dhvip);
        tv_myvipinfo = (TextView) f(R.id.tv_myvipinfo);
    }

    @Override
    protected void initListener() {
        for (int i = 0; i < ll_userinfo.getChildCount(); i++) {
            ll_userinfo.getChildAt(i).setOnClickListener(this);
        }
    }

    @Override
    protected void initData() {
        setTitle("个人中心");
        showBackButton();
        if(myUserInfo!=null){
            LoadNetPicUtil.load(iv_user_head_userinfo,myUserInfo.getHead_url());
            tv_user_uid_userinfo.setText(myUserInfo.getUid());
            tv_user_nick_userinfo.setText(myUserInfo.getNick());
            if(myUserInfo.isVip()){
                tv_dhvip.setText("我的VIP");
                tv_myvipinfo.setText("已开通永久VIP（开通时间："+myUserInfo.getVipkttime()+"）");
                tv_myvipinfo.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_userinfo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_messge_userinfo:
                startActivity(new Intent(context, MessageActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_uid_userinfo:
                ClipboardUtils.Copy2Clipboard(myUserInfo.getUid());
                Toast("UID已复制至剪贴板");
                break;
            case R.id.ll_nick_userinfo:
                //修改昵称
                EditText textInputEditText = new EditText(context);
                textInputEditText.setText(myUserInfo.getNick());
                textInputEditText.setHint("请输入昵称");
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0,ConvertUtils.dp2px(20),0,0);
                linearLayout.setLayoutParams(lp);
                linearLayout.setPadding(ConvertUtils.dp2px(10),0,ConvertUtils.dp2px(10),0);
                linearLayout.addView(textInputEditText);
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("修改昵称")
                        .setView(linearLayout)
                        .setPositiveButton("确定", null)
                        .setNegativeButton("取消", null)
                        .setCancelable(false);
                AlertDialog dialog = builder.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String value = textInputEditText.getText().toString();
                        if(TextUtils.isEmpty(value.trim())){
                            Toast("请输入昵称");
                            textInputEditText.requestFocus();
                        }else if(value.length()>32){
                            Toast("昵称最大32字符");
                        }else if(value.trim().equals(myUserInfo.getNick())){
                            dialog.dismiss();
                        }else{
                            String token = MySpUtils.getString("token");
                            if(!TextUtils.isEmpty(token)){
                                //网络请求更改昵称
                                final String newnick = value.trim();
                                try {
                                    Tencent tencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
                                    JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
                                    if(session!=null){
                                        final String imei = ImeiUtils.getImei(context);
                                        if(TextUtils.isEmpty(imei)){
                                            Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                                            return;
                                        }
                                        final String access_token = session.getString("access_token");
                                        final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                                        final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                                "imei=%s&newnick=%s&access_token=%s&token=%s&timestamp=%spjbj",
                                                imei,newnick,access_token,token,timestamp));
                                        openLoadingDialog("正在修改");
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Map<String, String> parms = new HashMap<>();
                                                parms.put("imei", imei);
                                                parms.put("newnick", newnick);
                                                parms.put("access_token", access_token);
                                                parms.put("token", token);
                                                parms.put("timestamp", timestamp);
                                                parms.put("parsign", parsign);
                                                String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/renick.php",parms);
                                                LogUtils.i(res);
                                                try {
                                                    final JSONObject registerobj = new JSONObject(res);
                                                    final int code = registerobj.getInt("code");
                                                    final String msg = registerobj.getString("msg");
                                                    ThreadUtils.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if(code == 1){
                                                                myUserInfo.setNick(newnick);
                                                                tv_user_nick_userinfo.setText(newnick);
                                                                MySpUtils.SaveObjectData("myUserInfo",myUserInfo);
                                                                dialog.dismiss();
                                                                Intent intent = new Intent();
                                                                intent.putExtra("newnick",newnick);
                                                                setResult(RESULT_OK,intent);
                                                            }
                                                            Toast(msg);
                                                        }
                                                    });
                                                } catch (final JSONException e) {
                                                    LogUtils.e(e.toString());
                                                }
                                                closeLoadingDialog();
                                            }
                                        }).start();
                                    }
                                } catch (JSONException e) {
                                    LogUtils.e(e.toString());
                                }
                            }
                        }
                    }
                });
                textInputEditText.requestFocus();
                //延迟打开软键盘
                new Timer().schedule(new TimerTask() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        assert imm != null;
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    }

                }, 200);//n秒后弹出
                break;
            case R.id.ll_exchangevip_userinfo:
                if(myUserInfo.isVip()){
                    Toast("您已是VIP用户");
                }else{
                    EditText kami_editText = new EditText(context);
                    kami_editText.setHint("请输入卡密");
                    LinearLayout l1 = new LinearLayout(context);
                    l1.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp1.setMargins(0,ConvertUtils.dp2px(20),0,0);
                    l1.setLayoutParams(lp1);
                    l1.setPadding(ConvertUtils.dp2px(10),0,ConvertUtils.dp2px(10),0);
                    l1.addView(kami_editText);
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context)
                            .setTitle("兑换永久VIP")
                            .setView(l1)
                            .setNeutralButton("获取卡密", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /*Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    Uri content_url = Uri.parse("https://www.csfaka.com/details/934DDF57");
                                    intent.setData(content_url);
                                    startActivity(intent);*/
                                    ToastUtils.showLong("请联系作者");
                                }
                            })
                            .setPositiveButton("确定", null)
                            .setNegativeButton("取消", null)
                            .setCancelable(false);
                    AlertDialog dialog1 = builder1.show();
                    dialog1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String kami = kami_editText.getText().toString().trim();
                            if(TextUtils.isEmpty(kami)){
                                Toast("请输入卡密");
                                kami_editText.requestFocus();
                            }else if(kami.length()!=32){
                                Toast("卡密长度有误");
                                kami_editText.requestFocus();
                            }else{
                                try {
                                    Tencent tencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
                                    JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
                                    if(session!=null){
                                        final String token = MySpUtils.getString("token");
                                        if(TextUtils.isEmpty(token)){
                                            return;
                                        }
                                        final String imei = ImeiUtils.getImei(context);
                                        if(TextUtils.isEmpty(imei)){
                                            Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                                            return;
                                        }
                                        final String access_token = session.getString("access_token");
                                        final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                                        final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                                "imei=%s&kami=%s&access_token=%s&token=%s&timestamp=%spjbj",
                                                imei,kami,access_token,token,timestamp));
                                        openLoadingDialog("正在验证");
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Map<String, String> parms = new HashMap<>();
                                                parms.put("imei", imei);
                                                parms.put("kami", kami);
                                                parms.put("access_token", access_token);
                                                parms.put("token", token);
                                                parms.put("timestamp", timestamp);
                                                parms.put("parsign", parsign);
                                                String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/vip.php",parms);
                                                LogUtils.i(res);
                                                try {
                                                    final JSONObject registerobj = new JSONObject(res);
                                                    final int code = registerobj.getInt("code");
                                                    final String msg = registerobj.getString("msg");
                                                    final String vipkttime = registerobj.optString("vipkttime","");
                                                    ThreadUtils.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if(code == 1){
                                                                tv_dhvip.setText("我的VIP");
                                                                tv_myvipinfo.setText("已开通永久VIP(开通时间："+vipkttime+")");
                                                                tv_myvipinfo.setVisibility(View.VISIBLE);
                                                                myUserInfo.setVip(true);
                                                                myUserInfo.setVipkttime(vipkttime);
                                                                MySpUtils.SaveObjectData("myUserInfo",myUserInfo);
                                                                dialog1.dismiss();
                                                                Intent intent = getIntent();
                                                                intent.putExtra("vip",true);
                                                                setResult(RESULT_OK,intent);
                                                            }
                                                            Toast(msg);
                                                        }
                                                    });
                                                } catch (final JSONException e) {
                                                    LogUtils.e(e.toString());
                                                }
                                                closeLoadingDialog();
                                            }
                                        }).start();
                                    }
                                } catch (JSONException e) {
                                    LogUtils.e(e.toString());
                                }
                            }
                        }
                    });
                    kami_editText.requestFocus();
                    //延迟打开软键盘
                    new Timer().schedule(new TimerTask() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void run() {
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            assert imm != null;
                            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        }

                    }, 200);//n秒后弹出
                }
                break;
            default:
                break;
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
