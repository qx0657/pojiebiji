package com.qx.pjbj.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import com.blankj.utilcode.util.PhoneUtils;

/**
 * Create by QianXiao
 * On 2020/7/31
 */
public class ImeiUtils {
    public static String getImei(Context context){
        String imei = PhoneUtils.getIMEI();
        if(TextUtils.isEmpty(imei)){
            imei = Settings.System.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        if (TextUtils.isEmpty(imei)){
            imei = "";
        }
        return imei;
    }
}
