package com.qx.pjbj.ui.message;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.qx.pjbj.R;
import com.qx.pjbj.base.BaseActivity;
import com.qx.pjbj.base.BasePresenter;
import com.qx.pjbj.data.PjNote;
import com.qx.pjbj.data.QQLoginConfig;
import com.qx.pjbj.ui.message.data.Message;
import com.qx.pjbj.ui.message.view.IMessageView;
import com.qx.pjbj.ui.message.view.MessageAdapter;
import com.qx.pjbj.utils.HttpConnectionUtil;
import com.qx.pjbj.utils.ImeiUtils;
import com.qx.pjbj.utils.MySpUtils;
import com.qx.pjbj.view.loading.MyLoadingDialog;
import com.tencent.tauth.Tencent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by QianXiao
 * On 2020/8/24
 */
public class MessageActivity extends BaseActivity implements IMessageView {
    private RecyclerView rv_messageactivity;
    private MessageAdapter adapter;

    private Tencent tencent;
    private MyLoadingDialog loadingDialog;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_message;
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
        rv_messageactivity = (RecyclerView) f(R.id.rv_messageactivity);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        showBackButton();
        setTitle("消息");
        rv_messageactivity.setLayoutManager(new LinearLayoutManager(context));
        onRefresh();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 0:
                onRefresh();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,"刷新");
        return true;
    }

    @Override
    public void onRefresh() {
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
            openLoadingDialog("正在加载");
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
                        String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/myMessage.php",parms);
                        //LogUtils.i(res);
                        JSONObject jsonObject = new JSONObject(res);
                        final String msg = jsonObject.getJSONObject("data").getString("msg");
                        final int code = jsonObject.getInt("code");
                        ThreadUtils.runOnUiThread(() -> {
                            closeLoadingDialog();
                            if(code==1){
                                try {
                                    JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("data");
                                    List<Message> list = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++){
                                        Message message = new Message();
                                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                        message.setFromuser(jsonObject2.getString("fromuser"));
                                        message.setMsg(jsonObject2.getString("msg"));
                                        message.setTime(TimeUtils.string2Date(jsonObject2.getString("time")));
                                        list.add(message);
                                    }
                                    if(list.size() == 0){
                                        Toast("暂无消息");
                                    }
                                    showMessage(list);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast(e.toString());
                                }
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
    public void showMessage(List<Message> messages) {
        adapter = new MessageAdapter(messages);
        rv_messageactivity.setAdapter(adapter);
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
