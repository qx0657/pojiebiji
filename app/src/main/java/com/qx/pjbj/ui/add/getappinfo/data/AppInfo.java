package com.qx.pjbj.ui.add.getappinfo.data;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Create by QianXiao
 * On 2020/7/29
 */
public class AppInfo implements Comparable<AppInfo>{
    private String appname;
    private String packagename;
    private Drawable icon;
    private long lastModified;

    public AppInfo(String appname, String packagename, Drawable icon, long lastModified) {
        this.appname = appname;
        this.packagename = packagename;
        this.icon = icon;
        this.lastModified = lastModified;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public int compareTo(AppInfo o) {
        if(o.getLastModified()>this.getLastModified()){
            return 1;
        }
        if(o.getLastModified()<this.getLastModified()){
            return -1;
        }
        return 0;
    }
}
