/**
 *
 */
package com.audioweb.entity;

/**
 * @author shuofang
 * 用户
 */
public class Users {
    private String userid; //用户编号
    private String loginid;//登录名
    private String password;//登录密码
    private String username; //姓名
    private Boolean isuse;//是否可用
    private String CreatDate;
    private String LastLoginDate;
    private String LastPasswordChangeDate;
    private String roleId;
    private String note;//备注
    private String RoleName;
    private int RoleLevel;
    private String MenuRights;
    private String DomainId;
    private int realcasttype;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLoginid() {
        return loginid;
    }

    public void setLoginid(String loginid) {
        this.loginid = loginid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getIsuse() {
        return isuse;
    }

    public void setIsuse(Boolean isuse) {
        this.isuse = isuse;
    }

    public String getCreatDate() {
        return CreatDate;
    }

    public void setCreatDate(String creatDate) {
        CreatDate = creatDate;
    }

    public String getLastLoginDate() {
        return LastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        LastLoginDate = lastLoginDate;
    }

    public String getLastPasswordChangeDate() {
        return LastPasswordChangeDate;
    }

    public void setLastPasswordChangeDate(String lastPasswordChangeDate) {
        LastPasswordChangeDate = lastPasswordChangeDate;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getDomainId() {
        return DomainId;
    }

    public void setDomainId(String domainId) {
        DomainId = domainId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRoleName() {
        return RoleName;
    }

    public void setRoleName(String roleName) {
        RoleName = roleName;
    }

    /**
     * @return the roleLevel
     */
    public int getRoleLevel() {
        return RoleLevel;
    }

    /**
     * @param roleLevel the roleLevel to set
     */
    public void setRoleLevel(int roleLevel) {
        RoleLevel = roleLevel;
    }

    public String getMenuRights() {
        return MenuRights;
    }

    public void setMenuRights(String menuRights) {
        MenuRights = menuRights;
    }

    public int getRealcasttype() {
        return realcasttype;
    }

    public void setRealcasttype(int realcasttype) {
        this.realcasttype = realcasttype;
    }

}
