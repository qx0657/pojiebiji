package com.qx.pjbj.ui.main.model;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.qx.pjbj.data.MyUserInfo;
import com.qx.pjbj.data.PjNote;
import com.qx.pjbj.data.QQLoginConfig;
import com.qx.pjbj.ui.main.presenter.MainPresenter;
import com.qx.pjbj.utils.HttpConnectionUtil;
import com.qx.pjbj.utils.ImeiUtils;
import com.qx.pjbj.utils.MySpUtils;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.qx.pjbj.MyApplication.myUserInfo;

/**
 * Create by QianXiao
 * On 2020/4/15
 */
public class MainModel implements IMainModel {
    private Context context;
    private MainPresenter presenter;
    //回调 IUiListener
    private IUiListener loginIUiListener;

    public MainModel(final Context context, MainPresenter presenter) {
        this.context = context;
        this.presenter = presenter;
        //授权登录回调
        loginIUiListener = new IUiListener() {
            @Override
            public void onComplete(Object o) {
                //授权成功
                JSONObject jsonObject = (JSONObject) o;
                //access_token、openid
                LogUtils.i(jsonObject.toString());
                try {
                    String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
                    String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
                    String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
                    //保存Session会话信息
                    MainModel.this.presenter.mTencent.saveSession(jsonObject);
                    if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                            && !TextUtils.isEmpty(openId)) {
                        //登录成功设置openid和token
                        MainModel.this.presenter.mTencent.setAccessToken(token, expires);
                        MainModel.this.presenter.mTencent.setOpenId(openId);
                    }
                    //获取IMEI准备连同登录获取的access_token转给服务端进行登录处理
                    final String imei = ImeiUtils.getImei(context);
                    if(TextUtils.isEmpty(imei)){
                        MainModel.this.presenter.mView.Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                        return;
                    }
                    MainModel.this.presenter.mView.openLoadingDialog("正在登录");
                    final String access_token = MainModel.this.presenter.mTencent.getAccessToken();
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
                            String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/login.php",parms);
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
                                ThreadUtils.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(code >= 0){
                                            MySpUtils.save("token",token);
                                            MainModel.this.presenter.mView.Toast(nick+"登录成功");
                                            if(myUserInfo == null){
                                                myUserInfo = new MyUserInfo("");
                                            }
                                            myUserInfo.setUid(unionid);
                                            myUserInfo.setNick(nick);
                                            myUserInfo.setHead_url(head_url);
                                            if(is_manager){
                                                myUserInfo.setManager(true);
                                            }
                                            MainModel.this.presenter.mView.loginSuccess(nick,head_url);
                                            if(is_vip){
                                                myUserInfo.setVip(true);
                                                myUserInfo.setVipkttime(vipkttime);
                                                MainModel.this.presenter.mView.ktVipSuccess();
                                            }
                                            MySpUtils.SaveObjectData("myUserInfo",myUserInfo);
                                        }else{
                                            if(code == -2){
                                                MainModel.this.presenter.mView.Toast("请检查手机时间是否正确后重试");
                                            }else{
                                                MainModel.this.presenter.mView.Toast(msg);
                                            }
                                        }
                                    }
                                });
                            } catch (final JSONException e) {
                                LogUtils.e(e.toString());
                                MainModel.this.presenter.mView.Toast(e.toString());
                            }
                            MainModel.this.presenter.mView.closeLoadingDialog();
                        }
                    }).start();

                } catch (JSONException e) {
                    LogUtils.e(e.toString());
                }
            }

            @Override
            public void onError(UiError uiError) {
                MainModel.this.presenter.mView.Toast("授权失败");
            }

            @Override
            public void onCancel() {
                MainModel.this.presenter.mView.Toast("授权取消");
            }
        };
    }

    @Override
    public void OnRefreshData(final RefreshCallback callback) {
        JSONObject session = presenter.mTencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String,String> map = new HashMap<>();
                    if(session!=null&&session.has("access_token")){
                        final String access_token = session.getString("access_token");
                        map.put("access_token",access_token);
                    }
                    if(!TextUtils.isEmpty(token)){
                        map.put("token",token);
                    }
                    final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                    final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                            "start=0&count=20&timestamp=%spjbj",
                            timestamp));
                    map.put("start","0");
                    map.put("count","20");
                    map.put("timestamp",timestamp);
                    map.put("parsign",parsign);
                    String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/getData.php",map);
                    //LogUtils.i(res);
                    JSONObject resobj = new JSONObject(res);
                    JSONObject dataobj = resobj.getJSONObject("data");
                    if(resobj.getInt("code")==1){
                        JSONArray pjdata = dataobj.getJSONArray("data");
                        //LogUtils.i(pjdata.toString());
                        List<PjNote> list = new ArrayList<>();
                        for (int i = 0; i < pjdata.length(); i++){
                            PjNote pjNote = new PjNote();
                            JSONObject jsonObject = pjdata.getJSONObject(i);
                            pjNote.setId(Long.parseLong(jsonObject.getString("id")));
                            pjNote.setGamename(jsonObject.getString("gamename"));
                            pjNote.setPackagename(jsonObject.getString("packagename"));
                            pjNote.setType(jsonObject.getString("type"));
                            pjNote.setAuthor(jsonObject.getString("author"));

                            pjNote.setUpdatetime(TimeUtils.string2Date(jsonObject.getString("updatetime")));
                            pjNote.setGood(Integer.parseInt(jsonObject.getString("good")));
                            pjNote.setLook(Integer.parseInt(jsonObject.getString("look")));
                            list.add(pjNote);
                        }
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.RefreshSuccess(1,list);
                            }
                        });
                    }else{
                        final String msg = dataobj.getString("msg");
                        ThreadUtils.runOnUiThread(() -> callback.RefreshError(msg));
                    }

                } catch (JSONException e) {
                    LogUtils.e(e.toString());
                    ThreadUtils.runOnUiThread(() -> callback.RefreshError(e.toString()));
                }
            }
        }).start();

    }

    @Override
    public void QQLogin(Tencent tencent) {
        tencent.login((Activity) context,"all", loginIUiListener);
    }

    @Override
    public IUiListener getIUiListener() {
        return loginIUiListener;
    }

    @Override
    public void LoadMore(int start, int count, LoadMoreCallback callback) {
        JSONObject session = presenter.mTencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null||TextUtils.isEmpty(token)){
            callback.onLoadMoreDataFail(-99,"请登录后查看更多");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String access_token = session.getString("access_token");
                    final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                    final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                            "start=%d&count=%d&timestamp=%spjbj",
                            start,count,timestamp));
                    Map<String,String> map = new HashMap<>();
                    map.put("start",String.valueOf(start));
                    map.put("count",String.valueOf(count));
                    map.put("timestamp",timestamp);
                    map.put("access_token",access_token);
                    map.put("token",token);
                    map.put("parsign",parsign);
                    String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/getData.php",map);
                    //LogUtils.i(res);
                    JSONObject resobj = new JSONObject(res);
                    JSONObject dataobj = resobj.getJSONObject("data");
                    if(resobj.getInt("code")==1){
                        JSONArray pjdata = dataobj.getJSONArray("data");
                        //LogUtils.i(pjdata.toString());
                        List<PjNote> list = new ArrayList<>();
                        for (int i = 0; i < pjdata.length(); i++){
                            PjNote pjNote = new PjNote();
                            JSONObject jsonObject = pjdata.getJSONObject(i);
                            pjNote.setId(Long.parseLong(jsonObject.getString("id")));
                            pjNote.setGamename(jsonObject.getString("gamename"));
                            pjNote.setPackagename(jsonObject.getString("packagename"));
                            pjNote.setType(jsonObject.getString("type"));
                            pjNote.setAuthor(jsonObject.getString("author"));

                            pjNote.setUpdatetime(TimeUtils.string2Date(jsonObject.getString("updatetime")));
                            pjNote.setGood(Integer.parseInt(jsonObject.getString("good")));
                            pjNote.setLook(Integer.parseInt(jsonObject.getString("look")));
                            list.add(pjNote);
                        }
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(list.size()==0){
                                    callback.onLoadMoreDataFail(0,"没有更多了");
                                }else{
                                    callback.onLoadMoreDataSuccess(list);
                                }
                            }
                        });
                    }else{
                        final String msg = dataobj.getString("msg");
                        ThreadUtils.runOnUiThread(() -> callback.onLoadMoreDataFail(-1,msg));
                    }

                } catch (JSONException e) {
                    LogUtils.e(e.toString());
                    ThreadUtils.runOnUiThread(() -> callback.onLoadMoreDataFail(-1,e.toString()));
                }
            }
        }).start();
    }

    @Override
    public void SearchGame(String key, SearchGameCallback callback) {
        JSONObject session = presenter.mTencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null||TextUtils.isEmpty(token)){
            callback.onSearchGameFail("未登录");
            return;
        }
        final String imei = ImeiUtils.getImei(context);
        if(TextUtils.isEmpty(imei)){
            MainModel.this.presenter.mView.Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String access_token = session.getString("access_token");
                    final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                    final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                            "imei=%s&access_token=%s&key=%s&token=%s&timestamp=%spjbj",
                            imei,access_token,key,token,timestamp));
                    Map<String,String> map = new HashMap<>();
                    map.put("imei",imei);
                    map.put("access_token",access_token);
                    map.put("key",key);
                    map.put("token",token);
                    map.put("timestamp",timestamp);
                    map.put("parsign",parsign);
                    String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/search.php",map);
                    LogUtils.i(res);
                    JSONObject resobj = new JSONObject(res);
                    JSONObject dataobj = resobj.getJSONObject("data");
                    if(resobj.getInt("code")==1){
                        JSONArray pjdata = dataobj.getJSONArray("data");
                        //LogUtils.i(pjdata.toString());
                        List<PjNote> list = new ArrayList<>();
                        for (int i = 0; i < pjdata.length(); i++){
                            PjNote pjNote = new PjNote();
                            JSONObject jsonObject = pjdata.getJSONObject(i);
                            pjNote.setId(Long.parseLong(jsonObject.getString("id")));
                            pjNote.setGamename(jsonObject.getString("gamename"));
                            pjNote.setPackagename(jsonObject.getString("packagename"));
                            pjNote.setType(jsonObject.getString("type"));
                            pjNote.setAuthor(jsonObject.getString("author"));

                            pjNote.setUpdatetime(TimeUtils.string2Date(jsonObject.getString("updatetime")));
                            pjNote.setGood(Integer.parseInt(jsonObject.getString("good")));
                            pjNote.setLook(Integer.parseInt(jsonObject.getString("look")));
                            list.add(pjNote);
                        }
                        if(list.size() == 0){
                            ThreadUtils.runOnUiThread(() -> callback.onSearchGameFail("啊嗷，没有搜索到任何结果"));
                        }else{
                            ThreadUtils.runOnUiThread(() -> callback.onSearchGameSuccess(list));
                        }

                    }else{
                        final String msg = dataobj.getString("msg");
                        ThreadUtils.runOnUiThread(() -> callback.onSearchGameFail(msg));
                    }

                } catch (JSONException e) {
                    LogUtils.e(e.toString());
                    ThreadUtils.runOnUiThread(() -> callback.onSearchGameFail(e.toString()));
                }
            }
        }).start();
    }

    @Override
    public void LoadMyShare(RefreshCallback callback) {
        JSONObject session = presenter.mTencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null||TextUtils.isEmpty(token)){
            callback.RefreshError("未登录");
            return;
        }
        final String imei = ImeiUtils.getImei(context);
        if(TextUtils.isEmpty(imei)){
            MainModel.this.presenter.mView.Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String access_token = session.getString("access_token");
                    final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                    final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                            "imei=%s&access_token=%s&token=%s&timestamp=%spjbj",
                            imei,access_token,token,timestamp));
                    Map<String,String> map = new HashMap<>();
                    map.put("imei",imei);
                    map.put("access_token",access_token);
                    map.put("token",token);
                    map.put("timestamp",timestamp);
                    map.put("parsign",parsign);
                    String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/myShare.php",map);
                    LogUtils.i(res);
                    JSONObject resobj = new JSONObject(res);
                    JSONObject dataobj = resobj.getJSONObject("data");
                    if(resobj.getInt("code")==1){
                        JSONArray pjdata = dataobj.getJSONArray("data");
                        //LogUtils.i(pjdata.toString());
                        List<PjNote> list = new ArrayList<>();
                        for (int i = 0; i < pjdata.length(); i++){
                            PjNote pjNote = new PjNote();
                            JSONObject jsonObject = pjdata.getJSONObject(i);
                            pjNote.setId(Long.parseLong(jsonObject.getString("id")));
                            pjNote.setGamename(jsonObject.getString("gamename"));
                            pjNote.setPackagename(jsonObject.getString("packagename"));
                            pjNote.setType(jsonObject.getString("type"));
                            pjNote.setPass(jsonObject.getString("pass").equals("1"));
                            pjNote.setAuthor(jsonObject.getString("author"));
                            pjNote.setUpdatetime(TimeUtils.string2Date(jsonObject.getString("updatetime")));
                            pjNote.setGood(Integer.parseInt(jsonObject.getString("good")));
                            pjNote.setLook(Integer.parseInt(jsonObject.getString("look")));
                            list.add(pjNote);
                        }
                        if(list.size() == 0){
                            ThreadUtils.runOnUiThread(() -> callback.RefreshError("您还没有分享过任何笔记，赶快去分享一条吧"));
                        }else{
                            ThreadUtils.runOnUiThread(() -> callback.RefreshSuccess(2,list));
                        }

                    }else{
                        final String msg = dataobj.getString("msg");
                        ThreadUtils.runOnUiThread(() -> callback.RefreshError(msg));
                    }

                } catch (JSONException e) {
                    LogUtils.e(e.toString());
                    ThreadUtils.runOnUiThread(() -> callback.RefreshError(e.toString()));
                }
            }
        }).start();
    }

    @Override
    public void LoadMyLike(RefreshCallback callback) {
        JSONObject session = presenter.mTencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null||TextUtils.isEmpty(token)){
            callback.RefreshError("未登录");
            return;
        }
        final String imei = ImeiUtils.getImei(context);
        if(TextUtils.isEmpty(imei)){
            MainModel.this.presenter.mView.Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String access_token = session.getString("access_token");
                    final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                    final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                            "imei=%s&access_token=%s&token=%s&timestamp=%spjbj",
                            imei,access_token,token,timestamp));
                    Map<String,String> map = new HashMap<>();
                    map.put("imei",imei);
                    map.put("access_token",access_token);
                    map.put("token",token);
                    map.put("timestamp",timestamp);
                    map.put("parsign",parsign);
                    String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/myLike.php",map);
                    LogUtils.i(res);
                    JSONObject resobj = new JSONObject(res);
                    JSONObject dataobj = resobj.getJSONObject("data");
                    if(resobj.getInt("code")==1){
                        JSONArray pjdata = dataobj.getJSONArray("data");
                        //LogUtils.i(pjdata.toString());
                        List<PjNote> list = new ArrayList<>();
                        for (int i = 0; i < pjdata.length(); i++){
                            PjNote pjNote = new PjNote();
                            JSONObject jsonObject = pjdata.getJSONObject(i);
                            pjNote.setId(Long.parseLong(jsonObject.getString("id")));
                            pjNote.setGamename(jsonObject.getString("gamename"));
                            pjNote.setPackagename(jsonObject.getString("packagename"));
                            pjNote.setType(jsonObject.getString("type"));
                            pjNote.setAuthor(jsonObject.getString("author"));
                            pjNote.setUpdatetime(TimeUtils.string2Date(jsonObject.getString("updatetime")));
                            pjNote.setGood(Integer.parseInt(jsonObject.getString("good")));
                            pjNote.setLook(Integer.parseInt(jsonObject.getString("look")));
                            list.add(pjNote);
                        }
                        if(list.size() == 0){
                            ThreadUtils.runOnUiThread(() -> callback.RefreshError("您还没有收藏任何笔记"));
                        }else{
                            ThreadUtils.runOnUiThread(() -> callback.RefreshSuccess(3,list));
                        }
                    }else{
                        final String msg = dataobj.getString("msg");
                        ThreadUtils.runOnUiThread(() -> callback.RefreshError(msg));
                    }

                } catch (JSONException e) {
                    LogUtils.e(e.toString());
                    ThreadUtils.runOnUiThread(() -> callback.RefreshError(e.toString()));
                }
            }
        }).start();
    }

    @Override
    public void getNoReadMsgNum(GetNoReadMsgNumCallBack callBack) {
        JSONObject session = presenter.mTencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null||TextUtils.isEmpty(token)){
            //未登录
            return;
        }
        final String imei = ImeiUtils.getImei(context);
        if(TextUtils.isEmpty(imei)){
            //获取手机IMEI错误，请检查是否授予IMEI权限
            return;
        }
        try {
            final String access_token = session.getString("access_token");
            final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
            final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                    "imei=%s&access_token=%s&token=%s&timestamp=%spjbj",
                    imei,access_token,token,timestamp));
            Map<String,String> map = new HashMap<>();
            map.put("imei",imei);
            map.put("access_token",access_token);
            map.put("token",token);
            map.put("timestamp",timestamp);
            map.put("parsign",parsign);
            String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/notreadMessageNum.php",map);
            LogUtils.i(res);
            JSONObject resobj = new JSONObject(res);
            JSONObject dataobj = resobj.getJSONObject("data");
            if(resobj.getInt("code")==1){
                int noreadmsgnum = dataobj.getInt("data");
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.ongetNoReadMsgNumSuccess(noreadmsgnum);
                    }
                });
            }else{
                final String msg = dataobj.getString("msg");
                //
            }

        } catch (JSONException e) {
            LogUtils.e(e.toString());
            //
        }
    }
}
