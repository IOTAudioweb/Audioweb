package com.niocast.entity;

import com.niocast.cast.InterCMDProcess;
import com.niocast.cast.MulticastThread;

import java.util.List;

/**
 * 投放的广播任务信息
 * @author HTT
 *
 */
public class CastTaskInfo {
	//初始化数据
	private int taskid;//广播编号
	private int vol = -1;//广播音量
	private String multiCastType;//广播类型
	private String multicastaddress;//广播地址
	private int multicastport;//广播端口
	private int curCastLevel;//当前广播级别
	private List<String> typestr;//广播信息
	private MulticastThread mct;//当前广播线程
	private List<String> domainidlist;//广播分区列表
	private List<TerminalInfo> castTeridlist;//广播终端列表
	private String taskName;//任务名称；
	//文件、定时广播数据、终端点播数据
	private int tasktype;//广播播放类型 0为顺序，1为循环，2为随机，3为单曲循环
	private int lastingSeconds;//广播设定倒计时（秒）负数为无倒计时
	private List<String> filelist;//广播文件列表
	//寻呼话筒、终端采播、终端点播数据
	private TerminalInfo mainTerm = null;//主终端
	private Boolean isupper;//是否上位机主动采播
	//实时采播数据
	private Boolean isTimer;//是否为定时器广播
	private String userid;//获取开启控件广播的userID
	//广播中数据
	private Boolean isCast = false;//是否正在广播
	private Boolean isStop;//是否暂停广播
	private long time;//获取时间//系统时间，用于前端页面castmusic获取
	/**
	 * 
	 * 文件广播、定时广播初始化
	*/
	public CastTaskInfo(int taskid,List<String> type,  int curCastLevel,List<String> domainidlist, List<TerminalInfo> castTeridlist,int vol,String multicastaddress,int multicastport,int tasktype,int lastingSeconds,List<String> filelist) {
		this.taskid = taskid;
		this.vol = vol;
		this.multiCastType = type.get(0);
		this.multicastaddress = multicastaddress;
		this.multicastport = multicastport;
		this.curCastLevel = curCastLevel;
		this.typestr = type;
		this.domainidlist = domainidlist;
		this.castTeridlist = castTeridlist;
		this.tasktype = tasktype;
		this.lastingSeconds = lastingSeconds;
		this.taskName = type.get(1);
		this.filelist = filelist;
		this.isupper = true;
	}

	/**
	 * 
	 * 终端采播、寻呼话筒初始化
	*/
	public CastTaskInfo(int taskid,List<String> type,int curCastLevel,List<String> domainidlist, List<TerminalInfo> castTeridlist,int vol,String multicastaddress,int multicastport,int lastingSeconds,TerminalInfo mainTerm,boolean isupper) {
		this.taskid = taskid;
		this.vol = vol;
		this.multiCastType = type.get(0);
		this.multicastaddress = multicastaddress;
		this.multicastport = multicastport;
		this.curCastLevel = curCastLevel;
		this.typestr = type;
		this.domainidlist = domainidlist;
		this.castTeridlist = castTeridlist;
		this.lastingSeconds = lastingSeconds;
		this.taskName = type.get(1);
		this.mainTerm = mainTerm;
		this.isupper = isupper; 
	}

	/**
	 * 
	 * 实时采播初始化
	*/
	public CastTaskInfo(int taskid,List<String> type,int curCastLevel,List<String> domainidlist, List<TerminalInfo> castTeridlist,int vol,String multicastaddress,int multicastport,boolean istimer) {
		this.taskid = taskid;
		this.vol = vol;
		this.multiCastType = type.get(0);
		this.multicastaddress = multicastaddress;
		this.multicastport = multicastport;
		this.curCastLevel = curCastLevel;
		this.typestr = type;
		this.domainidlist = domainidlist;
		this.castTeridlist = castTeridlist;
		this.taskName = type.get(1);
		this.isTimer = istimer;
		this.isupper = true;
		if(type.size() > 2) {
			this.userid = type.get(2);
		}
		//System.out.println(multicastaddress+":"+multicastport);
	}
	/**
	 * 
	 * 终端点播初始化
	*/
	public CastTaskInfo(int taskid,List<String> type,int curCastLevel,TerminalInfo mainTerm,List<String> filelist) {
		this.taskid = taskid;
		this.multiCastType = type.get(0);
		this.curCastLevel = curCastLevel;
		this.typestr = type;
		this.taskName = type.get(1);
		this.mainTerm = mainTerm;
		this.filelist = filelist;
		this.isupper = false;
		this.vol = -1;
	}
	public int getTaskid() {
		return taskid;
	}

	public String getMultiCastType() {
		return multiCastType;
	}

	public Boolean getIsCast() {
		return isCast;
	}

	public void setIsCast(Boolean isCast) {
		this.isCast = isCast;
	}

	public int getCurCastLevel() {
		return curCastLevel;
	}

/**
	 * @return the mct
	 */
	public MulticastThread getMct() {
		return mct;
	}

	/**
	 * @param mct the mct to set
	 */
	public void setMct(MulticastThread mct) {
		this.mct = mct;
	}

/**
	 * @return the castTeridlist
	 */
	public List<TerminalInfo> getCastTeridlist() {
		return castTeridlist;
	}

	/**
	 * @param castTeridlist the castTeridlist to set
	 */
	public void setCastTeridlist(List<TerminalInfo> castTeridlist) {
		this.castTeridlist = castTeridlist;
	}

/**
	 * @return the vol
	 */
	public int getVol() {
		return vol;
	}
	/**
	 * @param vol the vol to set复写修改组播vol方法，使其同步
	 */
	public void setVol(int vol) {
		if(vol != this.vol) {
			mct.sendMulticastPackt(InterCMDProcess.sendVolSet(vol,false));
			this.vol = vol;
		}
	}
	/**
	 * @return the filelist
	 */
	public List<String> getFilelist() {
		return filelist;
	}
	/**
	 * @param filelist the filelist to set
	 */
	public void setFilelist(List<String> filelist) {
		this.filelist = filelist;
	}
	/**
	 * @return the domainidlist
	 */
	public List<String> getDomainidlist() {
		return domainidlist;
	}
	/**
	 * @param domainidlist the domainidlist to set
	 */
	public void setDomainidlist(List<String> domainidlist) {
		this.domainidlist = domainidlist;
	}
	/**
	 * @return the isStop
	 */
	public Boolean getIsStop() {
		return isStop;
	}
	/**
	 * @param isStop the isStop to set 复写set方法，调用底层类实现播放与暂停 true 为暂停，false为播放
	 */
	public void setIsStop(Boolean isStop) {
		if(this.isStop != isStop) {
			if(isStop && mct != null) {
				mct.castPosition(0,1);
			}else if(mct != null){
				mct.castPosition(1,1);
			}else {
				return;
			}
			this.isStop = isStop;
		}
	}
	/**
	 * @return the typestr
	 */
	public List<String> getTypestr() {
		return typestr;
	}
	/**
	 * @return the multicastaddress
	 */
	public String getMulticastaddress() {
		return multicastaddress;
	}
	/**
	 * @return the multicastport
	 */
	public int getMulticastport() {
		return multicastport;
	}
	/**
	 * @return the castName
	 */
	public String getTaskName() {
		return taskName;
	}
	/**
	 * @return the tasktype
	 */
	public int getTasktype() {
		return tasktype;
	}

	public int getLastingSeconds() {
		return lastingSeconds;
	}

	public TerminalInfo getMainTerm() {
		return mainTerm;
	}
	
	public Boolean getIsTimer() {
		return isTimer;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CastTaskInfo other = (CastTaskInfo) obj;
		try {
			if(this.taskid == other.taskid) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	public void setTasktype(int tasktype) {
		this.tasktype = tasktype;
	}

	public long getTime() {
		time = System.currentTimeMillis();
		return time;
	}

	public Boolean getIsupper() {
		return isupper;
	}

	public void setIsupper(Boolean isupper) {
		this.isupper = isupper;
	}

	public String getUserid() {
		return userid;
	}
}
