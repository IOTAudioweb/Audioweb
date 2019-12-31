package com.audioweb.entity;

import java.util.Date;

/**
 * 日志类
 *
 * @ClassName Log
 * @Description TODO
 * @date 2016年11月15日 下午1:42:52
 */
public class Log {
    private String lid;//日志编号
    private String userid;//用户编号
    private String username;//用户名
    private String logtype;//日志类别：登陆日志，操作日志
    private String function;//功能模块
    private String logcontent;//操作
    private String ip;//IP地址
    private Date logtime;//生成时间
    private String remark;//备注

    public String getLid() {
        return lid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLogcontent() {
        return logcontent;
    }

    public void setLogcontent(String logcontent) {
        this.logcontent = logcontent;
    }

    public String getLogtype() {
        return logtype;
    }

    public void setLogtype(String logtype) {
        this.logtype = logtype;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getLogtime() {
        return logtime;
    }

    public void setLogtime(Date logtime) {
        this.logtime = logtime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
