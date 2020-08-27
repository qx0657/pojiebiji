package com.qx.pjbj.utils;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.Utils;
import com.qx.pjbj.R;

/**
 * Updated by qianxiao on 2020/07/29.
 */

public class ScreenShootUtils {
    /**
     * view转bitmap
     * @param scrollView
     * @return
     */
    public static Bitmap getBitmapByView(ScrollView scrollView) {
        Bitmap bitmap = ImageUtils.view2Bitmap(scrollView.getChildAt(0));
        bitmap = addLogo(bitmap);
        bitmap = ImageUtils.toRoundCorner(bitmap,30);
        return bitmap;
    }

    /**
     * 拼接图片（添加logo）
     * @param bit1
     * @return 返回拼接后的Bitmap
     */
    private static Bitmap addLogo(Bitmap bit1){
        int width = bit1.getWidth();
        View view = LayoutInflater.from(Utils.getApp()).inflate(R.layout.layout_logo, null, false);
        Bitmap logo = ImageUtils.view2Bitmap(view);
        int height = bit1.getHeight() + logo.getHeight();
        //创建一个空的Bitmap(内存区域),宽度等于第一张图片的宽度，高度等于两张图片高度总和
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //将bitmap放置到绘制区域,并将要拼接的图片绘制到指定内存区域
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bit1, 0, 0, null);
        canvas.drawBitmap(logo, 0, bit1.getHeight(), null);
        return bitmap;
    }

}
