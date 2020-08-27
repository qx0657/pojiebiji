package com.qx.pjbj;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.util.LogUtils;
import com.qx.pjbj.data.MyUserInfo;

/**
 * Create by QianXiao
 * On 2020/7/23
 */
public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    public static MyUserInfo myUserInfo;
    public static boolean islogin = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LogUtils.getConfig().setLogSwitch(true);
        LogUtils.getConfig().setGlobalTag("PJBJ_TAG");
    }
}
