package com.qx.pjbj.ui.add;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.qx.pjbj.R;
import com.qx.pjbj.base.BaseActivity;
import com.qx.pjbj.base.BasePresenter;
import com.qx.pjbj.data.PjNote;
import com.qx.pjbj.data.PublicActivityInfo;
import com.qx.pjbj.data.QQLoginConfig;
import com.qx.pjbj.ui.add.getappinfo.GetAppInfoActivity;
import com.qx.pjbj.utils.HttpConnectionUtil;
import com.qx.pjbj.utils.ImeiUtils;
import com.qx.pjbj.utils.MySpUtils;
import com.qx.pjbj.view.loading.ILoadingView;
import com.qx.pjbj.view.loading.MyLoadingDialog;
import com.tencent.tauth.Tencent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Create by QianXiao
 * On 2020/7/29
 */
public class PostActivity extends BaseActivity implements ILoadingView {
    private ScrollView sv_post;
    private FloatingActionsMenu fam_post;
    private RelativeLayout rv_main;
    private FloatingActionButton fab_select_game_post,fab_clear_post,fab_post_post;
    private TextInputEditText et_gamename_post,et_packagename_post,et_mothod_post;
    private Spinner spinner_modtype;

    private Tencent tencent;
    private MyLoadingDialog loadingDialog;

    private Mode currentMode = Mode.ADD;
    private long editNoteId = -1;

    public enum Mode{
        ADD,EDIT
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_post;
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
        rv_main = (RelativeLayout) f(R.id.rv_main);
        sv_post = (ScrollView) f(R.id.sv_post);
        fam_post = (FloatingActionsMenu) f(R.id.fam_post);
        fab_select_game_post = (FloatingActionButton) f(R.id.fab_select_game_post);
        fab_clear_post = (FloatingActionButton) f(R.id.fab_clear_post);
        fab_post_post = (FloatingActionButton) f(R.id.fab_post_post);
        et_gamename_post = (TextInputEditText) f(R.id.et_gamename_post);
        et_packagename_post = (TextInputEditText) f(R.id.et_packagename_post);
        et_mothod_post = (TextInputEditText) f(R.id.et_mothod_post);
        spinner_modtype = (Spinner) f(R.id.spinner_modtype);
    }

    @Override
    protected void initListener() {
        //页面滚动时 收起悬浮按钮
        sv_post.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if(fam_post.isExpanded()){
                fam_post.collapse();
            }
        });
        fam_post.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                if(KeyboardUtils.isSoftInputVisible(PostActivity.this)){
                    KeyboardUtils.hideSoftInput(PostActivity.this);
                }
            }

            @Override
            public void onMenuCollapsed() {

            }
        });
        fab_select_game_post.setOnClickListener(v -> {
            Intent intent = new Intent(context, GetAppInfoActivity.class);
            startActivityForResult(intent, PublicActivityInfo.GetAppInfoActivityREQUEST_CODE);
            fam_post.collapse();
        });
        fab_clear_post.setOnClickListener(v -> {
            et_gamename_post.setText("");
            et_packagename_post.setText("");
            et_mothod_post.setText("");
            fam_post.collapse();
            et_gamename_post.requestFocus();
        });
        fab_post_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gamename = Objects.requireNonNull(et_gamename_post.getText()).toString().trim();
                if(TextUtils.isEmpty(gamename)){
                    Toast("应用名称不能为空");
                    et_gamename_post.setText("");
                    et_gamename_post.requestFocus();
                    return;
                }
                String gamepkgname = Objects.requireNonNull(et_packagename_post.getText()).toString().trim();
                if(TextUtils.isEmpty(gamepkgname)){
                    Toast("应用包名不能为空");
                    et_packagename_post.setText("");
                    et_packagename_post.requestFocus();
                    return;
                }
                if(spinner_modtype.getSelectedItemPosition()==0){
                    Toast("请选择应用修改类型");
                    spinner_modtype.performClick();
                    return;
                }
                String[] modtypes = context.getResources().getStringArray(R.array.modtype);
                String modtype = modtypes[spinner_modtype.getSelectedItemPosition()];
                String method = Objects.requireNonNull(et_mothod_post.getText()).toString().trim();
                if(TextUtils.isEmpty(method)){
                    Toast("破解关键说明不能为空");
                    et_mothod_post.setText("");
                    et_mothod_post.requestFocus();
                    return;
                }
                if(tencent == null){
                    tencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
                }
                JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
                String token = MySpUtils.getString("token");
                if(session==null||TextUtils.isEmpty(token)){
                    Toast("未登录");
                    return;
                }
                final String imei = ImeiUtils.getImei(context);
                if(TextUtils.isEmpty(imei)){
                    Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                    return;
                }
                if(currentMode == Mode.EDIT){
                    openLoadingDialog("更新中");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String access_token = session.getString("access_token");
                                final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                                final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                        "imei=%s&access_token=%s&id=%d&appname=%s&apppkgname=%s&modtype=%s&method=%s&token=%s&timestamp=%spjbj",
                                        imei,access_token,editNoteId,gamename,gamepkgname,modtype,method,token,timestamp));
                                Map<String, String> parms = new HashMap<>();
                                parms.put("imei", imei);
                                parms.put("access_token", access_token);
                                parms.put("id", String.valueOf(editNoteId));
                                parms.put("appname", gamename);
                                parms.put("apppkgname", gamepkgname);
                                parms.put("modtype", modtype);
                                parms.put("method", method);
                                parms.put("token", token);
                                parms.put("timestamp", timestamp);
                                parms.put("parsign", parsign);
                                String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/editNote.php",parms);
                                LogUtils.i(res);
                                JSONObject jsonObject = new JSONObject(res);
                                final int code = jsonObject.getInt("code");
                                final String msg = jsonObject.getJSONObject("data").getString("msg");
                                ThreadUtils.runOnUiThread(() -> {
                                    closeLoadingDialog();
                                    Toast(msg);
                                    if(code == 1){
                                        //更新成功
                                        setResult(RESULT_OK,getIntent());
                                        finish();
                                    }
                                });
                            } catch (JSONException e) {
                                LogUtils.e(e.toString());
                                ToastUtils.showShort(e.toString());
                                ThreadUtils.runOnUiThread(PostActivity.this::closeLoadingDialog);
                            }

                        }
                    }).start();
                }else{
                    openLoadingDialog("分享中");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String access_token = session.getString("access_token");
                                final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                                final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                        "imei=%s&access_token=%s&appname=%s&apppkgname=%s&modtype=%s&method=%s&token=%s&timestamp=%spjbj",
                                        imei,access_token,gamename,gamepkgname,modtype,method,token,timestamp));
                                Map<String, String> parms = new HashMap<>();
                                parms.put("imei", imei);
                                parms.put("access_token", access_token);
                                parms.put("appname", gamename);
                                parms.put("apppkgname", gamepkgname);
                                parms.put("modtype", modtype);
                                parms.put("method", method);
                                parms.put("token", token);
                                parms.put("timestamp", timestamp);
                                parms.put("parsign", parsign);
                                String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/post.php",parms);
                                LogUtils.i(res);
                                JSONObject jsonObject = new JSONObject(res);
                                final int code = jsonObject.getInt("code");
                                final String msg = jsonObject.getJSONObject("data").getString("msg");
                                ThreadUtils.runOnUiThread(() -> {
                                    closeLoadingDialog();
                                    Toast(msg);
                                    if(code == 1){
                                        //发帖成功
                                        fab_clear_post.performClick();
                                        setResult(RESULT_OK,getIntent());
                                    }
                                });
                            } catch (JSONException e) {
                                LogUtils.e(e.toString());
                                ToastUtils.showShort(e.toString());
                                ThreadUtils.runOnUiThread(PostActivity.this::closeLoadingDialog);
                            }

                        }
                    }).start();
                }
            }
        });
        //软键盘打开关闭监听以调整破解关键说明编辑框显示在软键盘上面
        KeyboardUtils.registerSoftInputChangedListener(this, new KeyboardUtils.OnSoftInputChangedListener() {
            @Override
            public void onSoftInputChanged(int height) {
                if(height>20){
                    if(fam_post.isExpanded()){
                        fam_post.collapse();
                    }
                    fam_post.setVisibility(View.GONE);
                    sv_post.setPadding(0,0,0,0);
                    rv_main.setPadding(0,0,0,height + ConvertUtils.dp2px(10));
                }else{
                    fam_post.setVisibility(View.VISIBLE);
                    sv_post.setPadding(0,0,0,ConvertUtils.dp2px(100));
                    rv_main.setPadding(0,0,0,0);
                }
            }
        });
    }

    @Override
    protected void initData() {
        showBackButton();
        Intent intent = getIntent();
        currentMode = Mode.values()[intent.getIntExtra("mode",0)];
        if(currentMode == Mode.ADD){
            setTitle("分享笔记");
        }else{
            setTitle("修改笔记");
            fab_post_post.setTitle("保存");
            PjNote note = (PjNote) intent.getSerializableExtra("note");
            if(note == null){
                AppUtils.exitApp();
            }
            assert note != null;
            editNoteId = note.getId();
            et_gamename_post.setText(note.getGamename());
            et_packagename_post.setText(note.getPackagename());
            et_mothod_post.setText(note.getMothod());
            String[] modtypes = context.getResources().getStringArray(R.array.modtype);
            Map<String,Integer> modtypesmap = new LinkedHashMap<>();
            for (int i = 0; i < modtypes.length; i++) {
                modtypesmap.put(modtypes[i],i);
            }
            Integer index = modtypesmap.get(note.getType());
            assert index != null;
            spinner_modtype.setSelection(index);
        }
        if(MySpUtils.getBoolean("switch_autoopenselectapp_setting")){
            fab_select_game_post.performClick();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PublicActivityInfo.GetAppInfoActivityREQUEST_CODE:
                if(resultCode == RESULT_OK){
                    String appname = data.getStringExtra("appname");
                    String apppkname = data.getStringExtra("apppkname");
                    et_gamename_post.setText(appname);
                    et_packagename_post.setText(apppkname);
                    if(spinner_modtype.getSelectedItemPosition()==0){
                        spinner_modtype.performClick();
                    }else{
                        et_mothod_post.requestFocus();
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        KeyboardUtils.unregisterSoftInputChangedListener(this.getWindow());
        super.onDestroy();
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
