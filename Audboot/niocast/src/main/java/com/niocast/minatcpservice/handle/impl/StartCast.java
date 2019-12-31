package com.niocast.minatcpservice.handle.impl;

import com.audioweb.service.impl.CastTaskService;
import com.audioweb.service.impl.TerminalsService;
import com.audioweb.util.Const;
import com.audioweb.util.PageData;
import com.audioweb.util.SpringContextUtils;
import com.niocast.entity.CastTaskInfo;
import com.niocast.entity.TerminalInfo;
import com.niocast.minatcpservice.handle.DefaultCommand;
import com.niocast.util.GlobalInfoController;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.List;

public class StartCast extends DefaultCommand {
	private CastTaskService castTaskService = (CastTaskService)SpringContextUtils.getBeanByClass(CastTaskService.class);
	private TerminalsService terminalsService = (TerminalsService)SpringContextUtils.getBeanByClass(TerminalsService.class);
	
	private List<String> domainidlist ;//分组信息
	private List<TerminalInfo> terminalInfos;//终端信息

	public StartCast(IoSession session, byte[] content) {
		super(session, content);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public byte[] execute() {
		// TODO Auto-generated method stub
		try {
			if(commd.length >= 4) {
				String castlevel = commd[1];
				String vol = commd[2];
				String tids = commd[3];
				if(Format(tids)) {
					if(info != null) {
						int taskid = GlobalInfoController.getTaskId();
						List<String> types = new ArrayList<String>();
						types.add(Const.CASTTYPE[6]);
						types.add(info.getUsername()+":"+Const.CASTTYPE[6]);
						types.add(info.getUserid());
						String multicastaddress = GlobalInfoController.getMulticastAddress();
						int multicastport  = castTaskService.PortAnalyze();
						CastTaskInfo castTaskInfo = new CastTaskInfo(taskid, types, Integer.parseInt(castlevel), domainidlist, terminalInfos, Integer.parseInt(vol), multicastaddress, multicastport, false);
						//castTaskService.startFileCastTask(terids, domids, null, vol, castlevel, "-1", 0, types);
						boolean is = castTaskService.startCommCastTask(castTaskInfo);
						if(info.getTaskid() != 0) {
							GlobalInfoController.stopCastTaskInList(info.getTaskid());
						}else {
							info.setTaskid(taskid);
						}
						if(is) {
							logservice.saveLog(Const.LOGTYPE[1]+"-"+info.getUserid(), FUNCTION, "控件终端开启采播", IP, taskid+"");
							return StrToByte(StartCast+":"+taskid+":"+multicastaddress+":"+multicastport);//开启广播成功
						}else {
							logservice.saveLog(Const.LOGTYPE[1]+"-"+info.getUserid(), FUNCTION, "控件终端开启采播出错", IP, taskid+"");
							return StrToByte(CastError);//开启广播出错
						}
					}else {
						return StrToByte(CommError);
					}
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.error("开启广播出错:",e);
		}
		return StrToByte(CastCreatError);//创建任务出错
	}

	private boolean Format(String tids) {
		PageData pd = new PageData();
		try {
			String[] domidlist ;
			String terids = "";
			List<String> domids = new ArrayList<String>();
			String[] grouplist = tids == null ? null : tids.split("//");
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
			domainidlist = castTaskService.InfoAnalyze(domIds);
			List<String> terlists = castTaskService.InfoAnalyze(terids);
			terminalInfos = castTaskService.TermAnalyze(terlists, domainidlist, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
