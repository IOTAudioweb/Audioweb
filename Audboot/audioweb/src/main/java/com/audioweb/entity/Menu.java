package com.audioweb.entity;

import java.util.List;

/**
 * 菜单
 */
public class Menu {

    private String mid;        //菜单ID
    private String mname;    //菜单名称
    private String murl;    //链接
    private String parentmid;    //上级菜单ID
    private String mlevel;    //菜单等级
    private String micon;    //图标
    private String mintroduce;//菜单介绍
    private String note;//备注
    private Boolean isopen;//是否公开
    private Boolean isenabled;//是否可用
    private Boolean refresh;//是否按下刷新
    private String target;
    private Menu parentMenu;
    private List<Menu> subMenu;
    private boolean hasMenu = true;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getMname() {
        return mname;
    }

    public void setMname(String mname) {
        this.mname = mname;
    }

    public String getMurl() {
        return murl;
    }

    public void setMurl(String murl) {
        this.murl = murl;
    }

    public String getParentmid() {
        return parentmid;
    }

    public void setParentmid(String parentmid) {
        this.parentmid = parentmid;
    }

    public String getMlevel() {
        return mlevel;
    }

    public void setMlevel(String mlevel) {
        this.mlevel = mlevel;
    }

    public String getMicon() {
        return micon;
    }

    public void setMicon(String micon) {
        this.micon = micon;
    }

    public String getMintroduce() {
        return mintroduce;
    }

    public void setMintroduce(String mintroduce) {
        this.mintroduce = mintroduce;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getIsenabled() {
        return isenabled;
    }

    public void setIsenabled(Boolean isenabled) {
        this.isenabled = isenabled;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Menu getParentMenu() {
        return parentMenu;
    }

    public void setParentMenu(Menu parentMenu) {
        this.parentMenu = parentMenu;
    }

    public List<Menu> getSubMenu() {
        return subMenu;
    }

    public void setSubMenu(List<Menu> subMenu) {
        this.subMenu = subMenu;
    }

    public boolean isHasMenu() {
        return hasMenu;
    }

    public void setHasMenu(boolean hasMenu) {
        this.hasMenu = hasMenu;
    }

    public Boolean getIsopen() {
        return isopen;
    }

    public void setIsopen(Boolean isopen) {
        this.isopen = isopen;
    }

    public Boolean getRefresh() {
        return refresh;
    }

    public void setRefresh(Boolean refresh) {
        this.refresh = refresh;
    }
}
