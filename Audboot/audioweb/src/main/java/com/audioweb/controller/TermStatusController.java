package com.audioweb.controller;

import com.audioweb.entity.Page;
import com.audioweb.entity.Terminals;
import com.audioweb.service.DomainsManager;
import com.audioweb.service.QuartzManager;
import com.audioweb.service.TerminalsManager;
import com.audioweb.util.AppUtil;
import com.audioweb.util.Const;
import com.audioweb.util.Jurisdiction;
import com.audioweb.util.PageData;
import com.niocast.cast.InterCMDProcess;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;
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
@RequestMapping("/termstatus")
public class TermStatusController extends BaseController {
    private static final String TermManager = "终端运行控制";
    @Resource(name = "DomainsService")
    private DomainsManager DomainsService;
    @Resource(name = "TerminalsService")
    private TerminalsManager TerminalsService;
    @Resource(name = "quartzService")
    private QuartzManager quartzService;


    /**
     * 查看终端状态列表
     *
     * @return
     */
    @RequestMapping("/listTermStatus")
    public ModelAndView list(Page page) throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("termstatus/listTermStatus.do")) {
            return logout();
        } // 权限校验
        PageData pd = new PageData();
        pd = this.getPageData();
        String msg = (null == pd.get("MSG") || "".equals(pd.get("MSG").toString())) ? "" : pd.get("MSG").toString();
        String resume = (null == pd.get("resume") || "".equals(pd.get("resume").toString())) ? "" : pd.get("resume").toString();
        String namekey = (null == pd.get("namekey") || "".equals(pd.get("namekey").toString())) ? "" : pd.get("namekey").toString();
        Map<String, TerminalInfo> terminalInfos = GlobalInfoController.getTerinfoMap();
        List<String> tidlist = new ArrayList<String>();
        List<Terminals> termlist = new ArrayList<Terminals>();
        List<TerminalInfo> terminfos = new ArrayList<TerminalInfo>();
        //初始化
        if (msg.equals("") || msg.equals("on")) {//判断网页状态,获取对应终端信息
            for (Map.Entry<String, TerminalInfo> term : terminalInfos.entrySet()) {
                if (term.getValue().getIsOnline() || term.getValue().getIstrueOnline()) {
                    if (!namekey.equals("")) {
                        if (term.getValue().getTerid().contains(namekey) || term.getValue().getTname().contains(namekey))
                            tidlist.add(term.getValue().getTerid());
                    } else {
                        tidlist.add(term.getValue().getTerid());
                    }
                }
            }
            pd.put("MSG", "on");
            mv.addObject("MSG", "on");
        } else if (msg.equals("off")) {
            for (Map.Entry<String, TerminalInfo> term : terminalInfos.entrySet()) {
                if (term.getValue().getIsOnline() || term.getValue().getIstrueOnline())
                    tidlist.add(term.getValue().getTerid());
            }
            mv.addObject("MSG", msg);
            List<Terminals> dTerminals = TerminalsService.listAllTerm(page);
            for (int i = dTerminals.size() - 1; i >= 0; i--) {
                for (int j = tidlist.size() - 1; j >= 0; j--) {
                    if (tidlist.get(j).equals(dTerminals.get(i).getTIDString())) {
                        dTerminals.remove(i);
                        break;
                    }
                }
            }
            tidlist = new ArrayList<String>();
            for (Terminals terminals : dTerminals) {
                if (!namekey.equals("")) {
                    if (terminals.getTIDString().contains(namekey) || terminals.getTName().contains(namekey))
                        tidlist.add(terminals.getTIDString());
                } else {
                    tidlist.add(terminals.getTIDString());
                }
            }
        } else if (msg.equals("all") && namekey.equals("")) {
            mv.addObject("MSG", msg);
            tidlist = new ArrayList<String>();
        } else if (msg.equals("all") && !namekey.equals("")) {
            List<Terminals> dTerminals = TerminalsService.listAllTerm(page);
            mv.addObject("MSG", msg);
            tidlist = new ArrayList<String>();
            for (Terminals terminals : dTerminals) {
                if (terminals.getTIDString().contains(namekey) || terminals.getTName().contains(namekey))
                    tidlist.add(terminals.getTIDString());
            }
        }
        pd.put("termlist", tidlist);
        page.setPd(pd);
        termlist = TerminalsService.listAllTerByTidPage(page);
        for (int i = termlist.size() - 1; i >= 0; i--) {
            for (Map.Entry<String, TerminalInfo> tInfo : terminalInfos.entrySet()) {
                if (termlist.get(i).getTIDString().equals(tInfo.getKey())) {
                    terminfos.add(tInfo.getValue());
                    break;
                }
            }
        }
        if (resume != null && !resume.equals("")) {
            quartzService.resumeRefreshTask();
        }
        Boolean refresh = quartzService.getRefreshTask() && (System.currentTimeMillis() - GlobalInfoController.getDate()) > 60000;
        mv.addObject("terminfos", terminfos);
        mv.addObject("refresh", refresh);
        mv.addObject("pd", pd);
        mv.addObject("time", GlobalInfoController.getDate());
        mv.addObject("namekey", namekey);
        mv.addObject("user", Jurisdiction.getUserid());
        mv.setViewName("termstatus/termstatus");
        return mv;
    }

    /**
     * 重启终端
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/setTer")
    @ResponseBody
    public Object setTer() throws Exception {
        logBefore(Jurisdiction.getUsername() + "重启终端");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String returndata = "error";
        try {
            String Tid = (null == pd.get("Tid") || "".equals(pd.get("Tid").toString())) ? "" : pd.get("Tid").toString();
            if (!"".equals(Tid)) {
                TerminalInfo tInfo = GlobalInfoController.getTerminalInfo(Tid);
                GlobalInfoController.SendData(InterCMDProcess.sendTerReboot(), tInfo.getSession(), 0);
                returndata = "success";
                saveLog(Const.LOGTYPE[1], TermManager, "终端重启", this.getRemortIP(), pd.get("Tid").toString());
            }
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
        }
        map.put("result", returndata);
        return AppUtil.returnObject(new PageData(), map);
    }
    /**
     * 重启全部终端
     *
     * @param term
     * @param model
     * @return
     */
/*	@RequestMapping(value = "/Reboot")
	@ResponseBody
	public Object Reboot() throws Exception {
		logBefore(Jurisdiction.getUsername() + "重启全部终端");
		Map<String,String> map = new HashMap<String,String>();
		PageData pd = new PageData();
		pd = this.getPageData();
		String returndata = "error";
		try {
			if(Jurisdiction.getUserid().equals("1")) {
			GlobalInfoController.AllTerReboot();
			returndata = "success";
			saveLog(Const.LOGTYPE[1], TermManager, "全部终端重启", this.getRemortIP(),"");
			}
		} catch (Exception e) {
			// TODO: handle exception
			logError(e);
		}
		map.put("result", returndata);
		return AppUtil.returnObject(pd, map);
	}*/

    /**
     * 中断全部任务
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/delTask")
    @ResponseBody
    public Object delTask() throws Exception {
        logBefore(Jurisdiction.getUsername() + "中断终端全部任务");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String returndata = "error";
        try {
            String Tid = (null == pd.get("Tid") || "".equals(pd.get("Tid").toString())) ? "" : pd.get("Tid").toString();
            if (!"".equals(Tid)) {
                TerminalInfo tInfo = GlobalInfoController.getTerminalInfo(Tid);
                if (tInfo.getOrderCastInfo().size() > 0) {
                    GlobalInfoController.EndCast(tInfo);
                    synchronized (tInfo.getOrderCastInfo()) {
                        tInfo.getOrderCastInfo().clear();
                    }
                    returndata = "success";
                    saveLog(Const.LOGTYPE[1], TermManager, "中断终端全部任务", this.getRemortIP(), pd.get("Tid").toString());
                } else {
                    returndata = "nothing";
                }
                //GlobalInfoController.SendData(InterCMDProcess.sendTerReboot(), tInfo.getSession(), 0);
            }
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
        }
        map.put("result", returndata);
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 获取设置音量页面
     *
     * @return
     */
    @RequestMapping("/toVol")
    public ModelAndView toVol() throws Exception {
        ModelAndView mv = this.getModelAndView();
        mv.setViewName("termstatus/setvol");
        return mv;
    }

    /**
     * 重启终端
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/setVol")
    @ResponseBody
    public Object setVol() throws Exception {
        logBefore(Jurisdiction.getUsername() + "设置音量");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String returndata = "error";
        try {
            String is = (null == pd.get("is") || "".equals(pd.get("is").toString())) ? "" : pd.get("is").toString();//音量是否固化
            String tids = (null == pd.get("tids") || "".equals(pd.get("tids").toString())) ? "" : pd.get("tids").toString();//选中终端
            String vol = (null == pd.get("vol") || "".equals(pd.get("vol").toString())) ? "" : pd.get("vol").toString();//音量值
            if (!"".equals(tids) && !"".equals(is) && !"".equals(vol)) {
                String[] TIDStrings = tids.split(",");
                int volume = Integer.parseInt(vol);
                boolean issave = false;
                if (is.equals("true")) issave = true;
                GlobalInfoController.setTerminalInfoVol(TIDStrings, volume, issave);//给终端发送音量信息
                returndata = "success";
                saveLog(Const.LOGTYPE[1], TermManager, "设置终端音量", this.getRemortIP(), pd.get("tids").toString());
            }
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
        }
        map.put("result", returndata);
        return AppUtil.returnObject(new PageData(), map);
    }
}
