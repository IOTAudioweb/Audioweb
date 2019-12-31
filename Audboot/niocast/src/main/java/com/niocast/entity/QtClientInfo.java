package com.niocast.entity;

import org.apache.mina.core.session.IoSession;

import java.security.Key;
import java.util.List;

public class QtClientInfo {
	private String userid; //用户编号
	private String loginid;//登录名
	private String password;//登录密码
	private String username; //姓名
	private String roleId;//权限ID
	private String MenuRights;//权限信息，判断有无登录实时采播的权限
	private IoSession session;//会话信息，qt终端连接的会话，30秒自动断连，需心跳包
	private int taskid  = 0;//开启的广播编号
	private Key key;
	private List<String> terinfos;
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
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public String getMenuRights() {
		return MenuRights;
	}
	public void setMenuRights(String menuRights) {
		MenuRights = menuRights;
	}
	public IoSession getSession() {
		return session;
	}
	public void setSession(IoSession session) {
		this.session = session;
	}
	public int getTaskid() {
		return taskid;
	}
	public void setTaskid(int taskid) {
		this.taskid = taskid;
	}
	
	public Key getKey() {
		return key;
	}
	public void setKey(Key key) {
		this.key = key;
	}
	
	public List<String> getTerinfos() {
		return terinfos;
	}
	public void setTerinfos(List<String> terinfos) {
		this.terinfos = terinfos;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QtClientInfo other = (QtClientInfo) obj;
		try {
			if(this.userid == other.userid) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
}
