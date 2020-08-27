package com.qx.pjbj.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Create by QianXiao
 * On 2020/7/23
 */
public class PjNote implements Serializable,Comparable<PjNote>{
    /**
     * id
     */
    private Long id = -1l;
    /**
     * 游戏名称
     */
    private String gamename;
    /**
     * 游戏包名
     */
    private String packagename;
    /**
     * 修改类型
     */
    private String type;
    /**
     * 是否审核通过
     */
    private boolean pass = true;
    /**
     * 破解关键或思路
     */
    private String mothod;
    /**
     * 作者
     */
    private String author;
    /**
     * 更新时间
     */
    private Date updatetime;
    /**
     * 查看数量 点赞数量
     */
    private int look,good;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getGamename() {
        return gamename;
    }
    public void setGamename(String gamename) {
        this.gamename = gamename;
    }
    public String getPackagename() {
        return packagename;
    }
    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public String getMothod() {
        return mothod;
    }
    public void setMothod(String mothod) {
        this.mothod = mothod;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public Date getUpdatetime() {
        return updatetime;
    }
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public int getLook() {
        return look;
    }

    public void setLook(int look) {
        this.look = look;
    }

    public int getGood() {
        return good;
    }

    public void setGood(int good) {
        this.good = good;
    }

    @Override
    public int compareTo(PjNote arg0) {
        // TODO Auto-generated method stub
        if(getUpdatetime().getTime() > arg0.getUpdatetime().getTime()){
            return -1;
        }else if(getUpdatetime().getTime() < arg0.getUpdatetime().getTime()){
            return 1;
        }
        return 0;
    }
}
