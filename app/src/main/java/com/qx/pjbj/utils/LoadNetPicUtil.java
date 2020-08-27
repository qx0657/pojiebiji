package com.qx.pjbj.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.blankj.utilcode.util.ThreadUtils;

/**
 * Create by QianXiao
 * On 2020/7/26
 */
public class LoadNetPicUtil {
    /**
     * 给imageview加载网络图片
     * @param imageView
     * @param url
     */
    public static void load(final ImageView imageView, final String url){
        if(TextUtils.isEmpty(url)){
            return;
        }
        new Thread(){
            @Override
            public void run() {
                final Bitmap bitmap = Util.getbitmap(url);
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        }.start();
    }
}
