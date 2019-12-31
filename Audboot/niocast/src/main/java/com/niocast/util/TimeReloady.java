package com.niocast.util;

import com.niocast.entity.TerminalInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeReloady implements Runnable{
	private TerminalInfo tInfo = null;
	private List<TerminalInfo> terminalInfos = null;
	public TimeReloady(TerminalInfo tInfo) {
		// TODO Auto-generated constructor stub
		this.tInfo = tInfo;
		if(tInfo.getOrderCastInfo().size() > 0) {
			GlobalInfoController.StartCast(tInfo,tInfo.getOrderCastInfo().get(0));
			GlobalInfoController.getScheduledThreadPool().schedule(this, 1, TimeUnit.SECONDS);
		}
	}
	public TimeReloady(TerminalInfo tInfo, int timeout) {
		// TODO Auto-generated constructor stub
		this.tInfo = tInfo;
		if(tInfo.getOrderCastInfo().size() > 0) {
			GlobalInfoController.StartCast(tInfo,tInfo.getOrderCastInfo().get(0));
			GlobalInfoController.getScheduledThreadPool().schedule(this, timeout, TimeUnit.MILLISECONDS);
		}
	}
	public TimeReloady(List<TerminalInfo> terminalInfos) {
		// TODO Auto-generated constructor stub
		this.terminalInfos = terminalInfos;
		GlobalInfoController.getScheduledThreadPool().schedule(this, 1, TimeUnit.SECONDS);
	}
	public TimeReloady(List<TerminalInfo> terminalInfos, int timeout) {
		// TODO Auto-generated constructor stub
		this.terminalInfos = terminalInfos;
		GlobalInfoController.getScheduledThreadPool().schedule(this, timeout, TimeUnit.MILLISECONDS);
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(tInfo != null)
			GlobalInfoController.tInfoisCastCMDReturn(tInfo);
		else if(terminalInfos != null) {
			GlobalInfoController.isCastCMDReturn(terminalInfos);
		}
	}
	
}
