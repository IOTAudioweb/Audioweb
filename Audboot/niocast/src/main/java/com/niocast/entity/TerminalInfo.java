package com.niocast.entity;

import com.audioweb.entity.Terminals;
import org.apache.mina.core.session.IoSession;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 终端广播信息
 * @author HTT,shuofang
 */
public class TerminalInfo {
	private String terid;//终端编号
	private String domainId;//终端分组
	private InetAddress ipAddress;//终端IP地址
	private String castType;//广播信息
	private Boolean isOnline = false;//是否登录并在线
	private Boolean istrueOnline = false;//是否登录并在线
	private Boolean iscast = false;//是否正在广播
	private Boolean isPaging = false;//是否为寻呼话筒
	private Boolean isAutoCast = false;//是否为自动采播
	private int castid;//广播任务编号
	private Boolean isCastCMDReturn = true;//发送的广播命令终端是否回复，注：发送广播命令前需将终端的此信息置为否
	//private Terminals terminals;
	private List<CastTaskInfo> orderCastInfo = null;//顺序广播信息
	private Date lastUseTime ;//终端最后访问的时间
	private IoSession session = null;//终端连接信息
	private String tname;//终端名称
	private String domainName;//终端分区名称
	private int retry = 0;//重新发送次数
	
	public TerminalInfo(Boolean isOnline,Terminals terminals,IoSession session) {
		lastUseTime = new Date();
		try {
			this.ipAddress  = InetAddress.getByName(terminals.getTIP());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.terid = terminals.getTIDString();
		istrueOnline = this.isOnline;
		this.isOnline = isOnline;
		this.domainId = terminals.getDomainId();
		this.isPaging = terminals.getISCMIC();
		this.isAutoCast = terminals.getISAutoCast();
		this.tname = terminals.getTName();
		this.domainName = terminals.getDomainName();
		this.iscast = false;
		orderCastInfo = new LinkedList<>();
		if(session != null)
			this.session = session;
	}
	public TerminalInfo(String terid,String ipAddress) {
		this.terid = terid;
		try {
			this.ipAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getTerid() {
		return terid;
	}
	public void setTerid(String terid) {
		this.terid = terid;
	}
	public InetAddress getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((terid == null) ? 0 : terid.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TerminalInfo other = (TerminalInfo) obj;
		if (terid == null) {
			if (other.terid != null)
				return false;
		} else if (!terid.equals(other.terid))
			return false;
		return true;
	}
	public Boolean getIsOnline() {
		return isOnline;
	}
	public void setIsOnline(Boolean isOnline) {
		this.isOnline = isOnline;
	}
	public Boolean getIsCastCMDReturn() {
		return isCastCMDReturn;
	}
	public void setIsCastCMDReturn(Boolean isCastCMDReturn) {
		this.isCastCMDReturn = isCastCMDReturn;
	}
	/**
	 * @return the istrueOnline
	 */
	public Boolean getIstrueOnline() {
		return istrueOnline;
	}
	/**
	 * @param istrueOnline the istrueOnline to set
	 */
	public void setIstrueOnline(Boolean istrueOnline) {
		this.istrueOnline = istrueOnline;
	}
	/**
	 * @return the domainId
	 */
	public String getDomainId() {
		return domainId;
	}
	/**
	 * @param domainId the domainId to set
	 */
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	/**
	 * @return the iscast
	 */
	public Boolean getIscast() {
		return iscast;
	}
	/**
	 * @param iscast the iscast to set
	 */
	public void setIscast(Boolean iscast) {
		this.iscast = iscast;
	}
	/**
	 * @return the castType
	 */
	public String getCastType() {
		return castType;
	}
	/**
	 * @param castType the castType to set
	 */
	public void setCastType(String castType) {
		this.castType = castType;
	}
	/**
	 * @return the castid
	 */
	public int getCastid() {
		return castid;
	}
	/**
	 * @param castid the castid to set
	 */
	public void setCastid(int castid) {
		this.castid = castid;
	}
	/**
	 * @return the orderCastInfo
	 */
	public List<CastTaskInfo> getOrderCastInfo() {
		return orderCastInfo;
	}
	/**
	 * @return the session
	 */
	public IoSession getSession() {
		return session;
	}
	/**
	 * @param session the session to set
	 */
	public void setSession(IoSession session) {
		this.session = session;
	}
	/**
	 * @return the lastUseTime
	 */
	public Date getLastUseTime() {
		return lastUseTime;
	}
	/**
	 * @param lastUseTime the lastUseTime to set
	 */
	public void setLastUseTime(Date lastUseTime) {
		this.lastUseTime = lastUseTime;
	}
	public Boolean getIsPaging() {
		return isPaging;
	}
	public void setIsPaging(Boolean isPaging) {
		this.isPaging = isPaging;
	}
	public Boolean getIsAutoCast() {
		return isAutoCast;
	}
	public void setIsAutoCast(Boolean isAutoCast) {
		this.isAutoCast = isAutoCast;
	}
	public String getTname() {
		return tname;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public void setTname(String tname) {
		this.tname = tname;
	}
	public int getRetry() {
		return retry;
	}
	public void setRetry(int retry) {
		this.retry = retry;
	}
}
