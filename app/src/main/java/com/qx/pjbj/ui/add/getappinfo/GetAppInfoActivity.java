package com.qx.pjbj.ui.add.getappinfo;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.qx.pjbj.R;
import com.qx.pjbj.base.BaseActivity;
import com.qx.pjbj.base.BasePresenter;
import com.qx.pjbj.ui.add.getappinfo.data.AppInfo;
import com.qx.pjbj.ui.add.getappinfo.view.AppInfoAdapter;
import com.qx.pjbj.ui.message.MessageActivity;
import com.qx.pjbj.view.MySearchView;
import com.qx.pjbj.view.loading.ILoadingView;
import com.qx.pjbj.view.loading.MyLoadingDialog;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Create by QianXiao
 * On 2020/7/29
 */
public class GetAppInfoActivity extends BaseActivity implements ILoadingView {
    private RecyclerView rv_appselect;
    private MyLoadingDialog loadingDialog;

    private AppInfoAdapter adapter;

    //searchview相关 用于在searchview展开时按返回实现关闭searchview
    private MySearchView searchView;
    private SearchView.SearchAutoComplete searchAutoComplete;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_appselect;
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
        rv_appselect = (RecyclerView) f(R.id.rv_appselect);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        showBackButton();
        setTitle("选择应用");
        rv_appselect.setLayoutManager(new LinearLayoutManager(context));
        openLoadingDialog("初始化应用");
        List<AppInfo> arrayListSafe2 = Collections.synchronizedList(new ArrayList<AppInfo>());
        new Thread(() -> {
            PackageManager packageInfo = getPackageManager();
            List<PackageInfo> allPackageList = packageInfo.getInstalledPackages(PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
            for (PackageInfo info : allPackageList) {
                long lastModified = new File(info.applicationInfo.sourceDir).lastModified();
                String appname = info.applicationInfo.loadLabel(packageInfo).toString();
                String apppkname = info.packageName;
                Drawable iconDrawable = info.applicationInfo.loadIcon(packageInfo);
                if((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){// 非系统应用
                    arrayListSafe2.add(new AppInfo(appname,apppkname,iconDrawable,lastModified));
                }
            }
            Collections.sort(arrayListSafe2);
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    closeLoadingDialog();
                    showAppInfos(arrayListSafe2);
                }
            });
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_getappinfo, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final MenuItem otherItem = menu.findItem(R.id.menu_item_refresh);
        searchView = (MySearchView) searchItem.getActionView();
        searchView.setActionExpandListener(new MySearchView.OnSearchViewActionExpandListener() {
            @Override
            public void onActionExpand() {

            }

            @Override
            public void onActionCollapse() {
                adapter.getFilter().filter(null);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //搜索
                adapter.getFilter().filter(newText, count -> {
                    if(count == 0){
                        //Toast("搜索结果为空");
                    }
                });
                return true;
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(searchAutoComplete.isShown() && !KeyboardUtils.isSoftInputVisible(this)){
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
        }else{
            super.onBackPressed();
        }
    }

    private void showAppInfos(List<AppInfo> data){
        //动画
        LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils
                .loadAnimation(this, R.anim.recycleview_item_anim));
        lac.setDelay(0.2f);
        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
        rv_appselect.setLayoutAnimation(lac);

        adapter = new AppInfoAdapter(data, appInfo -> {
            Intent data1 = new Intent();
            data1.putExtra("appname",appInfo.getAppname());
            data1.putExtra("apppkname",appInfo.getPackagename());
            setResult(RESULT_OK, data1);
            finish();
        });
        rv_appselect.setAdapter(adapter);
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
