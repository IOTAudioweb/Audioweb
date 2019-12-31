package com.audioweb.controller;

import com.audioweb.entity.Domains;
import com.audioweb.entity.Users;
import com.audioweb.service.*;
import com.audioweb.util.AppUtil;
import com.audioweb.util.Const;
import com.audioweb.util.Jurisdiction;
import com.audioweb.util.PageData;
import com.niocast.cast.MulticastThread;
import com.niocast.entity.CastTaskInfo;
import com.niocast.entity.QtClientInfo;
import com.niocast.entity.TerminalInfo;
import com.niocast.minatcpservice.handle.DefaultCommand;
import com.niocast.util.GlobalInfoController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/realtimecast")
public class RealTimeCastController extends BaseController {
    private static final String FUNCTION = "实时采播";
    @Resource(name = "systemService")
    private SystemManager systemService;
    @Resource(name = "DomainsService")
    private DomainsManager domainsService;
    @Resource(name = "TerminalsService")
    private TerminalsManager terminalsService;
    @Resource(name = "castTaskService")
    private CastTaskManager castTaskService;
    @Resource(name = "usersService")
    private UsersManager usersService;

    /**
     * 去实时采播
     *
     * @return
     */
    @RequestMapping("/toCast")
    public ModelAndView toCast() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = this.getPageData();
        if (!Jurisdiction.hasJurisdiction("realtimecast/toCast.do")) {
            return logout();
        } // 权限校验
        pd.put("userid", Jurisdiction.getUserid());
        Users users = usersService.findByUserid(pd);
        if (users != null) {
            mv.addObject("realcasttype", users.getRealcasttype());
        }
        List<Domains> alldomain = domainsService.listAllDomains("");
        //添加根目录
        Domains domains = new Domains();
        domains.setDomainId(0);
        domains.setParentDomainId(-1);
        domains.setDomainName("根分区");
        alldomain.add(domains);
        JSONArray domarr = JSONArray.fromObject(alldomain);//所有分组
        JSONArray lastarr = new JSONArray();
        for (int i = 0; i < domarr.size(); i++) {//设置jsonobject类型：0表示分组，1表示终端
            JSONObject obj = domarr.getJSONObject(i);
            obj.element("type", "0");
            obj.element("isParent", true);
            if (obj.getString("domainId").equals("0")) {
                obj.element("open", true);
            }
            lastarr.add(obj);
        }
        List<PageData> termList = terminalsService.listAllTer("");//所有终端
        JSONArray terarr = JSONArray.fromObject(termList);//所有终端列表
        String terjson = terarr.toString().replaceAll("DomainId", "pId").replaceAll("TIDString", "tid").replaceAll("TName", "name");
        terarr = JSONArray.fromObject(terjson);
        for (int i = 0; i < terarr.size(); i++) {
            JSONObject obj = terarr.getJSONObject(i);
            obj.element("type", "1");
            lastarr.add(obj);
        }
        String lastjson = lastarr.toString().replaceAll("parentDomainId", "pId").replaceAll("domainId", "id").replaceAll("domainName", "name");
        QtClientInfo info = GlobalInfoController.getClientInfo(Jurisdiction.getUserid());
        if (info != null) {
            mv.addObject("clientTaskId", info.getTaskid());
        }
        mv.addObject("pd", pd);
        mv.addObject("zTreeNodes", lastjson);
        mv.setViewName("realtimecast/realtimecast-mp3");
        return mv;
    }

    /**
     * 去本地实时采播
     *
     * @return
     */
    @RequestMapping("/openFile")
    public ModelAndView openFile() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("realtimecast/toCast.do")) {
            return logout();
        } // 权限校验
        QtClientInfo info = GlobalInfoController.getClientInfo(Jurisdiction.getUserid());
        if (info != null) {
            GlobalInfoController.SendData(DefaultCommand.SendTop.getBytes(), info.getSession(), 0);
            mv.setViewName("realtimecast/nofile");
        } else
            mv.setViewName("realtimecast/openfile");
        return mv;
    }

    /**
     * 开始广播
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/startCast")
    @ResponseBody
    public Object startCast() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        try {
            List<String> domainidlist = castTaskService.InfoAnalyze(pd.getString("domids"));
            List<String> tList = castTaskService.InfoAnalyze(pd.getString("terids"));
            List<TerminalInfo> castTeridlist = castTaskService.TermAnalyze(tList, domainidlist, null);
            boolean isTimer = pd.getString("playmode").equals("1") ? true : false;
            int vol = Integer.parseInt(pd.get("vol").toString());
            int castlevel = Integer.parseInt(pd.get("castlevel").toString());
            int taskid = GlobalInfoController.getTaskId();
            List<String> types = new ArrayList<String>();
            types.add(Const.CASTTYPE[2]);
            types.add(Const.CASTTYPE[2] + taskid);
            String multicastaddress = GlobalInfoController.getMulticastAddress();
            int multicastport = castTaskService.PortAnalyze();
            CastTaskInfo castTaskInfo = new CastTaskInfo(taskid, types, castlevel, domainidlist, castTeridlist, vol, multicastaddress, multicastport, isTimer);
            //castTaskService.startFileCastTask(terids, domids, null, vol, castlevel, "-1", 0, types);
            boolean is = castTaskService.startCommCastTask(castTaskInfo);
            if (is) {
                saveLog(Const.LOGTYPE[1], FUNCTION, "开始广播", this.getRemortIP(), "任务编号：" + taskid + "，模式：" + (pd.getString("playmode").equals("1") ? "稳定" : (pd.getString("playmode").equals("0") ? "低延时" : "急速")));
                map.put("taskid", taskid + "");
                map.put("result", "success");
            } else {
                map.put("result", "error");
            }
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
            map.put("result", "error");
        }
//		}else{
//			map.put("result", "error");
//		}
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 结束广播,添加日志
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/stopCast")
    @ResponseBody
    public Object stopCast() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String taskid = (null == pd.get("taskid") || "".equals(pd.get("taskid").toString())) ? "" : pd.get("taskid").toString();
        if (taskid != null && !"".equals(taskid)) {
            MulticastThread mct = GlobalInfoController.stopCastTaskInList(Integer.parseInt(taskid));//停止发送线程
            if (mct != null) {//表示广播任务存在，则添加日志，若广播任务已经删除则不添加日志
                saveLog(Const.LOGTYPE[1], FUNCTION, "结束广播", this.getRemortIP(), taskid);
            }
            map.put("result", "success");
        } else {
            map.put("result", "error");
        }
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 开始广播
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/volChange")
    @ResponseBody
    public Object volChange() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        int vol = Integer.parseInt(pd.get("vol").toString());
        String taskid = (null == pd.get("taskid") || "".equals(pd.get("taskid").toString())) ? "" : pd.get("taskid").toString();
        if (taskid != null && !"".equals(taskid)) {
            CastTaskInfo cInfo = GlobalInfoController.getCastTaskInfo(Integer.parseInt(taskid));
            if (cInfo != null) {
                cInfo.setVol(vol);//音量控制
                logger.debug("调节音量:" + vol + "	任务:" + cInfo.getTaskName());
                map.put("result", "success");
                return AppUtil.returnObject(new PageData(), map);
            }
        }
        map.put("result", "error");
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 获取控件是否登录
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/getClient")
    @ResponseBody
    public Object getClient() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        String userRid = Jurisdiction.getUserid();
        QtClientInfo info = GlobalInfoController.getClientInfo(userRid);
        if (info != null) {
            map.put("result", "success");
        } else {
            map.put("result", "error");
        }
        return AppUtil.returnObject(new PageData(), map);
    }
    /**
     * 开始实时广播任务，发送广播命令，向广播任务列表中添加广播任务
     * @param terips 需要广播终端的ip组 ，格式如 "1001,1002,1003"
     * @param domids 需要广播的分组的id，格式同上
     * @param vol 广播音量
     * @return taskid
     * @throws Exception
     */
	/*public int startRealTimeCastTask(String terids,String domids,int vol,int castLevel) throws Exception{
		String[] domidlist = domids==null?null:domids.split(",");
		List<DomainInfo> domaininfolist = new LinkedList<>();
		int terrecport = Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "terRecPort"));
		int taskid = CastTaskInfo.getTaskid();
		for(String domid:domidlist){//为每个分组开一个组播广播文件线程
			if(domid.length()>1){//给二级分组
				String multicastaddress = GlobalInfoController.getMulticastAddress();
				int multicastport = GlobalInfoController.getMulticastPort();
				List<String> teridlist = new ArrayList<>();
				//向分组中每个选择终端发送文件广播命令
				List<PageData> terlist =terminalsService.listAllTerByDomainId(domid);
				for(PageData terpd:terlist){
					String terid = terpd.get("TIDString").toString();
					if(terids.contains(terid)){
						String terip = terpd.get("TIP").toString(); 
						sendRealTimeCastCMD(GlobalInfoController.getNetheartsrt(),multicastaddress,multicastport,terip,terrecport,vol);
						teridlist.add(terid);
					}
				}
				//组播
				if(teridlist.size()>0){
					//判断该分组目前是否有任务，当前优先级更高才会进行广播
					if(GlobalInfoController.getEnableCastByDomid(domid,teridlist,castLevel)){
						DomainInfo dominfo = sendRealTimeCast(taskid, multicastaddress, multicastport,domid,castLevel, teridlist);
						domaininfolist.add(dominfo);
					}
				}
			}
		}
		CastTaskInfo castTaskinfo = new CastTaskInfo(taskid,2, true, castLevel, domaininfolist);
		GlobalInfoController.addCastTaskTolist(castTaskinfo);
		return taskid;
	}
	*//**
     * 发送广播命令
     * @param netheartsrt 心跳检测与命令发送线程
     * @param multicastaddress 广播组播地址
     * @param multicastrecport 组播接收端口
     * @param targetIP 广播命令单播接收终端地址
     * @param targetPort 接收端口
     * @param vol 文件广播音量
     * @throws InterruptedException
     *//*
	private static void sendRealTimeCastCMD(UnicastThread netheartsrt,String multicastaddress,int multicastrecport,String targetIP,int targetPort,int vol) {
		List<String> types = new ArrayList<String>();
		types.add(CastTaskInfo.types[0]);
		byte[] senddata = InterCMDProcess.sendCast(true,multicastaddress, multicastrecport,vol,types);
		if(senddata!=null){
			sendCastCMD(netheartsrt,targetIP,targetPort,vol,senddata);
			//当终端没有返回消息时应该重复发送，另开一个线程
			//			Thread.sleep(1000);
			//			if(!GlobalInfoController.isTerCastCMDReturn(targetIP)){
			//				sendCastCMD(netheartsrt,targetIP,targetPort,vol,senddata);
			//			}
		}else{
			System.out.println("组播ip解析出错");
		}


	}
	private static void sendCastCMD(UnicastThread netheartsrt,String targetIP,int targetPort,int vol,byte[] senddata){
		System.out.println("发送文件组播命令");
		netheartsrt.sendCMDInfo(targetIP, targetPort, senddata);//发送组播命令
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] senddata1 = InterCMDProcess.sendVolSet(vol,false);
		netheartsrt.sendCMDInfo(targetIP, targetPort, senddata1);//网络调音
	}
*/
}
