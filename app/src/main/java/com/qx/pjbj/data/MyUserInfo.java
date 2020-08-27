package com.qx.pjbj.data;

import java.io.Serializable;

/**
 * Create by QianXiao
 * On 2020/7/24
 */
public class MyUserInfo implements Serializable {
    /**
     * UnionID QQ用户唯一标识
     */
    private String uid;
    /**
     * 昵称
     */
    private String nick;
    /**
     * 头像
     */
    private String head_url;
    /**
     * 是否vip
     */
    private boolean vip = false;
    /**
     * 是否管理员
     */
    private boolean manager = false;
    /**
     * VIP开通时间
     */
    private String vipkttime = "";

    public MyUserInfo(String nick) {
        this.nick = nick;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getHead_url() {
        return head_url;
    }

    public void setHead_url(String head_url) {
        this.head_url = head_url;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        this.manager = manager;
    }

    public String getVipkttime() {
        return vipkttime;
    }

    public void setVipkttime(String vipkttime) {
        this.vipkttime = vipkttime;
    }

    @Override
    public String toString() {
        return "MyUserInfo{" +
                "uid='" + uid + '\'' +
                ", nick='" + nick + '\'' +
                ", head_url='" + head_url + '\'' +
                ", vip=" + vip +
                ", vipkttime='" + vipkttime + '\'' +
                '}';
    }
}
