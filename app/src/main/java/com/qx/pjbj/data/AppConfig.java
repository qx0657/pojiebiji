package com.qx.pjbj.data;

import android.os.Environment;

import com.blankj.utilcode.util.AppUtils;

import java.io.File;

/**
 * Create by QianXiao
 * On 2020/7/29
 */
public class AppConfig {
    /**
     * 用于分享
     */
    public static final String APP_URL = "http://pjbj.qianxiao.fun/";
    /**
     * App外存目录：sdcard/破解笔记/
     */
    public static final String APP_DIR = Environment.getExternalStorageDirectory() + File.separator + AppUtils.getAppName() + File.separator;
}
