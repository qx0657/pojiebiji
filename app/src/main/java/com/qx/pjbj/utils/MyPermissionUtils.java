package com.qx.pjbj.utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.Utils;

import java.util.List;

/**
 * Create by QianXiao
 * On 2020/7/29
 */
public class MyPermissionUtils {
    public static void requestStrongPermission(){
        PermissionUtils.permission(PermissionConstants.STORAGE)
                .rationale((activity, shouldRequest) -> shouldRequest.again(true))
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> granted) {

                    }

                    @Override
                    public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                        if (!deniedForever.isEmpty()) {
                            //永久禁止
                            AlertDialog.Builder builder = new AlertDialog.Builder(Utils.getApp())
                                    .setTitle("温馨提示")
                                    .setMessage("您已拒绝本软件再次请求存储权限，请前往设置页面手动授予本如那件存储权限。")
                                    .setPositiveButton("前往设置页面", (dialog, which) -> {
                                        PermissionUtils.launchAppDetailsSettings();
                                    })
                                    .setCancelable(false);
                            builder.show();
                        }else{
                            requestStrongPermission();
                        }
                    }
                })
                .request();
    }

    public static void requestPhonePermission(Context context){
        PermissionUtils.permission(PermissionConstants.PHONE)
                .rationale((activity, shouldRequest) -> shouldRequest.again(true))
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> granted) {

                    }

                    @Override
                    public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                        if (!deniedForever.isEmpty()) {
                            //永久禁止
                            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                    .setTitle("温馨提示")
                                    .setMessage("您已拒绝本软件再次请求IMEI权限，请前往设置页面手动授予本如那件存储权限。")
                                    .setPositiveButton("前往设置页面", (dialog, which) -> {
                                        PermissionUtils.launchAppDetailsSettings();
                                    })
                                    .setCancelable(false);
                            builder.show();
                            ((Activity) context).finish();
                        }else{
                            requestPhonePermission(context);
                        }
                    }
                })
                .request();
    }
}
