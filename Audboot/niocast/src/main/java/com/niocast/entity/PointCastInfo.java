package com.niocast.entity;

import org.apache.mina.core.session.IoSession;

import java.io.BufferedInputStream;

public class PointCastInfo {
	private int taskid;//广播任务编号
	private TerminalInfo terminalInfo;//点播终端信息
	private CastTaskInfo castTaskInfo;//点播任务信息
	private int bitsize;//分包大小
	private int timesize;//时间间隔
	private BufferedInputStream in;//文件读取信息流
	private Boolean isStop;//是否暂停广播 true 为暂停
	private IoSession session;//本点播连接session
	public PointCastInfo() {
		// TODO Auto-generated constructor stub
	}
	/**
	 *  初始化点播任务信息
	 * @param taskid
	 * @param terminalInfo
	 * @param castTaskInfo
	 * @param bitsize
	 * @param timesize
	 * @param in
	 * @param isStop
	 * @param session
	 */
	public PointCastInfo(int taskid, TerminalInfo terminalInfo, CastTaskInfo castTaskInfo, int bitsize, int timesize, BufferedInputStream in, Boolean isStop, IoSession session) {
		this.taskid = taskid;
		this.terminalInfo = terminalInfo;
		this.castTaskInfo = castTaskInfo;
		this.bitsize = bitsize;
		this.timesize = timesize;
		this.in = in;
		this.isStop = isStop;
		this.session = session;
	}
	public int getTaskid() {
		return taskid;
	}
	public void setTaskid(int taskid) {
		this.taskid = taskid;
	}
	public TerminalInfo getTerminalInfo() {
		return terminalInfo;
	}
	public void setTerminalInfo(TerminalInfo terminalInfo) {
		this.terminalInfo = terminalInfo;
	}
	public CastTaskInfo getCastTaskInfo() {
		return castTaskInfo;
	}
	public void setCastTaskInfo(CastTaskInfo castTaskInfo) {
		this.castTaskInfo = castTaskInfo;
	}
	public int getBitsize() {
		return bitsize;
	}
	public void setBitsize(int bitsize) {
		this.bitsize = bitsize;
	}
	public int getTimesize() {
		return timesize;
	}
	public void setTimesize(int timesize) {
		this.timesize = timesize;
	}
	public BufferedInputStream getIn() {
		return in;
	}
	public void setIn(BufferedInputStream in) {
		this.in = in;
	}
	public Boolean getIsStop() {
		return isStop;
	}
	public void setIsStop(Boolean isStop) {
		this.isStop = isStop;
		castTaskInfo.setIsStop(isStop);
	}
	
	public IoSession getSession() {
		return session;
	}
	public void setSession(IoSession session) {
		this.session = session;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PointCastInfo other = (PointCastInfo) obj;
		if (terminalInfo == null) {
			if (other.terminalInfo != null)
				return false;
		} else if (!terminalInfo.equals(other.terminalInfo))
			return false;
		else if (taskid != other.taskid)
			return false;
		return true;
	}
}
