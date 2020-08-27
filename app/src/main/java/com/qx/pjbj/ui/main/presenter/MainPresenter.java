package com.qx.pjbj.ui.main.presenter;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.qx.pjbj.base.BasePresenter;
import com.qx.pjbj.data.MyUserInfo;
import com.qx.pjbj.data.PjNote;
import com.qx.pjbj.data.QQLoginConfig;
import com.qx.pjbj.ui.main.model.IMainModel;
import com.qx.pjbj.ui.main.model.MainModel;
import com.qx.pjbj.ui.main.view.IMainView;
import com.qx.pjbj.utils.HttpConnectionUtil;
import com.qx.pjbj.utils.ImeiUtils;
import com.qx.pjbj.utils.MySpUtils;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import static com.qx.pjbj.MyApplication.myUserInfo;

/**
 * Create by QianXiao
 * On 2020/4/15
 */
public class MainPresenter
        extends BasePresenter<IMainView, IMainModel>
        implements IMainPresenter,
        IMainModel.RefreshCallback, IMainModel.LoadMoreCallback, IMainModel.SearchGameCallback{
    public Tencent mTencent;
    public UserInfo mUserInfo;

    public boolean more = true;
    public boolean isLoading = false;//用来控制进入RefreshDate()的次数

    public MainPresenter(Context context) {
        super(context);
    }

    @Override
    protected IMainModel initModel() {
        return new MainModel(context,this);
    }

    @Override
    public void OnRefresh() {
        mView.openLoadingDialog("正在刷新");
        //将实现的回调接口（IMainModel.RefreshCallback）实现并传入
        mModel.OnRefreshData(this);
    }

    @Override
    public void LoginInit() {
        //初始化腾讯互联对象
        mTencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
        //加载腾讯登录缓存
        JSONObject session = mTencent.loadSession(QQLoginConfig.APP_ID);
        if(session!=null){
            mTencent.initSessionCache(session);
            //myUserInfo = MySpUtils.getObjectData("myUserInfo");
            if(myUserInfo==null){
                myUserInfo = new MyUserInfo("");
            }
            if(mTencent.isSessionValid()){
                LogUtils.i("登录状态有效",session.toString());
                mTencent.initSessionCache(session);
                //注册&登录账号
                final String imei = ImeiUtils.getImei(context);
                if(TextUtils.isEmpty(imei)){
                    mView.Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                    return;
                }
                final String access_token = mTencent.getAccessToken();
                final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                        "imei=%s&access_token=%s&timestamp=%spjbj",
                        imei,access_token,timestamp));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, String> parms = new HashMap<>();
                        parms.put("imei", imei);
                        parms.put("access_token", access_token);
                        parms.put("timestamp", timestamp);
                        parms.put("parsign", parsign);
                        String res = HttpConnectionUtil.getHttp().postRequset("http://qianxiao.fun/app/pojiebiji/login.php",parms);
                        LogUtils.i(res);
                        try {
                            final JSONObject registerobj = new JSONObject(res);
                            final int code = registerobj.getInt("code");
                            final JSONObject dataobj = registerobj.getJSONObject("data");
                            final String msg = dataobj.getString("msg");
                            final String unionid = dataobj.optString("unionid","");
                            final String nick = dataobj.optString("nickname","");
                            final boolean is_vip = "1".equals(dataobj.optString("vip","0"));
                            final boolean is_manager = "1".equals(dataobj.optString("manager","0"));
                            final String vipkttime = dataobj.optString("vipkttime","");
                            final String head_url = dataobj.optString("head_url","");
                            final String token = dataobj.optString("token","");
                            boolean hassignin = dataobj.optInt("jq",0)==1;
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(code >= 0){
                                        MySpUtils.save("token",token);
                                        if(MySpUtils.getBoolean("switch_autotipwelcome_setting")){
                                            mView.Toast("欢迎你"+nick);
                                        }
                                        myUserInfo.setUid(unionid);
                                        myUserInfo.setNick(nick);


                                        if(!hassignin){
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                                    .setTitle("签到提示")
                                                    .setMessage(myUserInfo.getNick()+"你好，你今日还未签到，是否立即签到？")
                                                    .setPositiveButton("立即签到", (dialog, which) -> {
                                                        mView.openLoadingDialog("签到中");
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
                                                                    String res = HttpConnectionUtil.getHttp().postRequset("http://qianxiao.fun/app/pojiebiji/Signin.php",parms);
                                                                    LogUtils.i(res);
                                                                    JSONObject jsonObject = new JSONObject(res);
                                                                    final String msg = jsonObject.getJSONObject("data").getString("msg");
                                                                    final int code = jsonObject.getInt("code");
                                                                    //final int addjf = jsonObject.getJSONObject("data").optInt("addjf",0);
                                                                    ThreadUtils.runOnUiThread(() -> {
                                                                        mView.closeLoadingDialog();
                                                                        if(code==1){
                                                                            //签到成功
                                                                            mView.Toast(msg);
                                                                        }else{
                                                                            mView.Toast(msg);
                                                                        }
                                                                    });

                                                                } catch (JSONException e) {
                                                                    LogUtils.e(e.toString());
                                                                    ThreadUtils.runOnUiThread(() -> mView.closeLoadingDialog());
                                                                }

                                                            }
                                                        }).start();
                                                    })
                                                    .setNegativeButton("暂不签到", (d,w)->mView.Toast("您可以进入积分中心-右上角-签到得积分进行签到"))
                                                    .setCancelable(false);
                                            builder.show();
                                        }

                                        myUserInfo.setHead_url(head_url);
                                        if(is_manager){
                                            myUserInfo.setManager(true);
                                        }
                                        mView.loginSuccess(nick,head_url);
                                        if(is_vip){
                                            myUserInfo.setVip(true);
                                            myUserInfo.setVipkttime(vipkttime);
                                            mView.ktVipSuccess();
                                        }
                                        MySpUtils.SaveObjectData("myUserInfo",myUserInfo);
                                    }else{
                                        if(code == -2){
                                            mView.Toast("请检查手机时间是否正确后重试");
                                        }else if(code == -4) {
                                            mView.Toast("登录已过期，请重新登录");
                                            mView.loginOut();
                                        }else{
                                            mView.Toast(msg);
                                        }
                                    }
                                }
                            });
                        } catch (final JSONException e) {
                            LogUtils.e(e.toString());
                        }
                    }
                }).start();
            }else{
                LogUtils.i("登录已过期",session.toString());
                mView.Toast("登录已过期，请重新登录");
            }
        }else{
            LogUtils.i("session is null");
        }
    }

    @Override
    public void Login() {
        mModel.QQLogin(mTencent);
    }

    @Override
    public boolean isQQInstalled() {
        return mTencent.isQQInstalled(context);
    }

    @Override
    public IUiListener getLoginIUiListener() {
        return mModel.getIUiListener();
    }

    @Override
    public void LoadMoreData(int start, int num) {
        mModel.LoadMore(start,num,this);
    }

    @Override
    public void SearchNote(String s) {
        mView.openLoadingDialog("正在搜素");
        mModel.SearchGame(s,this);
    }

    @Override
    public void loadMyShare() {
        mView.openLoadingDialog("正在请求");
        mModel.LoadMyShare(this);
    }

    @Override
    public void loadMyLike() {
        mView.openLoadingDialog("正在请求");
        mModel.LoadMyLike(this);
    }

    @Override
    public void getNoReadMsgNum() {
        mModel.getNoReadMsgNum(noReadMsgNum -> mView.showNoReadMsgNum(noReadMsgNum));
    }

    @Override
    public void RefreshSuccess(int a,List<PjNote> data) {
        mView.closeLoadingDialog();
        mView.RefreshDate(data);
        if(a == 2){
            mView.setTile("您共分享"+data.size()+"条笔记");
        }else if(a == 3){
            mView.setTile("您共收藏"+data.size()+"条笔记");
        }
    }

    @Override
    public void RefreshError(String e) {
        mView.closeLoadingDialog();
        mView.Toast(e);
    }

    @Override
    public void onLoadMoreDataSuccess(List<PjNote> data) {
        mView.addData(data);
    }

    @Override
    public void onLoadMoreDataFail(int code,String e) {
        switch (code){
            case 0:
                more = false;
                mView.changeState(0);
                isLoading = false;
                mView.Toast("没有更多了");
                break;
            default:
                mView.changeState(0);
                mView.Toast(e);
                break;
        }
    }

    @Override
    public void onSearchGameSuccess(List<PjNote> data) {
        mView.closeLoadingDialog();
        mView.RefreshDate(data);
    }

    @Override
    public void onSearchGameFail(String e) {
        mView.closeLoadingDialog();
        mView.Toast(e);
    }
}
