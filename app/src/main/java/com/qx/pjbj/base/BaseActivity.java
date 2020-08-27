package com.qx.pjbj.base;

import android.content.Context;
import android.os.Bundle;

import android.transition.Fade;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.BarUtils;
import com.qx.pjbj.R;

/**
 * Create by QianXiao
 * On 2020/4/15
 */
public abstract class BaseActivity<T extends BasePresenter<?,?>>
        extends AppCompatActivity {
    public Context context;
    /**
     * 对应的Presenter层的类对象
     */
    public T mPresenter;

    public Toolbar toolbar;
    public ActionBar actionBar;
    public boolean notAddToobarMargin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //使用抽象方法在子类中实现来绑定对应layout页面
        setContentView(getLayoutID());
        int toolbar_id = getToolBarID();
        if(toolbar_id != 0){
            toolbar = f(toolbar_id);
            setSupportActionBar(toolbar);
            actionBar = getSupportActionBar();
        }

        context = this;
        //使用抽象方法在子类中实现来初始化Presenter层的类对象
        mPresenter = initPresenter();
        initView();
        initListener();
        initData();
        //状态栏透明
        BarUtils.transparentStatusBar(this);
        if(!notAddToobarMargin){
            BarUtils.addMarginTopEqualStatusBarHeight(toolbar);
        }else{
            BarUtils.setStatusBarColor(this,ContextCompat.getColor(context,R.color.colorPrimary));
        }
    }

    protected abstract int getLayoutID();

    protected abstract int getToolBarID();

    protected abstract T initPresenter();

    protected abstract void initView();

    protected abstract void initListener();

    protected abstract void initData();

    /**
     * 绑定控件
     * @param id
     * @param <E>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <E> E f(int id){
        return (E)findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public <E> E f(View view, int id){
        return (E)view.findViewById(id);
    }

    //抽象父类里也可以写一些公共方法
    public void Toast(String s){
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showBackButton(){
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }
}
