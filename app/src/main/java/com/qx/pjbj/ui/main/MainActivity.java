package com.qx.pjbj.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.qx.pjbj.R;
import com.qx.pjbj.base.BaseActivity;
import com.qx.pjbj.checkupdate.CheckUpdateManager;
import com.qx.pjbj.data.PjNote;
import com.qx.pjbj.data.PublicActivityInfo;
import com.qx.pjbj.data.QQLoginConfig;
import com.qx.pjbj.ui.about.AboutActivity;
import com.qx.pjbj.ui.add.PostActivity;
import com.qx.pjbj.ui.detail.DetailActivity;
import com.qx.pjbj.ui.jf.JfActivity;
import com.qx.pjbj.ui.main.presenter.MainPresenter;
import com.qx.pjbj.ui.main.view.IMainView;
import com.qx.pjbj.ui.main.view.PjnoteAdapter;
import com.qx.pjbj.ui.manage.ManageActivity;
import com.qx.pjbj.ui.message.MessageActivity;
import com.qx.pjbj.ui.setting.SettingActivity;
import com.qx.pjbj.ui.userinfo.UserInfoActivity;
import com.qx.pjbj.utils.LoadNetPicUtil;
import com.qx.pjbj.utils.MyPermissionUtils;
import com.qx.pjbj.utils.MySpUtils;
import com.qx.pjbj.view.MySearchView;
import com.qx.pjbj.view.OvalImageView;
import com.qx.pjbj.view.YesOrNoDialog;
import com.qx.pjbj.view.loading.MyLoadingDialog;
import com.tencent.connect.auth.AuthAgent;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.Tencent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.qx.pjbj.MyApplication.islogin;
import static com.qx.pjbj.MyApplication.myUserInfo;


public class MainActivity extends BaseActivity<MainPresenter> implements IMainView {
    //侧滑栏
    private DrawerLayout drawer;
    private NavigationView navigationView;
    //悬浮按钮
    private FloatingActionButton fab;
    //searchview相关 用于在searchview展开时按返回实现关闭searchview
    private MySearchView searchView;
    private SearchView.SearchAutoComplete searchAutoComplete;

    //用户头像和昵称
    private OvalImageView iv_user_head;
    private TextView tv_user_nick;

    private PjnoteAdapter adapter;
    private int start = 1;
    private int lastVisibleItem;
    private boolean canloadmore = true;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView rv_main;
    private PjnoteAdapter.OnItemOnClickListener onItemOnClickListener;

    private MyLoadingDialog loadingDialog;
    private CheckUpdateManager checkUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        notAddToobarMargin = true;
        setTheme(R.style.AppTheme);
        MainActivity.super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_main;
    }

    @Override
    protected int getToolBarID() {
        return R.id.toolbar;
    }

    @Override
    protected MainPresenter initPresenter() {
        MainPresenter presenter = new MainPresenter(context);
        presenter.attach(this);
        return presenter;
    }

    @Override
    protected void initView() {
        fab = f(R.id.fab);
        drawer = f(R.id.drawer_layout);
        navigationView = f(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        iv_user_head = f(headerView,R.id.iv_user_head);
        tv_user_nick = f(headerView,R.id.tv_user_nick);
        rv_main = f(R.id.rv_main);
    }

    @Override
    protected void initListener() {
        //悬浮按钮点击事件
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(islogin){
                    //if(myUserInfo.isVip()){
                        Intent intent = new Intent(context, PostActivity.class);
                        startActivityForResult(intent,PublicActivityInfo.PostActivityREQUEST_CODE);
                    //}else{
                    //    tipOpenVip();
                    //}
                }else {
                    Snackbar.make(view, "请先登录", Snackbar.LENGTH_LONG)
                            .setAction("立即登录", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mPresenter.Login();
                                }
                            }).show();
                }

            }
        });
        //设置侧滑栏和toolbar绑定 即toolbar左侧显示菜单按钮，可点击切换
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerToggle.syncState();
        drawer.addDrawerListener(drawerToggle);
        //设置侧滑栏点击事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean shouldclosedrawer = true;
                switch (item.getItemId()) {
                    case R.id.nav_myinfo:
                        if(islogin){
                            Intent intent = new Intent(context, UserInfoActivity.class);
                            startActivityForResult(intent, PublicActivityInfo.UserInfoActivityREQUEST_CODE);
                            shouldclosedrawer = false;
                        }else{
                            Toast("请先登录");
                            shouldclosedrawer = false;
                        }
                        break;
                    case R.id.nav_myjf:
                        shouldclosedrawer = false;
                        Intent intent = new Intent(context, JfActivity.class);
                        startActivityForResult(intent, PublicActivityInfo.JFActivityREQUEST_CODE);
                        break;
                    case R.id.nav_manage:
                        shouldclosedrawer = false;
                        Intent intent2 = new Intent(context, ManageActivity.class);
                        startActivityForResult(intent2, PublicActivityInfo.ManageActivityREQUEST_CODE);
                        break;
                    case R.id.nav_myshare:
                        if(!islogin){
                            Toast("请先登录");
                            shouldclosedrawer = false;
                        }
                        //如果不是vip提示开通vip
                        if(!myUserInfo.isVip()){
                            tipOpenVip();
                        }else{
                            mPresenter.loadMyShare();
                        }
                        break;
                    case R.id.nav_mycollection:
                        if(!islogin){
                            Toast("请先登录");
                            shouldclosedrawer = false;
                        }
                        //如果不是vip提示开通vip
                        if(!myUserInfo.isVip()){
                            tipOpenVip();
                        }else{
                            mPresenter.loadMyLike();
                        }
                        break;
                    case R.id.nav_setting:
                        //设置
                        startActivity(new Intent(context, SettingActivity.class));
                        shouldclosedrawer = false;
                        break;
                    case R.id.nav_about:
                        //关于
                        startActivity(new Intent(context, AboutActivity.class));
                        shouldclosedrawer = false;
                        break;
                    case R.id.nav_exit:
                        finish();
                        break;
                }
                if(shouldclosedrawer){
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ThreadUtils.runOnUiThread(() -> drawer.closeDrawers());
                        }
                    },500);
                }
                return true;
            }
        });
        //设置点击登录监听/已登录点击而实现退出
        View.OnClickListener onClickListener_login = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!islogin){
                    //登录
                    if(!mPresenter.isQQInstalled()){
                        MainActivity.this.getIntent().putExtra(AuthAgent.KEY_FORCE_QR_LOGIN, true);
                        Toast("检测到您的手机未安装QQ，请输入账号密码登录");
                    }else{
                        Toast("正在拉起QQ登录");
                    }
                    mPresenter.Login();
                }else{
                    new YesOrNoDialog(context, new YesOrNoDialog.YesOrNoDialogInterface() {
                        @Override
                        public void yesOnClick(YesOrNoDialog d) {
                            loginOut();
                            d.dismiss();
                        }

                        @Override
                        public void noOnClick(YesOrNoDialog d) {
                            d.dismiss();
                        }
                    }).Show().setContent(myUserInfo.getNick()+"已登录。是否退出登录？")
                            .setYesText("退出");
                }
            }
        };
        iv_user_head.setOnClickListener(onClickListener_login);
        tv_user_nick.setOnClickListener(onClickListener_login);
        rv_main.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /*
                到达底部了,如果不加!isLoading的话到达底部如果还一滑动的话就会一直进入这个方法
                就一直去做请求网络的操作,这样的用户体验肯定不好.添加一个判断,每次滑倒底只进行一次网络请求去请求数据
                当请求完成后,在把isLoading赋值为false,下次滑倒底又能进入这个方法了
                 */
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount() && !mPresenter.isLoading) {
                    //到达底部之后如果footView的状态不是正在加载的状态,就将 他切换成正在加载的状态
                    if(mPresenter.more && canloadmore){
                        adapter.changeState(1);
                        mPresenter.isLoading = true;
                        mPresenter.LoadMoreData(start,20);
                    }else{
                        adapter.changeState(2);
                    }

                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //拿到最后一个出现的item的位置
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });
        //条目点击事件
        onItemOnClickListener = new PjnoteAdapter.OnItemOnClickListener() {
            @Override
            public void OnClick(PjNote pjNote, View view) {
                Tencent tencent = Tencent.createInstance(QQLoginConfig.APP_ID,context,QQLoginConfig.AUTHORITIES);
                JSONObject session = tencent.loadSession(QQLoginConfig.APP_ID);
                final String token = MySpUtils.getString("token");
                if(session==null|| TextUtils.isEmpty(token)){
                    Snackbar.make(view, "请先登录", Snackbar.LENGTH_LONG)
                            .setAction("立即登录", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mPresenter.Login();
                                }
                            }).show();
                }else{
                    long id = pjNote.getId();
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("id",id);
                    intent.putExtra("pjnote",pjNote);
                    startActivityForResult(intent,PublicActivityInfo.DetailActivityREQUEST_CODE);
                }
            }
        };
    }

    private void tipOpenVip(){
        TextView textView = new TextView(context);
        textView.setPadding(ConvertUtils.dp2px(20),ConvertUtils.dp2px(15),ConvertUtils.dp2px(20),ConvertUtils.dp2px(5));
        textView.setText(Html.fromHtml("<pre style=\"color:black;font-size:20px;\">你目前为非VIP用户，暂不支持该功能。<br/><br/>你可以通过发布笔记积攒积分后再左侧侧滑栏-积分中心中进行兑换永久VIP。<br/><br/>您也可以选择支持作者，购买VIP卡密后在左侧侧滑栏-个人中心-进行兑换VIP，同时也是以维持服务器的继续运行。<br/><br/>VIP价值随数据库笔记数量的变化而变化。<br/><br/>浅笑感谢您的支持！！！<br/>发卡地址：<a href=\"https://w.url.cn/s/ASsUBlh\">https://w.url.cn/s/AmbQcZj</a></pre>"));
        textView.setTextColor(Color.parseColor("#383838"));
        textView.setTextSize(16);
        textView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse("https://www.csfaka.com/details/934DDF57");
            intent.setData(content_url);
            startActivity(intent);
        });
        //textView.setAutoLinkMask(Linkify.WEB_URLS);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("温馨提示")
                .setView(textView)
                .setPositiveButton("确定", (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse("https://w.url.cn/s/ASsUBlh");
                    intent.setData(content_url);
                    startActivity(intent);
                })
                .setNegativeButton("我知道了", null)
                .setCancelable(false);
        builder.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void loginOut(){
        islogin = false;
        if(mPresenter.mTencent!=null){
            mPresenter.mTencent.logout(context);
        }
        iv_user_head.setImageResource(R.drawable.default_head);
        tv_user_nick.setText("QQ登录");
        tv_user_nick.setTextColor(Color.WHITE);
    }

    @Override
    public void loginSuccess(String nick, final String head) {
        islogin = true;
        LoadNetPicUtil.load(iv_user_head,head);
        tv_user_nick.setText(nick);
        if(myUserInfo.isManager()){
            navigationView.getMenu().findItem(R.id.nav_manage).setVisible(true);
        }
    }

    @Override
    public void ktVipSuccess() {
        tv_user_nick.setTextColor(Color.parseColor("#FF4500"));
    }

    @Override
    public void changeState(int s) {
        adapter.changeState(s);
    }

    @Override
    public void setTile(String title) {
        setTitle(title);
    }

    YesOrNoDialog msgtipdialog;
    int lastnoreadmsgnum = 0;

    @Override
    public void showNoReadMsgNum(int num) {
        if(num==0){
            return;
        }
        if(msgtipdialog == null){
            msgtipdialog = new YesOrNoDialog(context, new YesOrNoDialog.YesOrNoDialogInterface() {
                @Override
                public void yesOnClick(YesOrNoDialog d) {
                    startActivity(new Intent(context, MessageActivity.class));
                    d.dismiss();
                }

                @Override
                public void noOnClick(YesOrNoDialog d) {
                    d.dismiss();
                }
            });
        }
        if(!msgtipdialog.isShowing()){
            lastnoreadmsgnum = num;

            msgtipdialog.Show().setContent("你有"+num+"条未读消息")
                    .setYesText("查看").setCancelable(false);;
        }else if(lastnoreadmsgnum!=num){
            lastnoreadmsgnum = num;
            msgtipdialog.dismiss();
            msgtipdialog.Show().setContent("你有"+num+"条未读消息")
                    .setYesText("查看").setCancelable(false);;
        }

    }

    private Timer timer;

    @Override
    protected void initData() {
        BarUtils.addMarginTopEqualStatusBarHeight(navigationView);
        linearLayoutManager = new LinearLayoutManager(context);
        rv_main.setLayoutManager(linearLayoutManager);
        //登陆初始化+缓存登录
        mPresenter.LoginInit();
        mPresenter.OnRefresh();
        //检查更新
        if(checkUpdateManager == null){
            checkUpdateManager = new CheckUpdateManager(context);
        }
        checkUpdateManager.check(true);
        //检查未读消息
        if(timer == null){
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mPresenter.getNoReadMsgNum();
            }
        },1000,10000);

        if(!PermissionUtils.isGranted(Manifest.permission.READ_PHONE_STATE)){
            MyPermissionUtils.requestPhonePermission(context);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Constants.REQUEST_LOGIN:
                Tencent.onActivityResultData(requestCode,resultCode,data, mPresenter.getLoginIUiListener());
                break;
            case PublicActivityInfo.UserInfoActivityREQUEST_CODE:
                if(resultCode == RESULT_OK){
                    String newnick = data.getStringExtra("newnick");
                    tv_user_nick.setText(newnick);
                    if(data.getBooleanExtra("vip",false)){
                        ktVipSuccess();
                    }
                }
                break;
            case PublicActivityInfo.ManageActivityREQUEST_CODE:
            case PublicActivityInfo.PostActivityREQUEST_CODE:
            case PublicActivityInfo.DetailActivityREQUEST_CODE:
                if(resultCode == RESULT_OK){
                    mPresenter.OnRefresh();
                }
                break;
            case PublicActivityInfo.JFActivityREQUEST_CODE:
                if(resultCode == RESULT_OK){
                    ktVipSuccess();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_refresh:
                //刷新
                canloadmore = true;
                mPresenter.more = true;
                mPresenter.OnRefresh();
                setTitle("破解笔记");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final MenuItem otherItem = menu.findItem(R.id.menu_item_refresh);
        searchView = (MySearchView) searchItem.getActionView();
        searchView.setActionExpandListener(new MySearchView.OnSearchViewActionExpandListener() {
            @Override
            public void onActionExpand() {
                otherItem.setVisible(false);
                canloadmore = false;
            }

            @Override
            public void onActionCollapse() {
                otherItem.setVisible(true);
                canloadmore = true;
                mPresenter.OnRefresh();
                setTile("破解笔记");
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(TextUtils.isEmpty(query.trim())){
                    Toast("请输入搜索关键字");
                    searchAutoComplete.setText("");
                    searchAutoComplete.requestFocus();
                    return false;
                }
                mPresenter.SearchNote(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //设置图标（叉叉）颜色
        ImageView mCloseButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        mCloseButton.setColorFilter(Color.WHITE);
        //设置searchview输入框文字颜色
        searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setTextColor(Color.WHITE);
        searchAutoComplete.setHintTextColor(Color.parseColor("#F5F5F5"));

        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawers();
        }else if(searchAutoComplete.isShown() && !KeyboardUtils.isSoftInputVisible(this)){
            //如果searchview正处展开，则按下返回键时将其关闭
            try {
                searchAutoComplete.setText("");//清除文本
                //利用反射调用收起SearchView的onCloseClicked()方法
                Method method = Class.forName("androidx.appcompat.widget.SearchView").getDeclaredMethod("onCloseClicked");
                method.setAccessible(true);
                method.invoke(searchView);
            } catch (Exception e) {
                LogUtils.e(e.toString());
            }
        }else if(MySpUtils.getBoolean("switch_backgroundrun_setting")){
            moveTaskToBack(false);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void RefreshDate(List<PjNote> data) {
        start = data.size();
        //动画
        LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils
                .loadAnimation(this, R.anim.recycleview_item_anim));
        lac.setDelay(0.2f);
        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
        rv_main.setLayoutAnimation(lac);

        adapter = new PjnoteAdapter(data);
        adapter.setOnItemOnClickListener(onItemOnClickListener);
        rv_main.setAdapter(adapter);
    }

    @Override
    public void addData(List<PjNote> data) {
        start += data.size();
        mPresenter.isLoading = false;
        adapter.addData(data);
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
    protected void onDestroy() {
        timer.cancel();
        timer = null;
        super.onDestroy();
    }
}
