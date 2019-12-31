package com.niocast.util;

import com.audioweb.entity.Page;
import com.audioweb.entity.Terminals;
import com.audioweb.service.impl.TerminalsService;
import com.audioweb.util.BaseStaticLogger;
import com.audioweb.util.Const;
import com.audioweb.util.SpringContextUtils;
import com.audioweb.util.Tools;
import com.niocast.cast.InterCMDProcess;
import com.niocast.cast.MulticastThread;
import com.niocast.cast.SendPointData;
import com.niocast.entity.*;
import com.niocast.minatcpservice.TCPMinaCastThread;
import com.niocast.minatcpservice.handle.DefaultCommand;
import com.niocast.minathread.MinaCastHandler;
import com.niocast.minathread.MinaCastThread;
import com.niocast.websocket.WebStreamHandler;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Shuofang
 *	TODO 终端信息与通信全局管理
 */
public class GlobalInfoController extends BaseStaticLogger{
	public static final int TERMPORT = Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "terRecPort"));//终端接收端口
	public static String SERVERIP;//服务器IP
	public static String GATEWAY;//网关
	public static String NETMASK;//子网掩码
	private static Map<String, TerminalInfo> terinfoMap = new ConcurrentHashMap<>();//终端登录状态等信息
	private static Map<Integer,CastTaskInfo> castTaskMap = new ConcurrentHashMap<>();//广播任务储存列表
	private static List<MonitorInfoBean> mInfoBeans = new LinkedList<MonitorInfoBean>();//系统状态链表存储
	private static Map<Integer, SendPointData> pointTaskMap =  new ConcurrentHashMap<>();//点播信息存储表，ID为时间间隔，单位ms
	private static Map<String, QtClientInfo> clinetmap = new ConcurrentHashMap<>();//qt控件终端登录状态等信息
	
	private static ExecutorService executorService =  null;//终端IO交互处理线程池
	private static ExecutorService groupexecutorService =  null;//组播线程线程池
	private static ExecutorService standbyexecutorService =  null;//分发数据或者给任务待机的线程池
	private static ScheduledExecutorService scheduledThreadPool = null;//定时执行线程池
	
	
	private static String multicastAddress="224.0.1.1";//组播地址
	private static Integer multicastPort=49153;//组播端口号
	private static Integer taskId=1;//广播ID号
	//private static int temp=2;//test 
	private static MinaCastThread castThread;//心跳检测线程
	private static TCPMinaCastThread tcpThread;//终端通信线程
	private static TerminalsService terminalsService = (TerminalsService) SpringContextUtils
			.getBeanByClass(TerminalsService.class);
	//private static Logger logger = LoggerFactory.getLogger(GlobalInfoController.class);
	private static Long Date;//服务器启动时间
	
	/**
	 * 将线程加入IO线程池运行
	 * @param thread
	 */
	public static void putThreadintoPool(Runnable thread){
		if(executorService!=null)
		executorService.execute(thread);
	}
	/**
	 * 将线程加入组播线程池运行
	 * @param thread
	 */
	public static void putThreadintoGroupPool(Runnable thread){
		if(groupexecutorService!=null)
			groupexecutorService.execute(thread);
	}
	/**
	 * 将线程加入数据处理线程池运行
	 * @param thread
	 */
	public static void putThreadintoStandbyPool(Runnable thread){
		if(standbyexecutorService!=null)
			standbyexecutorService.execute(thread);
	}
	/**
	 * 终端登录
	 * @param terid
	 * @param ipAddress
	 * @throws Exception 
	 */
	public static String setTerminalLogin(String terid,InetSocketAddress socketAddress,IoSession session) throws Exception{
		String returnData = null;
		TerminalInfo tInfo = terinfoMap.get(terid);
		if(null != tInfo) {
			if(tInfo.getIpAddress().equals(socketAddress.getAddress())) {
				if(new Date().getTime()-tInfo.getLastUseTime().getTime() > 3000f || new Date().getTime()-tInfo.getLastUseTime().getTime() < 0) {//对2次收包时间进行判断
						tInfo.setIsOnline(true);//重置登录状态
						tInfo.setIstrueOnline(false);//重置登录状态
						//tInfo.setIscast(false);//添加并未广播信息,好重新入组
						//tInfo.setSession(session);//将连接信息存入信息表中
					if(tInfo.getOrderCastInfo().size() > 0) {//判断是否有广播任务
						returnData = "cast";
					}else {
						returnData = "true";
					}
				}else {
					returnData = "fast";
				}
			}else {
				returnData = "error";
			}
		}else {
			//没有此终端信息或者IP不同
			try {
				Terminals terminals = terminalsService.getTermByTid(terid);
				if(null != terminals) {
					if(InetAddress.getByName(terminals.getTIP()).equals(socketAddress.getAddress())) {
						/*synchronized (terinfolist) {*/
							terinfoMap.put(terid, new TerminalInfo(true,terminals,session));
						/*}*/
						returnData = "true";
						return returnData;
					//终端的IP与配置不相同
					}else{
						returnData = "error";
					}
				//并未有此终端	
				}else {
					returnData = "nothing";
				}
			} catch (Exception e) {
				// TODO: handle exception
				returnData = "error";
			}
		}
		return returnData;
	}
	/**
	 * 终端心跳检测显示在线
	 * @param terid
	 * @param ipAddress
	 * @throws Exception 
	 */
	public static String setTerminalOnline(String terid,InetSocketAddress socketAddress,Boolean isOnline,IoSession session) throws Exception{
		String returnData = null;
		TerminalInfo tInfo = terinfoMap.get(terid);
		if(null != tInfo) {
			if(tInfo.getIpAddress().equals(socketAddress.getAddress())) {
				if(isOnline && (new Date().getTime()-tInfo.getLastUseTime().getTime() > 3000f || new Date().getTime()-tInfo.getLastUseTime().getTime() < 0)) {//对2次收包时间进行判断
					//双重验证保证终端是否掉线
					synchronized (tInfo) {
						tInfo.setIstrueOnline(isOnline);
						tInfo.setIsOnline(isOnline);
						if(tInfo.getOrderCastInfo().size() == 0 || tInfo.getOrderCastInfo().get(0).getMainTerm() == null || !tInfo.getOrderCastInfo().get(0).getMainTerm().equals(tInfo))
							tInfo.setIscast(false);//添加并未广播信息，好重新入组,除非为终端发起的终端采播
						tInfo.setLastUseTime(new Date());//更新最后访问时间
						if(!session.equals(tInfo.getSession())) {
							tInfo.setSession(session);
						}
					}
					if(tInfo.getOrderCastInfo().size() > 0 && !tInfo.getIsPaging() && !(tInfo.getIsAutoCast() && Const.CASTTYPE[3].equals(tInfo.getCastType())
							&& tInfo.getOrderCastInfo().get(0).getMultiCastType().equals(Const.CASTTYPE[3])
							&& tInfo.getOrderCastInfo().get(0).getMainTerm().equals(tInfo) && tInfo.getRetry() == 0)
							) {//判断是否有广播任务并入组,终端采播源终端除外
						if(GlobalInfoController.getCastTaskInfo(tInfo.getOrderCastInfo().get(0).getTaskid()) != null) {
							StartCast(tInfo,tInfo.getOrderCastInfo().get(0));
							returnData = "cast";
						}else {
							GlobalInfoController.DelTheTasks(tInfo, tInfo.getOrderCastInfo().get(0));
							returnData = "error";
						}
					}else
						returnData = "true";
					return returnData;
				}else if(!isOnline){//终端未登录却主动操作
					tInfo.setIscast(false);//添加并未广播信息，好重新入组
					if(!session.equals(tInfo.getSession())) {
						tInfo.setSession(session);
					}
					returnData = "true";
					return returnData;
				}else{
					returnData = "fast";
					return returnData;
				}
			}else {
				returnData = "error";
			}
		}else {
			//信息列表不存在此终端
			//终端的IP与配置的IP相同
			try {
				Terminals terminals = terminalsService.getTermByTid(terid);
				if(null != terminals) {
					if(InetAddress.getByName(terminals.getTIP()).equals(socketAddress.getAddress())) {
						/*synchronized (terinfolist) {*/
							terinfoMap.put(terid, new TerminalInfo(true,terminals,session));
						/*}*/
						returnData = "true";
						return returnData;
					//终端的IP与配置不相同
					}else{
						returnData = "error";
					}
				//并未有此终端	
				}else {
					returnData = "nothing";
				}
			} catch (Exception e) {
				// TODO: handle exception
				returnData = "error";
			}
		}
		return returnData;
	}
	/**
	 * 终端的广播命令是否已回复
	 * @param terid
	 * @return
	 */
	/*public static Boolean isTerCastCMDReturn(String ipaddress){
		for(int i=0;i<terinfolist.size();i++){
			if(terinfolist.get(i).getIpAddress().equals(ipaddress)){
				return terinfolist.get(i).getIsCastCMDReturn();
			}
		}
		return false;
	}*/
	/**
	 * 终端列表全局信息中是否包含终端,并返回遍历序号
	 * @param terid
	 * @return
	 */
	/*public static Integer hasTermianlInfo(String terid,String ipAddress){
		for(int i =0;i<terinfolist.size();i++) {
			if(terinfolist.get(i).getTerid().equals(terid)&&terinfolist.get(i).getIpAddress().equals(ipAddress)) {
				return i;
			}
		}
		return -1;
	}*/
	
	/**
	 * 更新获取所有终端本地信息至内存
	 * 
	 */
	public static void falshSqlTerminals(){
		Page page = new Page();
		try {
			terinfoMap.clear();
			List<Terminals> tList = terminalsService.listAllTerm(page);
			for(Terminals terminals:tList) {
				terinfoMap.put(terminals.getTIDString(),new TerminalInfo(false, terminals,null));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 重置终端列表的所有终端命令是否回复的状态，置为否
	 * @param teridlist
	 */
	/*public static void resetCastTerCMD(List<String> teridlist){
		synchronized (terinfolist) {
			for(String terid:teridlist){
				int curindex = terinfolist.indexOf(new TerminalInfo(terid));
				terinfolist.get(curindex).setIsCastCMDReturn(false);;
			}
		}
	}*/
	/**
	 * 获取组播地址 TODO 怎么不重复并重置地址和端口号
	 * @return
	 */
	public static String getMulticastAddress() {
		synchronized (multicastAddress) {
			String[] ips = multicastAddress.split("\\.");
			if(ips.length>3){
				int ip4 = Integer.parseInt(ips[3]);
				int ip3 = Integer.parseInt(ips[2]);
				if(ip4<255){
					ip4++;
				}else if(ip3 < 255){
					ip3++;
					ip4=1;
				}else {
					ip3 = 1;
					ip4 = 1;
				}
				multicastAddress = ips[0]+"."+ips[1]+"."+ip3+"."+ip4;
				return multicastAddress;
			}else{
				multicastAddress = "224.0.1.1";//组播地址
				return multicastAddress;
			}
		}
	}
	public static int getMulticastPort() {
		//获取组播端口
		synchronized (multicastPort){
			if(multicastPort > 65535) {
				multicastPort = 49153;
			}
			 return multicastPort++;
		}
	}
	
	/**
	 * @return the castThread
	 */
	public static MinaCastThread getCastThread() {
		return castThread;
	}

	/**
	 * @param castThread the castThread to set
	 */
	public static void setCastThread(MinaCastThread castThread) {
		GlobalInfoController.castThread = castThread;
	}

	/**
	 * 获取广播任务列表信息
	 * @return
	 */
	public static List<CastTaskInfo> getCastTasklistInfos() {
		return new ArrayList<>(castTaskMap.values());
	}
	/**
	 * 获取指定广播任务信息
	 * @return
	 */
	public static CastTaskInfo getCastTaskInfo(int taskId) {
		return castTaskMap.get(taskId);
	}
	/**
	 * 获取指定终端信息
	 * @return
	 */
	public static TerminalInfo getTerminalInfo(String TIDString) {
		return terinfoMap.get(TIDString);
	}
	/**
	 * 删除指定终端信息
	 * @return
	 */
	public static void deleteTerminalInfo(String TIDString) {
		TerminalInfo tInfo = terinfoMap.get(TIDString);
		if(tInfo != null && tInfo.getOrderCastInfo().size()>0 && tInfo.getIscast()) {
			EndCast(tInfo/*,tInfo.getOrderCastInfo().get(0)*/);
		}
		terinfoMap.remove(TIDString);
	}
	/**
	 * 批量删除指定终端信息
	 * @return
	 */
	public static void deleteTerminalInfo(String[] TIDStrings) {
		for(int i = 0;i<TIDStrings.length;i++) {
			TerminalInfo tInfo = terinfoMap.get(TIDStrings[i]);
			if(tInfo != null && tInfo.getOrderCastInfo().size()>0 && tInfo.getIscast()) {
				EndCast(tInfo/*,tInfo.getOrderCastInfo().get(0)*/);
			}
			terinfoMap.remove(TIDStrings[i]);
		}
	}
	/**
	 * @param TIDStrings
	 * @param vol
	 * @param is
	 * 批量设置终端音量
	 * @return
	 */
	public static void setTerminalInfoVol(String[] TIDStrings,int vol,boolean is) {
		for(int i = 0;i<TIDStrings.length;i++) {
			TerminalInfo tInfo = terinfoMap.get(TIDStrings[i]);
			if(tInfo != null && (tInfo.getIsOnline()||tInfo.getIstrueOnline())) {
				SendData(InterCMDProcess.sendVolSet(vol, is), tInfo.getSession(), 0);
			}
		}
	}
	/**
	 * @param TIDStrings
	 * @param domainId
	 * @param domainname
	 * 批量更新终端分区信息
	 * @return
	 */
	public static void updataTerminalInfo(String[] TIDStrings,String domainId,String domainname) {
		for(int i = 0;i<TIDStrings.length;i++) {
			TerminalInfo tInfo = terinfoMap.get(TIDStrings[i]);
			if(tInfo !=null)
			synchronized (tInfo) {
				tInfo.setDomainName(domainname);
				tInfo.setDomainId(domainId);
			}
		}
	}
	/**
	 * 向广播任务列表添加广播任务
	 * @return
	 */
	public static void addCastTaskTolist(CastTaskInfo taskinfo) {
		castTaskMap.put(taskinfo.getTaskid(), taskinfo);
	}
	
	public static Integer getTaskId() {
		synchronized (taskId) {
			return taskId++;
		}
	}
	/**
	 * 
	 * @param tiInfo
	 * @param taskinfo
	 * TODO 添加备用任务入终端信息
	 * 时间：2019年1月2日
	 */
	public static void AddAltTasks(List<TerminalInfo> tInfos,CastTaskInfo taskinfo) {
		EndTasksController(tInfos,taskinfo,true);//停止对应终端现在的任务
		//Thread.sleep(10);//停止10ms等待终端确定停止
		StartTasks(tInfos,taskinfo,true);//开始组播命令批量发送
		new TimeReloady(tInfos);//定时检测终端入组情况
	}
	/**
	 * 
	 * @param tiInfo
	 * @param taskinfo
	 * @param type true为加任务，false为去除任务
	 * TODO 根据优先级确定停止广播所选分区的终端并加上
	 * 时间：2019年1月2日
	 */
	public static void EndTasksController(List<TerminalInfo> terminalInfos, CastTaskInfo taskinfo, boolean type) {
		if(type) {
			for (TerminalInfo ter:terminalInfos) {
				try {
					List<CastTaskInfo> castTaskInfos = ter.getOrderCastInfo();
					synchronized (castTaskInfos) {
						if(castTaskInfos.size() > 0 ) {
							for(int i = 0;i<castTaskInfos.size();i++) {//与组中的任务进行优先级以及时间先后的判定
								if(castTaskInfos.get(i).getCurCastLevel() >= taskinfo.getCurCastLevel()) {
									if(i == 0) {//如果为零，则优先播放
										EndCast(ter/*,ter.getOrderCastInfo().get(0)*/);//发送停止广播命令
										castTaskInfos.add(i, taskinfo);
										//new TimeReloady(tInfo);
									}else {
										castTaskInfos.add(i, taskinfo);
									}
									break;
								}
							}
						}
						castTaskInfos.add(taskinfo);
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}else {
			for (TerminalInfo tInfo:terminalInfos) {
				try {
					List<CastTaskInfo> castTaskInfos = tInfo.getOrderCastInfo();
					if(castTaskInfos.size() == 1) {
						EndCast(tInfo/*,taskinfo*/);
						castTaskInfos.remove(0);
						RefreshTerData(tInfo);
					}else if(castTaskInfos.size() > 0) {
						synchronized (castTaskInfos) {
							for(int i = 0;i<castTaskInfos.size();i++) {
								if(castTaskInfos.get(i).equals(taskinfo)) {
									castTaskInfos.remove(i);
									break;
								}
							}
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * 
	 * @param terminalInfos
	 * @param taskinfo
	 * @param type true 加任务 false 结束后入组任务
	 * TODO 开启分组终端广播
	 * 时间：2019年1月2日
	 */
	public static void StartTasks(List<TerminalInfo> terminalInfos, CastTaskInfo taskinfo, boolean type) {
		if(type) {
			for (TerminalInfo ter:terminalInfos) {
				StartCast(ter,taskinfo);
			}
		}else {
			for (TerminalInfo ter:terminalInfos) {
				if(ter.getOrderCastInfo().size() > 0)
					StartCast(ter,ter.getOrderCastInfo().get(0));
			}
		}
	}
	/**
	 * 
	 * @param tInfo
	 * TODO 从单一终端删除指定任务信息
	 * 时间：2019年1月2日
	 */
	public static void DelTheTasks(TerminalInfo tInfo,CastTaskInfo castTaskInfo) {
		List<CastTaskInfo> castTaskInfos = tInfo.getOrderCastInfo();
		if(castTaskInfos.size() == 1) {
			EndCast(tInfo/*,castTaskInfo*/);
			tInfo.getOrderCastInfo().remove(0);
			RefreshTerData(tInfo);
		}else if(castTaskInfos.size() > 0) {
			int i = 0;
			synchronized (tInfo) {
				for(;i<castTaskInfos.size();i++) {
					if(castTaskInfos.get(i).equals(castTaskInfo)) {
						//EndCast(tInfo,castTaskInfo);
						tInfo.getOrderCastInfo().remove(i);
						break;
					}
				}
			}
			if(i == 0) {
				//Thread.sleep(5);//等待终端确定接收停止命令后
				StartCast(tInfo,castTaskInfos.get(0));//第一任务删除后第二任务自动开始广播
				new TimeReloady(tInfo);
			}
			RefreshTerData(tInfo);
		}
	}
	/**
	 * 
	 * @param tInfo
	 * TODO 终端与所给任务进行对比（若为第一任务）并开始广播 
	 * 时间：2019年1月2日
	 */
	public static void StartCast(TerminalInfo tInfo,CastTaskInfo castTaskInfo) {
		if(tInfo.getOrderCastInfo().size() >0) {
			CastTaskInfo cInfo = tInfo.getOrderCastInfo().get(0);
			if((tInfo.getIsOnline()||tInfo.getIstrueOnline()) && cInfo.equals(castTaskInfo)) {
				if(cInfo.getIsCast()) {
				sendFileCastCMD(tInfo,
					tInfo.getIpAddress(),
					castTaskInfo);
				}else {
					DelTheTasks(tInfo,cInfo);
				}
			}
		}
		RefreshTerData(tInfo);
	}
	/**
	 * 
	 * @param tInfo
	 * @param cInfo
	 * TODO 指定终端停止指定广播
	 * 时间：2019年1月2日
	 */
	public static void EndCast(TerminalInfo tInfo) {
		if(tInfo.getIsOnline()||tInfo.getIstrueOnline()) {
			//List<String> sList = new ArrayList<>();
			//sList.add();
			//sList.add("0");
			if(tInfo.getOrderCastInfo().size() >  0) {
				byte[] endbs =InterCMDProcess.sendCast(false,"", 0,0,tInfo.getOrderCastInfo().get(0).getMultiCastType());
				SendData(endbs, tInfo.getSession(),0);
				//logger.info(tInfo.getTname()+"停止广播");
				try {
					if(Const.CASTTYPE[4].equals(tInfo.getCastType())) {//如果为点播则直接停止点播
						tInfo.getOrderCastInfo().get(0).getMct().close();
					}else if(Const.CASTTYPE[3].equals(tInfo.getCastType()) && tInfo.getOrderCastInfo().get(0).getIsupper() && tInfo.equals(tInfo.getOrderCastInfo().get(0).getMainTerm())) {
						tInfo.getOrderCastInfo().get(0).getMct().close();//如果为被动终端采播则直接停止采播
					}
				} catch (Exception e) {
					// TODO: handle exception
					logError(e);
				}
			}
			//castThread.sendCMDInfo(new InetSocketAddress(tInfo.getIpAddress(), Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "terRecPort"))), endbs);// 发送结束组播命令
		}
	}
	/**
	 * 发送入组广播命令
	 * 
	 * @param netheartsrt
	 *            心跳检测与命令发送线程
	 * @param multicastaddress
	 *            文件广播组播地址
	 * @param multicastrecport
	 *            组播接收端口
	 * @param targetIP
	 *            广播命令单播接收终端地址
	 * @param vol
	 *            文件广播音量
	 * @throws InterruptedException
	 */
	public static void sendFileCastCMD(TerminalInfo tInfo,InetAddress targetIP,CastTaskInfo castTaskInfo) {
		if(castTaskInfo.getMainTerm() == null || !castTaskInfo.getMainTerm().equals(tInfo)) {//是否为源终端
			byte[] senddata = InterCMDProcess.sendCast(true, castTaskInfo.getMulticastaddress(), castTaskInfo.getMulticastport(),castTaskInfo.getVol(),castTaskInfo.getMultiCastType());
			if(senddata != null && tInfo.getSession() != null) {
				/*if(castTaskInfo.getVol()>0)
					sendCastCMD(tInfo, targetIP, castTaskInfo.getVol(), senddata,castTaskInfo.getMultiCastType(),true);
				else*/
				synchronized (tInfo) {
					tInfo.setIsCastCMDReturn(false);
					tInfo.setRetry(tInfo.getRetry()+1);
				}
				sendCastCMD(tInfo, targetIP, senddata,castTaskInfo.getMultiCastType()/*,false*/);
			} else {
				logger.error("发送终端入组命令出错");
			}
		}else {
			byte[] senddata ;
			if(!Const.CASTTYPE[3].equals(castTaskInfo.getMultiCastType())) {
				senddata = InterCMDProcess.sendMainTermCast(true, castTaskInfo.getMulticastaddress(), castTaskInfo.getMulticastport(),castTaskInfo.getMultiCastType());
			}else{
				senddata = InterCMDProcess.sendMainTermCast(true, castTaskInfo.getTypestr().get(2), Integer.parseInt(castTaskInfo.getTypestr().get(3)),castTaskInfo.getMultiCastType());
			}
			if(senddata != null && tInfo.getSession() != null) {
				sendCastCMD(tInfo, targetIP, senddata,castTaskInfo.getMultiCastType());
			} else {
				logger.error("发送源终端启用命令出错");
			}
		}
	}
	/**
	 * 
	 * @param netheartsrt
	 * @param targetIP
	 * @param vol
	 * @param senddata
	 * @param type
	 * TODO	发送入组广播命令
	 * 时间：2019年1月2日
	 */
	private static void sendCastCMD(TerminalInfo tInfo, InetAddress targetIP,
			byte[] senddata,String type/*,boolean isvol*/) {
		logger.debug("第"+tInfo.getRetry()+"次发送"+type+"命令 -> "+targetIP);
		SendData(senddata, tInfo.getSession(),0);
	}
	/**
	 * 
	 * @param tInfo
	 * TODO 刷新终端任务信息
	 * 时间：2019年1月2日
	 */
	public static void RefreshTerData(TerminalInfo tInfo) {
		synchronized (tInfo) {
			if(tInfo.getOrderCastInfo().size() > 0) {
				tInfo.setCastType(tInfo.getOrderCastInfo().get(0).getMultiCastType());
				tInfo.setCastid(tInfo.getOrderCastInfo().get(0).getTaskid());
				tInfo.setIscast(tInfo.getOrderCastInfo().get(0).getIsCast());
			}else {
				tInfo.setCastType("");
				tInfo.setCastid(-1);
				tInfo.setIscast(false);
			}
		}
	}
	
	/**
	 * 根据分组编号和新任务优先级查询广播任务列表判断新任务是否与正在广播任务出现分组冲突，
	 * 若该分组存在旧任务，则比较任务的优先级，若新任务优先级数值小，优先级高，则新任务可以广播，否则不能
	 * 若分组没有就任务，则可以广播
	 * @param domid 分组编号
	 * @param curlevel 新任务优先级值
	 * @return true 新任务可以广播，false：新任务不可以广播 弃用
	 */
	/*public static Boolean getEnableCastByDomid(List<TerminalInfo> teridlist,List<TerminalInfo> startteridlist,int curlevel){
		List<TerminalInfo> tList = new ArrayList<TerminalInfo>();
		tList.addAll(teridlist);
		startteridlist.addAll(teridlist);
		if(castTasklist.size() != 0)
		for(int i=castTasklist.size()-1;i>=0;i--){
			if(tList.removeAll(castTasklist.get(i).getCastTeridlist())) {
				if(curlevel<=castTasklist.get(i).getCurCastLevel()){//优先级数值越小，优先级越高
					//新任务优先级高，则停止该任务
					List<Terminals> ters = new ArrayList<Terminals>();
					ters.addAll(teridlist);
					ters.removeAll(tList);
					for(Terminals ter:ters) {
						List<String> sList = new ArrayList<>();
						for(int j =0;j < CastTaskInfo.types.length;j++) {
							if(castTasklist.get(i).getMultiCastType().equals(CastTaskInfo.types[j])) {
								sList.add(String.valueOf(j));
								break;
							}
						}
						sList.add("0");
						byte[] endbs =InterCMDProcess.sendCast(false,"", 0,0,sList);
						netheartsrt.sendCMDInfo(new InetSocketAddress(ter.getTIP(), Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "terRecPort"))), endbs);// 发送结束组播命令
					}
					castTasklist.get(i).getCastTeridlist().removeAll(ters);
					if(castTasklist.get(i).getCastTeridlist().size()<1) {
						if(castTasklist.get(i).getMct() != null)
							castTasklist.get(i).getMct().close();
					}
					//domaininfolist.get(j).getMct().close();
				}else if(tList != null && tList.size() > 0){
					teridlist =tList;
				}else {
					return false;
				}
			}
		}
		return true;
	}*/
	/**
	 * 向广播任务列表中结束广播任务
	 * @param castTaskid 需要结束的广播任务编号
	 * @return castTaskType
	 * @throws InterruptedException 
	 */
	public static MulticastThread stopCastTaskInList(int castTaskid){
		MulticastThread multicastThread = null;
		if(castTaskMap.containsKey(castTaskid)) {
			CastTaskInfo cti = castTaskMap.get(castTaskid);
			if(cti != null) {
				multicastThread = cti.getMct();
				if(cti.getMct()!= null && cti.getMct().getIsopen()) {
					if(!cti.getMct().getIsture()) {//防止线程未开启就结束导致报错
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					cti.getMct().close();
				}
			}
			castTaskMap.remove(castTaskid);
		}
		/*synchronized (castTasklist) {*/
			/*for(CastTaskInfo cti:castTasklist){
				if(cti.getTaskid()==castTaskid){
					castTaskType = cti.getMultiCastType();
					if(cti.getMct()!= null && cti.getMct().getIsopen()) {
						if(!cti.getMct().getIsture()) {//防止线程未开启就结束导致报错
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						cti.getMct().close();
					}
					castTasklist.remove(cti);
					break;
				}
			}*/
		/*}*/
		return multicastThread;
	}
	/**
	 * 向广播任务列表中结束广播任务
	 * @param castTaskid 需要结束的广播任务编号
	 * @return castTaskType
	 * @throws InterruptedException 
	 */
	public static void stopAllTask(){
		synchronized (castTaskMap) {
			try {
				WebStreamHandler.stopAllSocket();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for(Map.Entry<Integer,CastTaskInfo> cti:castTaskMap.entrySet()) {
				if(cti.getValue() != null) {
					putThreadintoGroupPool(new Runnable() {
						@Override
						public void run() {
							if(Const.CASTTYPE[6].equals(cti.getValue().getMultiCastType())) {
								String userid = cti.getValue().getUserid();
								if(userid != null) {
									QtClientInfo qinfo = GlobalInfoController.getClientInfo(userid);
									if(qinfo != null) {
										try {
											GlobalInfoController.SendData(DefaultCommand.StopCast.getBytes(DefaultCommand.UTF_8), qinfo.getSession(), 0);
										} catch (UnsupportedEncodingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							}
							// TODO Auto-generated method stub
							if(cti.getValue().getMct()!= null && cti.getValue().getMct().getIsopen()) {
								if(!cti.getValue().getMct().getIsture()) {//防止线程未开启就结束导致报错
									try {
										Thread.sleep(200);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								cti.getValue().getMct().close();
							}
						}
					});
				}
			}
			castTaskMap.clear();
		}
	}
	/*public static MulticastThread getSendThreadListByTaskid(int taskid){
		synchronized (castTasklist) {
			for(CastTaskInfo cti:castTasklist){
				if(cti.getTaskid()==taskid){
					if(cti.getMct() !=null)
						return cti.getMct();
				}
			}
		}
		return null;
	}*/
	/*
	/**
	 * 从广播任务列表中删除广播任务信息
	 * @param castTaskid 广播任务编号
	 */
	/*public static void delCastTaskfromList(int castTaskid,MulticastThread mfst) {
		for(int i=0;i<castTasklist.size();i++){
			if(castTasklist.get(i).getCastTaskId()==castTaskid){
				List<DomainInfo> domainInfos = castTasklist.get(i).();
				for(DomainInfo dInfo:domainInfos) {
					if(dInfo.getMct().equals(mfst)) {
						domainInfos.remove(dInfo);
						break;
					}
				}
				if(!(domainInfos.size() >=1))
					castTasklist.remove(i);
			}
		}
	}*/
	/**
	 * 获取IO线程池
	 * @param 
	 * @return
	 */
	public static ExecutorService getExecutorService() {
		return executorService;
	}

	public static void setExecutorService(ExecutorService executorService) {
		GlobalInfoController.executorService = executorService;
	}
	/**
	 * 获取定时线程池
	 * @param 
	 * @return
	 */
	public static ScheduledExecutorService getScheduledThreadPool() {
		return scheduledThreadPool;
	}

	public static void setScheduledThreadPool(ScheduledExecutorService scheduledThreadPool) {
		GlobalInfoController.scheduledThreadPool = scheduledThreadPool;
	}
	
	public static void resetMultiCastGlobalInfo(){
		synchronized (multicastAddress) {
			GlobalInfoController.multicastAddress="224.0.1.1";
		}
		synchronized (multicastPort){
			GlobalInfoController.multicastPort=49153;
		}
//		System.out.println("定时重置组播地址信息");
	}
	/**
	 * 获取组播线程池
	 * @param 
	 * @return
	 */
	public static ExecutorService getGroupexecutorService() {
		return groupexecutorService;
	}
	public static void setGroupexecutorService(ExecutorService groupexecutorService) {
		GlobalInfoController.groupexecutorService = groupexecutorService;
	}
	
	
	/**
	 * 分发数据或者给任务待机的线程池
	 * @param 
	 * @return
	 */
	public static ExecutorService getStandbyexecutorService() {
		return standbyexecutorService;
	}
	public static void setStandbyexecutorService(ExecutorService standbyexecutorService) {
		GlobalInfoController.standbyexecutorService = standbyexecutorService;
	}
	
	public static Map<Integer, SendPointData> getPointTaskMap() {
		return pointTaskMap;
	}
	public static void setPointTaskMap(Map<Integer, SendPointData> pointTaskMap) {
		GlobalInfoController.pointTaskMap = pointTaskMap;
	}
	public static List<MonitorInfoBean> getmInfoBeans() {
		return mInfoBeans;
	}
	
	
	public static Map<String, TerminalInfo> getTerinfoMap() {
		return terinfoMap;
	}
	public static Map<Integer, CastTaskInfo> getCastTaskMap() {
		return castTaskMap;
	}
	/**
	 *  更新系统信息
	 * @param bean
	 * 
	 */
	public static void addInfoBaen(MonitorInfoBean bean) {
		synchronized (mInfoBeans) {
			if(mInfoBeans.size() >= 10) {//超过10次的丢弃前面的信息
				mInfoBeans.remove(9);
			}
			mInfoBeans.add(0, bean);
		}
	}
	/**
	 *  存入点播定时器信息
	 * @param timesize
	 * @param sendPointData
	 */
	public static boolean putPointTaskMap(int timesize,SendPointData sendPointData) {
		synchronized (pointTaskMap) {
			if(pointTaskMap.get(timesize) == null) {
				pointTaskMap.put(timesize, sendPointData);
				return true;
			}else {
				return false;
			}
		}
	}
	/**
	 *  获取对应时间分片下的定时器，若无则返回null
	 * @param timesize
	 * @return
	 */
	public static SendPointData getsendPointData(int timesize) {
		return pointTaskMap.get(timesize);
	}
	/**
	 * 	移除对应时间分片下的定时器
	 * @param timesize
	 * @return
	 */
	public static boolean removesendPointData(int timesize) {
		synchronized (pointTaskMap) {
			if(pointTaskMap.remove(timesize) != null)return true;
			else return false;
		}
	}
	/**
	 * 	移除对应时间分片下的定时器的对应定时任务
	 * @param timesize
	 * @return
	 */
	public static boolean removePointTask(int taskid,int timesize) {
		SendPointData sData = getsendPointData(timesize);
		if(sData != null) {
			if(sData.removePointCastInfo(taskid))return true;
			else return false;
		}else {
			return false;
		}
	}
	/**
	 * 	获取对应时间分片下的定时器的对应定时任务
	 * @param timesize
	 * @return
	 */
	public static PointCastInfo getPointTask(int taskid,int timesize) {
		SendPointData sData = getsendPointData(timesize);
		if(sData != null) {
			for(PointCastInfo pInfo:sData.getpointList()) {
				if(pInfo.getTaskid() == taskid)return pInfo;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param senddata 发送的byte[]组
	 * @param session 目标session
	 * @param Timeout 延时长度
	 * TODO	发送数据给终端
	 * 时间：2019年1月11日
	 */
	public static void SendData(byte[] senddata,IoSession session,int Timeout) {
		putThreadintoPool(new Runnable() {
			@Override
			public void run() {
				// IO线程池中运行
				if(Timeout > 0) {
					try {
						Thread.sleep(Timeout < 500 ? Timeout:500);
					} catch (InterruptedException e) {
						// TODO 延时长度，保证某些有序发送段能做到,防止线程池堵塞，最多延时500毫秒
						e.printStackTrace();
					}
				}
				if(senddata != null && session != null) {
					  IoBuffer buf = IoBuffer.wrap(senddata);
					  WriteFuture future = session.write(buf);  
			          // 在100毫秒超时间内等待写完成
					  future.awaitUninterruptibly(100);
			          // The message has been written successfully
			          if( future.isWritten())
			          {
			                      // send sucess!
			          }
			          // The messsage couldn't be written out completely for some reason.
			          // (e.g. Connection is closed)
			          else
			          {
			              logger.warn("回复给客户端的数据发送失败！IP:"+((InetSocketAddress)session.getRemoteAddress()).getAddress().getHostAddress());
			              session.closeNow();
			          }
				}
			}
		});
	}
	/**
	 * 
	 * TODO	监测终端是否入组(分组)
	 * 时间：2019年1月11日
	 */
	public static void isCastCMDReturn(List<TerminalInfo> terinfolist) {
		for(TerminalInfo tInfo:terinfolist) {
			tInfoisCastCMDReturn(tInfo);
		}
	}
	/**
	 * 
	 * TODO	监测终端是否入组（单一终端）
	 * 时间：2019年1月11日
	 */
	public static void tInfoisCastCMDReturn(TerminalInfo tInfo) {
		try {
			if(!tInfo.getIsCastCMDReturn() && tInfo.getOrderCastInfo().size() > 0 && tInfo.getRetry() < 3) {
				EndCast(tInfo);//重新对指定终端发送退组入组命令
				Thread.sleep(10);
				new TimeReloady(tInfo);
			}else if(tInfo.getRetry() >= 3){
				synchronized (tInfo) {
					tInfo.setRetry(0);
					tInfo.setIsCastCMDReturn(true);
					tInfo.setIscast(false);
				}
			}else {
				synchronized (tInfo) {
					tInfo.setIsCastCMDReturn(true);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logError(e);
		}
	}
	/**
	 * 
	 * @param DomainId
	 * @return mytInfos
	 * @throws Exception
	 * TODO 获取指定分区的终端ID
	 * 时间：2019年1月2日
	 */
	public static List<TerminalInfo> listTerByDomainId(String DomainId) throws Exception {
		//获取指定分区的终端ID
		List<TerminalInfo> mytInfos = new ArrayList<>();
		for(Map.Entry<String, TerminalInfo> tInfo:terinfoMap.entrySet()) {
			if(tInfo.getValue().getDomainId().equals(DomainId))
				mytInfos.add(tInfo.getValue());
		}
		return mytInfos;
	}
	/**
	 * 根据Session获取指定终端信息
	 * @return
	 */
	public static TerminalInfo getTerminalInfo(IoSession session) {
		if(session.getAttribute(MinaCastHandler.TERID) != null) {
			return getTerminalInfo(session.getAttribute(MinaCastHandler.TERID).toString());
		}
		for(Map.Entry<String, TerminalInfo> tInfo:terinfoMap.entrySet()) {
			if(session.equals(tInfo.getValue().getSession()))return tInfo.getValue();
		}
		return null;
	}
	public static Long getDate() {
		return Date;
	}
	public static void setDate(Long date) {
		Date = date;
	}
	//初始化系统IP、终端配置信息
	public static void initServerConfig(int time) {
		scheduledThreadPool.schedule(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					SERVERIP =  Tools.GetValueByKey(Const.CONFIG, "serverIp");
					GATEWAY =  Tools.GetValueByKey(Const.CONFIG, "gateway");
					NETMASK =  Tools.GetValueByKey(Const.CONFIG, "netmask");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logError(e);
				}
				if(SERVERIP == null) {
					initServerConfig(time*2);
					logger.error("获取服务器配置信息失败，正在尝试重新获取");
				}
			}
		}, time, TimeUnit.SECONDS);
	}
	//更新全部在线终端的配置
	public static boolean TermUpdata() {
		try {
			putThreadintoStandbyPool(new Runnable() {
				@Override
				public void run() {
					try {
						List<Terminals> tList = terminalsService.listAllTerm(new Page());
						// TODO Auto-generated method stub
						for(Map.Entry<String, TerminalInfo> tInfo:terinfoMap.entrySet()) {
							if((tInfo.getValue().getIsOnline()||tInfo.getValue().getIstrueOnline())) {
								for(Terminals ter:tList) {
									if(tInfo.getKey().equals(ter.getTIDString())) {
										SendData(InterCMDProcess.sendTerReset(ter), tInfo.getValue().getSession(), 0);
										tList.remove(ter);
										break;
									}
								}
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
						logError(e);
					}
				}
			});
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			logError(e);
		}
		return false;
	}
		//全部在线终端重启
		public static void AllTerReboot() {
			try {
				// TODO Auto-generated method stub
				for(Map.Entry<String, TerminalInfo> tInfov:terinfoMap.entrySet()) {
					TerminalInfo tInfo = tInfov.getValue();
					if((tInfo.getIsOnline()||tInfo.getIstrueOnline())) {
						SendData(InterCMDProcess.sendTerReboot(), tInfo.getSession(), 0);
						/*synchronized (tInfo) {
							tInfo.setIsOnline(false);
							tInfo.setIstrueOnline(false);
						}*/
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				logError(e);
			}
		}
		
	public static Map<String, QtClientInfo> getClinetmap() {
		return clinetmap;
	}
	/**
	 * 获取指定Qt终端信息
	 * @return
	 */
	public static QtClientInfo getClientInfo(String userid) {
		if(userid != null)
			return clinetmap.get(userid);
		else
			return null;
	}
	/**
	 * 删除指定Qt终端信息
	 * @return
	 */
	public static void deleteClientInfo(String userid) {
		QtClientInfo cInfo = clinetmap.get(userid);
		if(cInfo != null && cInfo.getTaskid()>0 && getCastTaskInfo(cInfo.getTaskid()) != null) {
			stopCastTaskInList(cInfo.getTaskid());
		}
		clinetmap.remove(userid);
	}
	/**
	 * 添加指定Qt终端信息
	 * @return
	 */
	public static void putClientInfo(String userid,QtClientInfo clientInfo) {
		clinetmap.put(userid, clientInfo);
	}
	public static TCPMinaCastThread getTcpThread() {
		return tcpThread;
	}
	public static void setTcpThread(TCPMinaCastThread tcpThread) {
		GlobalInfoController.tcpThread = tcpThread;
	}
}
