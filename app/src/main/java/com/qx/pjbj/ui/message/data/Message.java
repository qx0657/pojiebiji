package com.qx.pjbj.ui.message.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Create by QianXiao
 * On 2020/8/24
 */
public class Message implements Serializable,Comparable<Message> {
    private String fromuser = "";
    private Date time;
    private String msg = "";

    public String getFromuser() {
        return fromuser;
    }

    public void setFromuser(String fromuser) {
        this.fromuser = fromuser;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public int compareTo(Message o) {
        if(o.getTime().getTime()>getTime().getTime()){
            return 1;
        }
        if(o.getTime().getTime()<getTime().getTime()){
            return -1;
        }
        return 0;
    }
}
