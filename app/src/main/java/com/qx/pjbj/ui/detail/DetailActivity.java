package com.qx.pjbj.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.qx.pjbj.R;
import com.qx.pjbj.base.BaseActivity;
import com.qx.pjbj.base.BasePresenter;
import com.qx.pjbj.data.PjNote;
import com.qx.pjbj.data.PublicActivityInfo;
import com.qx.pjbj.data.QQLoginConfig;
import com.qx.pjbj.ui.add.PostActivity;
import com.qx.pjbj.utils.ClipboardUtils;
import com.qx.pjbj.utils.HttpConnectionUtil;
import com.qx.pjbj.utils.ImeiUtils;
import com.qx.pjbj.utils.MySpUtils;
import com.qx.pjbj.utils.ScreenShootUtils;
import com.qx.pjbj.view.loading.MyLoadingDialog;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.qx.pjbj.MyApplication.myUserInfo;

/**
 * Create by QianXiao
 * On 2020/7/28
 */
public class DetailActivity extends BaseActivity implements IDetailView {
    private long noteid = -1;
    private PjNote pjNote;
    private TextView tv_gamename_detail,tv_packagename_detail,tv_type_detail,tv_author_detail,tv_time_detail,tv_method_detail;
    private TextView tv_like_detail,tv_praise_detail;
    private LinearLayout ll_gamename_detail;
    private ScrollView sv_detail;
    private FloatingActionsMenu fam_detail;
    private FloatingActionButton fab_pass_detail,fab_refuse_detail;

    private boolean isauthor = false;
    private MenuItem edititem,deleteItem,lookbyjfItem;

    private Tencent tencent;
    private MyLoadingDialog loadingDialog;

    private final String path = PathUtils.getCachePathExternalFirst()+System.getProperty("file.separator")+"share.jpeg";

    private PageType currentPageType = PageType.Default;

    public enum PageType{
        Default,Examine
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_detail;
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
        fam_detail = (FloatingActionsMenu) f(R.id.fam_detail);
        fab_pass_detail = (FloatingActionButton) f(R.id.fab_pass_detail);
        fab_refuse_detail = (FloatingActionButton) f(R.id.fab_refuse_detail);

        sv_detail = (ScrollView) f(R.id.sv_detail);
        ll_gamename_detail = (LinearLayout) f(R.id.ll_gamename_detail);
        tv_gamename_detail = (TextView) f(R.id.tv_gamename_detail);
        tv_packagename_detail = (TextView) f(R.id.tv_packagename_detail);
        tv_type_detail = (TextView) f(R.id.tv_type_detail);
        tv_author_detail = (TextView) f(R.id.tv_author_detail);
        tv_time_detail = (TextView) f(R.id.tv_time_detail);
        tv_method_detail = (TextView) f(R.id.tv_method_detail);
        tv_like_detail = (TextView) f(R.id.tv_like_detail);
        tv_praise_detail = (TextView) f(R.id.tv_praise_detail);
    }

    @Override
    protected void initListener() {
        fab_pass_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //审核通过
                JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
                String token = MySpUtils.getString("token");
                if(session==null||TextUtils.isEmpty(token)){
                    Toast("未登录");
                }else{
                    String imei = ImeiUtils.getImei(context);
                    if(TextUtils.isEmpty(imei)){
                        Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                        return;
                    }
                    final int[] points = {0};
                    TextView textView = new TextView(context);
                    EditText textInputEditText = new EditText(context);
                    textInputEditText.setText(myUserInfo.getNick());
                    textInputEditText.setHint("请输入此笔记所得积分数量");
                    textInputEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    switch (pjNote.getType()){
                        case "存档":
                        case "dex修改":
                            points[0] = 4;
                            textInputEditText.setText("4");
                            break;
                        case "dll修改":
                        case "dat-so修改":
                        case "其他":
                            points[0] = 5;
                            textInputEditText.setText("5");
                            break;
                        case "so修改":
                            points[0] = 6;
                            textInputEditText.setText("6");
                            break;
                        default:
                            textInputEditText.setText("0");
                            break;
                    }
                    textView.setText("该条笔记类型为："+pjNote.getType()+"\n一般正常+"+points[0] +"积分，管理员可根据质量多加或少加1-2分。");

                    LinearLayout linearLayout = new LinearLayout(context);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                    linearLayout.setLayoutParams(lp);
                    linearLayout.setPadding(ConvertUtils.dp2px(10),ConvertUtils.dp2px(10),ConvertUtils.dp2px(10),0);
                    linearLayout.addView(textInputEditText);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setTitle("获取积分数量")
                            .setView(linearLayout)
                            .setPositiveButton("确定", null)
                            .setNegativeButton("取消", null)
                            .setCancelable(false);
                    AlertDialog dialog = builder.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            points[0] = Integer.parseInt(textInputEditText.getText().toString().trim());
                            switch (pjNote.getType()){
                                case "存档":
                                case "dex修改":
                                    if(points[0]<2||points[0]>6){
                                        Toast(pjNote.getType()+"类型加分范围2-6分");
                                        return;
                                    }
                                    break;
                                case "dll修改":
                                case "dat-so修改":
                                case "其他":
                                    if(points[0]<3||points[0]>7){
                                        Toast(pjNote.getType()+"类型加分范围3-7分");
                                        return;
                                    }
                                    break;
                                case "so修改":
                                    if(points[0]<4||points[0]>8){
                                        Toast(pjNote.getType()+"类型加分范围4-8分");
                                        return;
                                    }
                                    break;
                                default:
                                    break;
                            }
                            if(points[0]>0&&points[0]<=8){
                                openLoadingDialog("请求服务器");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            final String access_token = session.getString("access_token");
                                            final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                                            final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                                    "imei=%s&access_token=%s&id=%d&points=%d&token=%s&timestamp=%spjbj",
                                                    imei,access_token,noteid, points[0],token,timestamp));
                                            Map<String, String> parms = new HashMap<>();
                                            parms.put("imei", imei);
                                            parms.put("access_token", access_token);
                                            parms.put("id", String.valueOf(noteid));
                                            parms.put("points", String.valueOf(points[0]));
                                            parms.put("token", token);
                                            parms.put("timestamp", timestamp);
                                            parms.put("parsign", parsign);
                                            String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/managerPassNote.php",parms);
                                            LogUtils.i(res);
                                            JSONObject jsonObject = new JSONObject(res);
                                            final int code = jsonObject.getInt("code");
                                            final String msg = jsonObject.getJSONObject("data").getString("msg");
                                            ThreadUtils.runOnUiThread(() -> {
                                                closeLoadingDialog();
                                                Toast(msg);
                                                if(code == 1 || code==-11){
                                                    //审核通过成功 或已被审核
                                                    setResult(RESULT_OK);
                                                    finish();
                                                }
                                            });
                                        } catch (JSONException e) {
                                            LogUtils.e(e.toString());
                                            ThreadUtils.runOnUiThread(() -> closeLoadingDialog());
                                        }

                                    }
                                }).start();
                            }else if(points[0]<=0){
                                Toast("积分值应为正数");
                            }else{
                                Toast("积分值过大，最高支持+8分");
                            }
                        }
                    });

                }
            }
        });
        fab_refuse_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //审核拒绝
                int points = 100;
                JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
                String token = MySpUtils.getString("token");
                if(session==null||TextUtils.isEmpty(token)){
                    Toast("未登录");
                }else{
                    String imei = ImeiUtils.getImei(context);
                    if(TextUtils.isEmpty(imei)){
                        Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                        return;
                    }
                    final String[] reason = {"无"};
                    EditText textInputEditText = new EditText(context);
                    textInputEditText.setText("");
                    textInputEditText.setHint("请输入回绝理由");
                    LinearLayout linearLayout = new LinearLayout(context);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                    linearLayout.setLayoutParams(lp);
                    linearLayout.setPadding(ConvertUtils.dp2px(10),ConvertUtils.dp2px(10),ConvertUtils.dp2px(10),0);
                    linearLayout.addView(textInputEditText);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setTitle("回绝理由")
                            .setView(linearLayout)
                            .setPositiveButton("确定", null)
                            .setNegativeButton("取消", null)
                            .setCancelable(false);
                    AlertDialog dialog = builder.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            reason[0] = textInputEditText.getText().toString().trim();
                            if(TextUtils.isEmpty(reason[0])){
                                Toast("回拒理由不能为空");
                            }else{
                                openLoadingDialog("请求服务器");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            final String access_token = session.getString("access_token");
                                            final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                                            final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                                    "imei=%s&access_token=%s&id=%d&reason=%s&token=%s&timestamp=%spjbj",
                                                    imei,access_token,noteid, reason[0],token,timestamp));
                                            Map<String, String> parms = new HashMap<>();
                                            parms.put("imei", imei);
                                            parms.put("access_token", access_token);
                                            parms.put("id", String.valueOf(noteid));
                                            parms.put("reason", reason[0]);
                                            parms.put("token", token);
                                            parms.put("timestamp", timestamp);
                                            parms.put("parsign", parsign);
                                            String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/managerDeleteNote.php",parms);
                                            LogUtils.i(res);
                                            JSONObject jsonObject = new JSONObject(res);
                                            final int code = jsonObject.getInt("code");
                                            final String msg = jsonObject.getJSONObject("data").getString("msg");
                                            ThreadUtils.runOnUiThread(() -> {
                                                closeLoadingDialog();
                                                Toast(msg);
                                                if(code == 1 || code==-11){
                                                    //审核拒绝成功 或已被拒绝
                                                    setResult(RESULT_OK);
                                                    finish();
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
                }
            }
        });
        tv_packagename_detail.setSelected(true);
        tv_type_detail.setSelected(true);
        tv_author_detail.setSelected(true);
        tv_gamename_detail.setOnClickListener(v -> {
            ClipboardUtils.Copy2Clipboard(pjNote.getGamename());
            Toast("应用名已复制至剪贴板");
        });
        tv_packagename_detail.setOnClickListener(v -> {
            ClipboardUtils.Copy2Clipboard(pjNote.getPackagename());
            Toast("包名已复制至剪贴板");
        });
        tv_method_detail.setOnClickListener(v -> {
            if(!TextUtils.isEmpty(pjNote.getMothod())){
                ClipboardUtils.Copy2Clipboard(pjNote.getMothod());
                Toast("修改关键已复制至剪贴板");
            }
        });
        tv_praise_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pjNote.isPass()){
                    Toast("帖子暂未审核通过");
                    return;
                }
                JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
                String token = MySpUtils.getString("token");
                if(session==null||TextUtils.isEmpty(token)){
                    Toast("未登录");
                }else{
                    String imei = ImeiUtils.getImei(context);
                    if(TextUtils.isEmpty(imei)){
                        Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                        return;
                    }
                    openLoadingDialog("请求服务器");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String access_token = session.getString("access_token");
                                final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                                final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                        "imei=%s&access_token=%s&id=%d&token=%s&timestamp=%spjbj",
                                        imei,access_token,noteid,token,timestamp));
                                Map<String, String> parms = new HashMap<>();
                                parms.put("imei", imei);
                                parms.put("access_token", access_token);
                                parms.put("id", String.valueOf(noteid));
                                parms.put("token", token);
                                parms.put("timestamp", timestamp);
                                parms.put("parsign", parsign);
                                String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/praise.php",parms);
                                LogUtils.i(res);
                                JSONObject jsonObject = new JSONObject(res);
                                final int code = jsonObject.getInt("code");
                                final String msg = jsonObject.getJSONObject("data").getString("msg");
                                ThreadUtils.runOnUiThread(() -> {
                                    closeLoadingDialog();
                                    Toast(msg);
                                    if(code == 1){
                                        //点赞成功
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
        });
        tv_like_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pjNote.isPass()){
                    Toast("帖子暂未审核通过");
                    return;
                }
                JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
                String token = MySpUtils.getString("token");
                if(session==null||TextUtils.isEmpty(token)){
                    Toast("未登录");
                }else{
                    final String imei = ImeiUtils.getImei(context);
                    if(TextUtils.isEmpty(imei)){
                        Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                        return;
                    }
                    openLoadingDialog("请求服务器");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String access_token = session.getString("access_token");
                                final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                                final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                        "imei=%s&access_token=%s&id=%d&token=%s&timestamp=%spjbj",
                                        imei,access_token,noteid,token,timestamp));
                                Map<String, String> parms = new HashMap<>();
                                parms.put("imei", imei);
                                parms.put("access_token", access_token);
                                parms.put("id", String.valueOf(noteid));
                                parms.put("token", token);
                                parms.put("timestamp", timestamp);
                                parms.put("parsign", parsign);
                                String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/like.php",parms);
                                LogUtils.i(res);
                                JSONObject jsonObject = new JSONObject(res);
                                final int code = jsonObject.getInt("code");
                                final String msg = jsonObject.getJSONObject("data").getString("msg");
                                ThreadUtils.runOnUiThread(() -> {
                                    closeLoadingDialog();
                                    Toast(msg);
                                    if(code == 1){
                                        if(!msg.contains("取消收藏")){
                                            //收藏成功
                                            tv_like_detail.setText("取消收藏");
                                        }else{
                                            tv_like_detail.setText("收藏");
                                        }
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
        });
    }

    @Override
    protected void initData() {
        FileUtils.delete(path);
        showBackButton();
        Intent intent = getIntent();
        assert intent != null;
        currentPageType = PageType.values()[intent.getIntExtra("pagetype",0)];
        if(currentPageType == PageType.Examine){
            tv_like_detail.setVisibility(View.GONE);
            tv_praise_detail.setVisibility(View.GONE);
            fam_detail.setVisibility(View.VISIBLE);
            fam_detail.expand();
        }
        noteid = intent.getLongExtra("id",-1);
        if(noteid == -1){
            AppUtils.exitApp();
        }
        pjNote = (PjNote) intent.getSerializableExtra("pjnote");
        if(pjNote==null){
            AppUtils.exitApp();
        }
        setTitle(pjNote.getGamename());
        tv_gamename_detail.setText(pjNote.getGamename());
        tv_packagename_detail.setText(pjNote.getPackagename());
        tv_type_detail.setText(pjNote.getType());
        tv_author_detail.setText(pjNote.getAuthor());
        tv_time_detail.setText(TimeUtils.date2String(pjNote.getUpdatetime(),"yyyy-MM-dd HH:mm:ss"));
        if(!pjNote.isPass()){
            tv_praise_detail.setVisibility(View.GONE);
            tv_like_detail.setVisibility(View.GONE);
        }
        onRefresh(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        edititem = menu.findItem(R.id.menu_item_edit);
        deleteItem = menu.findItem(R.id.menu_item_delete);
        lookbyjfItem = menu.findItem(R.id.menu_item_lookbyjf);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_refresh:
                //刷新
                onRefresh(true);
                break;
            case R.id.menu_item_share:
                ll_gamename_detail.setVisibility(View.VISIBLE);
                break;
            case R.id.menu_item_share_text:
                //分享文本
                String ShareStr = getShareStr();
                Intent StringIntent = new Intent(Intent.ACTION_SEND);
                StringIntent.setType("text/plain");
                StringIntent.putExtra(Intent.EXTRA_TEXT, ShareStr);
                startActivity(Intent.createChooser(StringIntent, "分享"));
                break;
            case R.id.menu_item_share_pictoqq:
                //分享图片
                Bitmap bitmap = ScreenShootUtils.getBitmapByView(sv_detail);
                ImageUtils.save(bitmap, path,Bitmap.CompressFormat.JPEG);
                final Bundle params = new Bundle();
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL,path);
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, 0x00);
                tencent.shareToQQ(DetailActivity.this, params, new IUiListener(){

                    @Override
                    public void onComplete(Object o) {
                        ll_gamename_detail.setVisibility(View.GONE);
                        FileUtils.delete(path);
                    }

                    @Override
                    public void onError(UiError uiError) {
                        Toast("分享错误（"+uiError.toString()+")");
                        ll_gamename_detail.setVisibility(View.GONE);
                        FileUtils.delete(path);
                    }

                    @Override
                    public void onCancel() {
                        Toast("分享取消");
                        ll_gamename_detail.setVisibility(View.GONE);
                        FileUtils.delete(path);
                    }
                });
                break;
            case R.id.menu_item_edit:
                //编辑
                JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
                String token = MySpUtils.getString("token");
                if(session==null||TextUtils.isEmpty(token)){
                    Toast("未登录");
                }else if(!pjNote.isPass()) {
                    Toast("帖子正在审核，请等待通过后再编辑");
                }else{
                    Intent intent = new Intent(context,PostActivity.class);
                    intent.putExtra("mode",PostActivity.Mode.EDIT.ordinal());
                    intent.putExtra("note",pjNote);
                    startActivityForResult(intent, PublicActivityInfo.PostActivityREQUEST_CODE_FROM_DETAIL);
                }
                break;
            case R.id.menu_item_delete:
                //删除
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("温馨提示");
                if(!pjNote.isPass()){
                    builder.setTitle("帖子还没有审核通过，您确定现在就要删除您的这条笔记吗？\n删除后无法恢复哦。");
                }else{
                    builder.setTitle("您确定要删除您的这条笔记吗？\n删除后无法恢复哦。");
                }
                builder.setPositiveButton("确定", (dialog, which) -> {
                            deleteNote();
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false);
                builder.show();
                break;
            case R.id.menu_item_lookbyjf:
                LookByJf();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void LookByJf() {
        JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null||TextUtils.isEmpty(token)){
            Toast("未登录");
        }else{
            final String imei = ImeiUtils.getImei(context);
            if(TextUtils.isEmpty(imei)){
                Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                return;
            }
            openLoadingDialog("正在兑换");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String access_token = session.getString("access_token");
                        final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                        final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                "imei=%s&access_token=%s&id=%d&token=%s&timestamp=%spjbj",
                                imei,access_token,noteid,token,timestamp));
                        Map<String, String> parms = new HashMap<>();
                        parms.put("imei", imei);
                        parms.put("access_token", access_token);
                        parms.put("id", String.valueOf(noteid));
                        parms.put("token", token);
                        parms.put("timestamp", timestamp);
                        parms.put("parsign", parsign);
                        String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/exchangeNote.php",parms);
                        //LogUtils.i(res);
                        JSONObject jsonObject = new JSONObject(res);
                        final int code = jsonObject.getInt("code");
                        final String msg = jsonObject.getJSONObject("data").getString("msg");
                        ThreadUtils.runOnUiThread(() -> {
                            Toast(msg);
                            if(code == 1){
                                onRefresh(true);
                            }
                        });
                        ThreadUtils.runOnUiThread(() -> closeLoadingDialog());
                    } catch (JSONException e) {
                        LogUtils.e(e.toString());
                        ThreadUtils.runOnUiThread(() -> closeLoadingDialog());
                    }

                }
            }).start();
        }
    }

    private void deleteNote() {
        if(tencent==null){
            tencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
        }
        JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null||TextUtils.isEmpty(token)){
            Toast("未登录");
        }else{
            final String imei = ImeiUtils.getImei(context);
            if(TextUtils.isEmpty(imei)){
                Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                return;
            }
            openLoadingDialog("正在删除");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String access_token = session.getString("access_token");
                        final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                        final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                "imei=%s&access_token=%s&id=%d&token=%s&timestamp=%spjbj",
                                imei,access_token,noteid,token,timestamp));
                        Map<String, String> parms = new HashMap<>();
                        parms.put("imei", imei);
                        parms.put("access_token", access_token);
                        parms.put("id", String.valueOf(noteid));
                        parms.put("token", token);
                        parms.put("timestamp", timestamp);
                        parms.put("parsign", parsign);
                        String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/deleteNote.php",parms);
                        LogUtils.i(res);
                        JSONObject jsonObject = new JSONObject(res);
                        final String msg = jsonObject.getJSONObject("data").getString("msg");
                        final int code = jsonObject.getInt("code");
                        ThreadUtils.runOnUiThread(() -> {
                            closeLoadingDialog();
                            Toast(msg);
                            if(code==1){
                                setResult(RESULT_OK,getIntent());
                                finish();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PublicActivityInfo.PostActivityREQUEST_CODE_FROM_DETAIL:
                if(resultCode == RESULT_OK){
                    onRefresh(true);
                    setResult(RESULT_OK,getIntent());
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getShareStr(){
        StringBuilder c = new StringBuilder();;
        c.append("【应用名称】");
        c.append(pjNote.getGamename());
        c.append("\n【包名】");
        c.append(pjNote.getPackagename());
        c.append("\n【修改类型】");
        c.append(pjNote.getType());
        c.append("\n【作者】");
        c.append(pjNote.getAuthor());
        c.append("\n【时间】");
        c.append(TimeUtils.date2String(pjNote.getUpdatetime(),"yyyy-MM-dd HH:mm:ss"));
        c.append("\n【关键思路】");
        c.append(pjNote.getMothod());
        c.append("\n\n破解笔记，一款旨在用户共享逆向破解经验的软件。\n" +
                "点击链接查看并下载：http://pjbj.qianxiao.fun/");
        return c.toString();
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

    @Override
    public void onRefresh(boolean isshowloadingdialog) {
        if(tencent==null){
            tencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
        }
        JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
        String token = MySpUtils.getString("token");
        if(session==null||TextUtils.isEmpty(token)){
            Toast("未登录");
        }else{
            final String imei = ImeiUtils.getImei(context);
            if(TextUtils.isEmpty(imei)){
                Toast("获取手机IMEI错误，请检查是否授予IMEI权限");
                return;
            }
            if(isshowloadingdialog){
                openLoadingDialog("正在加载");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String access_token = session.getString("access_token");
                        final String timestamp = String.valueOf(System.currentTimeMillis()/1000);
                        final String parsign = EncryptUtils.encryptSHA1ToString(String.format(
                                "imei=%s&access_token=%s&id=%d&token=%s&timestamp=%spjbj",
                                imei,access_token,noteid,token,timestamp));
                        Map<String, String> parms = new HashMap<>();
                        parms.put("imei", imei);
                        parms.put("access_token", access_token);
                        parms.put("id", String.valueOf(noteid));
                        parms.put("token", token);
                        parms.put("timestamp", timestamp);
                        parms.put("parsign", parsign);
                        String res = HttpConnectionUtil.getHttp().postRequset("http://pjbj.qianxiao.fun/getDetail.php",parms);
                        //LogUtils.i(res);
                        JSONObject jsonObject = new JSONObject(res);
                        final int code = jsonObject.getInt("code");
                        if(code==1){
                            jsonObject = jsonObject.getJSONObject("data");
                            String detail = jsonObject.getString("detail");
                            pjNote.setMothod(detail);

                            boolean like = jsonObject.getBoolean("like");
                            boolean hasexchange = jsonObject.getBoolean("hasexchange");
                            boolean isauthor = jsonObject.getBoolean("isauthor");
                            ThreadUtils.runOnUiThread(() -> {
                                //tv_like_detail.setVisibility(View.VISIBLE);
                                tv_method_detail.setText(detail);
                                if(like){
                                    tv_like_detail.setText("取消收藏");
                                }
                                if(isauthor){
                                    if(pjNote.isPass()){
                                        edititem.setVisible(true);
                                    }
                                    if(currentPageType == PageType.Default){
                                        deleteItem.setVisible(true);
                                    }
                                }
                                if(hasexchange){
                                    Toast("你已花费积分获得了该笔记得查看权限");
                                    lookbyjfItem.setVisible(false);
                                }
                            });
                        }else{
                            final String msg = jsonObject.getJSONObject("data").getString("msg");
                            ThreadUtils.runOnUiThread(() -> {
                                Toast(msg);
                                if(code == -7){
                                    //tv_like_detail.setVisibility(View.VISIBLE);

                                    lookbyjfItem.setVisible(true);
                                    int needjf = 5;
                                    switch (pjNote.getType()){
                                        case "so修改":
                                            needjf = 6;
                                            break;
                                        case "dll修改":
                                        case "dat-so修改":
                                        case "其他":
                                            needjf = 5;
                                            break;
                                        case "dex修改":
                                        case "存档":
                                            needjf = 4;
                                            break;
                                        default:
                                            break;
                                    }
                                    tv_method_detail.setText("请使用积分兑换查看该笔记权限，兑换该笔记永久查看权限需要"+needjf+"积分" +
                                            "\n\n您也可以在积分中心使用积分兑换永久VIP，积分可通过每日签到、发帖获取。您也可以联系浅笑获取卡密在个人中心兑换永久VIP。");

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                            .setTitle("温馨提示")
                                            .setMessage("你暂不是VIP用户，是否通过花费"+needjf+"积分来获取查看这条笔记的权限")
                                            .setPositiveButton("确定", (dialog, which) -> {
                                                LookByJf();
                                            })
                                            .setNegativeButton("不了", null)
                                            .setCancelable(false);
                                    builder.show();
                                }else if(code == -11){
                                    tv_method_detail.setText("该笔记已被删除");
                                }
                            });
                        }
                        ThreadUtils.runOnUiThread(() -> closeLoadingDialog());
                    } catch (JSONException e) {
                        LogUtils.e(e.toString());
                        ThreadUtils.runOnUiThread(() -> closeLoadingDialog());
                    }

                }
            }).start();
        }
    }
}
