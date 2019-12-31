package com.niocast.cast;

import com.audioweb.entity.Domains;
import com.audioweb.entity.Terminals;
import com.audioweb.service.impl.CastTaskService;
import com.audioweb.service.impl.DomainsService;
import com.audioweb.service.impl.SystemService;
import com.audioweb.service.impl.TerminalsService;
import com.audioweb.util.*;
import com.niocast.entity.CastTaskInfo;
import com.niocast.entity.PointCastInfo;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TerminalUnicast extends BaseStaticLogger{
	private static SystemService systemService = (SystemService) SpringContextUtils
			.getBeanByClass(SystemService.class);
	private static CastTaskService castTaskService = (CastTaskService)SpringContextUtils.getBeanByClass(CastTaskService.class);
	private static TerminalsService terminalsService = (TerminalsService) SpringContextUtils
			.getBeanByClass(TerminalsService.class);
	private static DomainsService domainservice = (DomainsService) SpringContextUtils
			.getBeanByClass(DomainsService.class);
	private static String basepath = null;
	//终端点播获取文件列表
	public static byte[] getFilesName(String path) {
		//String omit = "…";
		ByteBuffer bytes = null ;
		try {
			if(basepath == null) basepath = systemService.getBaseAttri("终端点播目录");
			ArrayList<String> filelist= null;
			if(path == null)
				filelist = FileUtil.getFiles(basepath,path,1);
			else {
				filelist = FileUtil.getFiles(basepath,path,2);
			}
			bytes = ByteBuffer.allocate(filelist.size()*34);
			//byte[] bomit = omit.getBytes("GB2312");
			for(int i =0;i < filelist.size() && i < 30;i++) {
/*				if(filelist.get(i).getBytes("GB2312").length > 34) {
					byte[] names = new  byte[34];
					System.arraycopy(filelist.get(i).getBytes("GB2312"),0,names,0,26);
					System.arraycopy(bomit,0,names,26,2);
					System.arraycopy(filelist.get(i).getBytes("GB2312"),filelist.get(i).getBytes("GB2312").length-4,names,28,4);//超过34字节的文字用...代替
					bytes.put(names);
				}else {
*/					bytes.put(filelist.get(i).getBytes("GB2312"));
					for(int j = 34-filelist.get(i).getBytes("GB2312").length;j>0;j--) {
						bytes.put((byte)0);
					}/*
				}*/
				/*
				System.arraycopy(name,0,data3,0,data1.length);
				System.arraycopy(data2,0,data3,data1.length,data2.length);*/
			}
			//System.out.println(new String(bytes,"GB2312"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bytes.flip();  
		byte[] by = new byte[bytes.remaining()];
		bytes.get(by);
		bytes.clear();
		return by;
	}
	//终端点播播放
	public static void Unicast(TerminalInfo tInfo,String fileName) {
		try {
			int castlevel = Integer.parseInt(systemService.getBaseAttri("终端点播优先级"));
			List<String> type = new ArrayList<String>();
			int taskid = GlobalInfoController.getTaskId();
			type.add(Const.CASTTYPE[4]);
			type.add(Const.CASTTYPE[4]+taskid);
			fileName = fileName.replace("/", "\\");
			List<String> filelist = castTaskService.FileAnalyze(Const.CASTTYPE[4], fileName);
			if(tInfo != null) {
				CastTaskInfo castTaskInfo = new CastTaskInfo(taskid, type, castlevel, tInfo, filelist);
				castTaskService.startPointCastTask(castTaskInfo);
				saveLog(Const.LOGTYPE[2], Const.CASTTYPE[4], "开始广播", tInfo.getIpAddress().getHostAddress(),String.valueOf(taskid));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//终端采播播放
	public static boolean Termcast(TerminalInfo tInfo,String type) {
		try {
			if(tInfo != null && tInfo.getIsAutoCast()) {
				Terminals terminals = terminalsService.getTermByTid(tInfo.getTerid());
				if(terminals != null) {
					CastTaskInfo castTaskInfo = null;
					int taskid = GlobalInfoController.getTaskId();
					try {
						int castlevel = Integer.parseInt(systemService.getBaseAttri("终端采播优先级"));
						List<String> types = new ArrayList<String>();
						types.add(Const.CASTTYPE[3]);
						types.add(Const.CASTTYPE[3]+taskid);
						types.add(type);
						types.add(Tools.GetValueByKey(Const.CONFIG, "netheartPort"));
						//获取组播端口以及IP
						String multicastaddress = GlobalInfoController.getMulticastAddress();
						int multicastport = castTaskService.PortAnalyze();
						PageData pd = new PageData();
						String Precinct = terminals.getPrecinct();
						String[] domidlist ;
						String terids = "";
						List<String> domids = new ArrayList<String>();
						String[] grouplist = Precinct == null ? null : Precinct.split("//");
						domidlist = grouplist[0] == null ? null : grouplist[0].split(",");
						for(int i = 0;i<domidlist.length;i++) {
							if(!domidlist[i].contains("*")) {
								domids.add(domidlist[i]);
							}
						}
						String domIds = grouplist[0].replaceAll("[//*]", "");
						pd.put("domidlists", domids);
						if(grouplist.length > 1) {
							terids = grouplist[1];
						}
						List<PageData> terlist= terminalsService.listAllTerByDomainsId(pd);
						for(PageData s:terlist) {
							terids += ","+s.getString("TIDString");
						}
						List<String> domainidlist = castTaskService.InfoAnalyze(domIds);
						List<String> terlists = castTaskService.InfoAnalyze(terids);
						List<TerminalInfo> terminalInfos = castTaskService.TermAnalyze(terlists, domainidlist, tInfo);
						castTaskInfo = new CastTaskInfo(taskid, types, castlevel, domainidlist, terminalInfos, 30, multicastaddress, multicastport, -1, tInfo, true);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						saveLog(Const.LOGTYPE[2], Const.CASTTYPE[3], "终端主动采播启动出错", tInfo.getIpAddress().getHostAddress(),String.valueOf(taskid));
						return false;
					}
					castTaskService.startPagCastTask(castTaskInfo);
					saveLog(Const.LOGTYPE[2], Const.CASTTYPE[3], "终端主动采播开始", tInfo.getIpAddress().getHostAddress(),String.valueOf(taskid));
					return true;
				}
			}else {
				saveLog(Const.LOGTYPE[2], Const.CASTTYPE[3], "终端无权限主动采播", tInfo.getIpAddress().getHostAddress(),tInfo.getTerid());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 	点播停止，type为类型
	 * @param terminalInfo
	 * @param type
	 */
	public static void Unicaststop(TerminalInfo terminalInfo,String type) {
		GlobalInfoController.getScheduledThreadPool().schedule(new stopPointCast(terminalInfo,type,0), 0, TimeUnit.MILLISECONDS);
	}
	/**
	 * 	终端点播暂停
	 * @param terminalInfo
	 */
	public static boolean castPause(TerminalInfo tInfo,boolean ispause) {
		// TODO Auto-generated method stub
		if(tInfo != null && tInfo.getCastType().equals("终端点播")) {
			PointCastInfo pInfo = GlobalInfoController.getPointTask(tInfo.getOrderCastInfo().get(0).getTaskid(), tInfo.getOrderCastInfo().get(0).getMct().getTimesize());
			if(pInfo != null) {
				pInfo.setIsStop(ispause);
				return true;
			}else {
				try {
					Thread.sleep(100);
					pInfo = GlobalInfoController.getPointTask(tInfo.getOrderCastInfo().get(0).getTaskid(), tInfo.getOrderCastInfo().get(0).getMct().getTimesize());
					if(pInfo != null) {
						pInfo.setIsStop(ispause);
						return true;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	//寻呼话筒点播终端列表
	public static byte[] Unicastdomain(TerminalInfo tInfo,String domain) {
		PageData pd = new PageData();
		ByteBuffer bytes = null;
		try {
			byte[] f = {(byte)0};
			String flag;
			Terminals terids = terminalsService.getTermByTid(tInfo.getTerid());
			if(terids != null && terids.getISCMIC()) {
				List<Domains> domains = new ArrayList<Domains>();
				List<Terminals> tList = new ArrayList<Terminals>();
				if(domain == null) {//获取根分组
					String domainsId = terids.getPrecinct();
					String[] grouplist = domainsId == null ? null : domainsId.split("//");
					String domIds = grouplist[0].replaceAll("[//*]", "");
					List<String> domainidlist = castTaskService.InfoAnalyze(domIds);
					domains = domainservice.listAllDomains("");
					for(int i = domains.size()-1;i>=0;i--) {
						if(!domainidlist.contains(String.valueOf(domains.get(i).getDomainId()))) {
							domains.remove(i);
						}
					}
				}
				else {//获取进一步的分组
					String[] domidlist ;
					String terid = "";
					List<String> domids = new ArrayList<String>();
					String domainsId = terids.getPrecinct();
					String[] grouplist = domainsId == null ? null : domainsId.split("//");
					domidlist = grouplist[0] == null ? null : grouplist[0].split(",");
					for(int i = 0;i<domidlist.length;i++) {
						if(!domidlist[i].contains("*")) {
							domids.add(domidlist[i]);
						}
					}
					String domIds = grouplist[0].replaceAll("[//*]", "");
					pd.put("domidlists", domids);
					if(grouplist.length > 1) {
						terid = grouplist[1];
					}
					List<PageData> terlist= terminalsService.listAllTerByDomainsId(pd);
					for(PageData s:terlist) {
						terid += ","+s.getString("TIDString");
					}
					List<String> domainidlist = castTaskService.InfoAnalyze(domIds);
					List<String> terlists = castTaskService.InfoAnalyze(terid);
					flag = new String(f, "GB2312");
					domain = domain.replace(flag, "");
					domain = domain.replace("-", "");
					List<String> ids = new ArrayList<String>();
					ids.add(domain);
					pd.put("ids", ids);
					List<Domains> d = domainservice.listAllDomainByIds(pd);
					if(d != null) {
						tList = terminalsService.listTerByDomainId(String.valueOf(d.get(0).getDomainId()));
						for(int i = tList.size()-1;i>=0;i--) {
							if(!terlists.contains(tList.get(i).getTIDString())) {
								tList.remove(i);
							}
						}
						domains = domainservice.listAllSubByDomainId(String.valueOf(d.get(0).getDomainId()));
						for(int i = domains.size()-1;i>=0;i--) {
							if(!domainidlist.contains(String.valueOf(domains.get(i).getDomainId()))) {
								domains.remove(i);
							}
						}
					}
				}
				
				//编码
				Collections.sort(domains);
				rename(domains);
				bytes = ByteBuffer.allocate((domains.size()*14+tList.size())*14+1);
				bytes.put((byte)(domains.size()+tList.size()));
				for(int i =0;i < domains.size();i++) {
					if(domains.get(i).getDomainName().getBytes("GB2312").length > 14) {
						byte[] names = new  byte[14];
						System.arraycopy(domains.get(i).getDomainName().getBytes("GB2312"),0,names,0,14);
						bytes.put(names);
					}else {
						bytes.put(domains.get(i).getDomainName().getBytes("GB2312"));
						for(int j = 14-domains.get(i).getDomainName().getBytes("GB2312").length;j>0;j--) {
							bytes.put((byte)0);
						}
					}
				}
				if(domain != null && tList != null) {
					for(int i =0;i < tList.size();i++) {
						if(tList.get(i).getTIDString().getBytes("GB2312").length > 14) {
							byte[] names = new  byte[14];
							System.arraycopy(tList.get(i).getTIDString().getBytes("GB2312"),0,names,0,14);
							bytes.put(names);
						}else {
							bytes.put(tList.get(i).getTIDString().getBytes("GB2312"));
							for(int j = 14-tList.get(i).getTIDString().getBytes("GB2312").length;j>0;j--) {
								bytes.put((byte)0);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(bytes != null) {
			bytes.flip();  
			byte[] by = new byte[bytes.remaining()];
			bytes.get(by);
			bytes.clear();
			return by;
		}else {
			return null;
		}
	}
	private static void rename(List<Domains> domains) {
		//根据终端编号修改终端名称
		for(Domains domain : domains) {
			int id = domain.getDomainId();
			while(id >100) {
				domain.setDomainName("-"+domain.getDomainName());
				id = id/100;
			}
		}
	}
	//寻呼话筒点播播放
	public static boolean CMICCast(TerminalInfo tInfo,String domainName,String tids) {
		String[] tidss = tids.split(",");
		List<String> tidlist = new ArrayList<>();
		for(String d:tidss) {
			tidlist.add(d);
		}
		tidlist = rankTid(tidlist);
		List<String> type = new ArrayList<String>();
		type.add(Const.CASTTYPE[5]);
		type.add(Const.CASTTYPE[5]+tInfo.getTerid());
		PageData pd = new PageData();
		try {
			List<String> idStrings = rank(domainName);
			idStrings.addAll(tidlist);
			type.add(String.valueOf(idStrings.size()));//添加num信息 type长度为3
			pd.put("ids", idStrings);
			if(tInfo != null && tInfo.getIsPaging()) {
				//分区格式化处理
				List<Domains> domainslist = new ArrayList<Domains>();
				List<Domains> domains = domainservice.listAllDomainByIds(pd);
				for(Domains domain:domains) {
					List<Domains> dm = domainservice.listAllSubByDomainId(String.valueOf(domain.getDomainId()));
					if(dm.size() >0) {
						domainslist.removeAll(dm);
						domainslist.addAll(dm);
					}
				}
				domainslist.addAll(domains);
				//格式化分区获取终端的对象
				for(Domains domain:domainslist) {
					idStrings.add(String.valueOf(domain.getDomainId()));
				}
				pd.put("ids", idStrings);
				List<Terminals> terminals = terminalsService.listAllTerByIds(pd);
				//特定选中终端加入组中
				for(String tid:tidlist) {
					Terminals t = terminalsService.getTermByTid(tid);
					if(t != null && !idStrings.contains(t.getDomainId()))
						idStrings.add(String.valueOf(t.getDomainId()));
				}
				//终端信息
				for(Terminals ter:terminals) {
					tidlist.add(ter.getTIDString());
				}
				//判断源终端组是否添加
				if(!idStrings.contains(tInfo.getDomainId()))
					idStrings.add(String.valueOf(tInfo.getDomainId()));
				//获取终端列表
				List<TerminalInfo> terminalInfos = castTaskService.TermAnalyze(tidlist, idStrings, tInfo);
				//开启广播
				int taskid = GlobalInfoController.getTaskId();
				int castlevel = Integer.parseInt(systemService.getBaseAttri("寻呼话筒优先级"));
				String groupAddress = GlobalInfoController.getMulticastAddress();
				int groupport = GlobalInfoController.getMulticastPort();//寻呼话筒因为管理权限问题，设置为主动
				CastTaskInfo castTaskInfo = new CastTaskInfo(taskid, type, castlevel, idStrings, terminalInfos, 30, groupAddress, groupport, -1, tInfo,true);
				castTaskService.startPagCastTask(castTaskInfo);
				saveLog(Const.LOGTYPE[2], Const.CASTTYPE[5], "寻呼话筒开始广播", tInfo.getIpAddress().getHostAddress(),castTaskInfo.getTaskName());
			}else {
				saveLog(Const.LOGTYPE[2], Const.CASTTYPE[5], "寻呼话筒点播失败", tInfo.getIpAddress().getHostAddress(),tInfo.getTerid());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private static List<String> rank(String domainName) {
		//重构分区结构
		domainName = domainName.replaceAll("-", "");
		byte[] f = {(byte)0};
		String flag;
		String[] idStrings =null;
		List<String> idList = new ArrayList<String>();
		try {
			flag = new String(f, "GB2312");
			domainName = domainName.replaceAll(flag," ");
			idStrings = domainName.split(" ");
			for(String str:idStrings) {
				if(!(" ".equals(str) || "".equals(str)))
				idList.add(str);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return idList;
	}
	private static List<String> rankTid(List<String> Tidlist) {
		//重构分区结构
		byte[] f = {(byte)0};
		String flag;
		List<String> list = new ArrayList<String>();
		try {
			flag = new String(f, "GB2312");
			for(String str:Tidlist) {
				str = str.replaceAll(flag,"");
				str = str.replaceAll(" ","");
				str = str.replaceAll("-","");
				if(!str.equals(""))
					list.add(str);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
}

class stopPointCast implements Runnable{
	private TerminalInfo terminalInfo;
	private String type;
	private int times;
	public stopPointCast(TerminalInfo terminalInfo,String type,int times) {
		// TODO Auto-generated constructor stub
		this.terminalInfo = terminalInfo;
		this.type = type;
		this.times = times;
	}
	@Override
	public void run() {
		boolean is =false;
		// TODO Auto-generated method stub
		try {
			if(terminalInfo != null) {
				if(terminalInfo.getOrderCastInfo().size() > 0) {
					for(int i =0;i < terminalInfo.getOrderCastInfo().size();i++) {
						if(type.equals(terminalInfo.getOrderCastInfo().get(i).getMultiCastType())) {
							GlobalInfoController.stopCastTaskInList(terminalInfo.getOrderCastInfo().get(i).getTaskid());
							is = true;
						}
					}
				}
			}
			if(!is && times < 3) {
				GlobalInfoController.getScheduledThreadPool().schedule(new stopPointCast(terminalInfo,type,++times), 200, TimeUnit.MILLISECONDS);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
