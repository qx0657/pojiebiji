package com.qx.pjbj.ui.manage;

import android.content.Intent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import com.qx.pjbj.data.PublicActivityInfo;
import com.qx.pjbj.data.QQLoginConfig;
import com.qx.pjbj.ui.detail.DetailActivity;
import com.qx.pjbj.ui.main.view.PjnoteAdapter;
import com.qx.pjbj.utils.HttpConnectionUtil;
import com.qx.pjbj.utils.ImeiUtils;
import com.qx.pjbj.utils.MySpUtils;
import com.qx.pjbj.view.loading.MyLoadingDialog;
import com.tencent.tauth.Tencent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by QianXiao
 * On 2020/8/22
 */
public class ManageActivity extends BaseActivity implements IManageView{
    private RecyclerView rv_manageactivity;

    private PjnoteAdapter adapter;
    private PjnoteAdapter.OnItemOnClickListener onItemOnClickListener;
    private Tencent tencent;
    private MyLoadingDialog loadingDialog;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_manage;
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
        rv_manageactivity = (RecyclerView) f(R.id.rv_manageactivity);
    }

    @Override
    protected void initListener() {
        onItemOnClickListener = new PjnoteAdapter.OnItemOnClickListener() {
            @Override
            public void OnClick(PjNote pjNote, View view) {
                long id = pjNote.getId();
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("pagetype",DetailActivity.PageType.Examine.ordinal());
                intent.putExtra("id",id);
                intent.putExtra("pjnote",pjNote);
                startActivityForResult(intent, PublicActivityInfo.DetailActivityREQUEST_CODE);
            }
        };
    }

    @Override
    protected void initData() {
        showBackButton();
        setTitle("审核中心");
        rv_manageactivity.setLayoutManager(new LinearLayoutManager(context));
        requestData(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PublicActivityInfo.DetailActivityREQUEST_CODE:
                if(resultCode == RESULT_OK){
                    requestData(false);
                    setResult(RESULT_OK);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 0:
                requestData(true);
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
    public void requestData(boolean showtip) {
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
                        String res = HttpConnectionUtil.getHttp().postRequset("http://qianxiao.fun/app/pojiebiji/manageData.php",parms);
                        //LogUtils.i(res);
                        JSONObject jsonObject = new JSONObject(res);
                        final String msg = jsonObject.getJSONObject("data").getString("msg");
                        final int code = jsonObject.getInt("code");
                        ThreadUtils.runOnUiThread(() -> {
                            closeLoadingDialog();
                            if(code==1){
                                try {
                                    JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("data");
                                    List<PjNote> list = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++){
                                        PjNote pjNote = new PjNote();
                                        JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                        pjNote.setId(Long.parseLong(jsonObject2.getString("id")));
                                        pjNote.setGamename(jsonObject2.getString("gamename"));
                                        pjNote.setPackagename(jsonObject2.getString("packagename"));
                                        pjNote.setType(jsonObject2.getString("type"));
                                        pjNote.setPass(jsonObject2.getString("pass").equals("1"));
                                        pjNote.setAuthor(jsonObject2.getString("author"));
                                        pjNote.setUpdatetime(TimeUtils.string2Date(jsonObject2.getString("updatetime")));
                                        pjNote.setGood(Integer.parseInt(jsonObject2.getString("good")));
                                        pjNote.setLook(Integer.parseInt(jsonObject2.getString("look")));
                                        list.add(pjNote);
                                    }
                                    if(list.size() == 0&&showtip){
                                        Toast("暂无要审核的数据");
                                    }
                                    showData(list);
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
    public void showData(List<PjNote> pjNotes) {
        adapter = new PjnoteAdapter(pjNotes);
        adapter.setOnItemOnClickListener(onItemOnClickListener);
        rv_manageactivity.setAdapter(adapter);
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
