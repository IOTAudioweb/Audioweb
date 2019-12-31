package com.audioweb.controller;

import com.audioweb.service.DomainsManager;
import com.audioweb.service.TerminalsManager;
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
import com.niocast.websocket.WebStreamHandler;
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

/**
 * @author Shuofang
 * TODO
 */
@Controller
@RequestMapping("/taskmanage")
public class TaskManageController extends BaseController {
    private static final String FUNCTION = "广播任务管理";
    @Resource(name = "DomainsService")
    private DomainsManager domainsService;
    @Resource(name = "TerminalsService")
    private TerminalsManager terminalsService;

    /**
     * 查看广播任务列表
     *
     * @return
     */
    @RequestMapping("/listTask")
    public ModelAndView list() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("taskmanage/listTask.do")) {
            return logout();
        } // 权限校验
        PageData pd = new PageData();
        pd = this.getPageData();
        List<CastTaskInfo> tasklist = GlobalInfoController.getCastTasklistInfos();
        mv.addObject("tasklist", tasklist);
        mv.addObject("pd", pd);
        mv.setViewName("taskmanage/taskmanage");
        return mv;
    }

    /**
     * 停止广播
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/stopTask")
    @ResponseBody
    public Object stopTask() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String taskid = pd.getString("taskid");
        if (taskid != null && !"".equals(taskid)) {
            CastTaskInfo info = GlobalInfoController.getCastTaskInfo(Integer.parseInt(taskid));
            if (info != null)
                if (Const.CASTTYPE[6].equals(info.getMultiCastType())) {//停止控件采播后发送命令
                    String userid = info.getUserid();
                    if (userid != null) {
                        QtClientInfo qinfo = GlobalInfoController.getClientInfo(userid);
                        if (qinfo != null) {
                            GlobalInfoController.SendData(DefaultCommand.StopCast.getBytes(DefaultCommand.UTF_8), qinfo.getSession(), 0);
                        }
                    }
                }
            MulticastThread mct = GlobalInfoController.stopCastTaskInList(Integer.parseInt(taskid));
            if (mct != null) {
                WebStreamHandler.stopSocketByTaskid(mct);//实时采播断开连接
            }
            saveLog(Const.LOGTYPE[1], FUNCTION, "停止广播任务", this.getRemortIP(), taskid);
            map.put("result", "success");
        } else {
            map.put("result", "error");
        }
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 停止广播
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/stopAllTask")
    @ResponseBody
    public Object stopAllTast() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        try {
            GlobalInfoController.stopAllTask();
            map.put("result", "success");
            saveLog(Const.LOGTYPE[1], FUNCTION, "停止全部广播任务", this.getRemortIP(), null);
        } catch (Exception e) {
            logError(e);
            map.put("result", "error");
            // TODO: handle exception
        }
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 分区列表
     *
     * @return
     */
    @RequestMapping("/domids")
    public ModelAndView domids() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        List<String> alldomidsInfo = new ArrayList<String>();
        List<String> alltidsInfo = new ArrayList<String>();
        String taskId = pd.getString("taskid");
        List<CastTaskInfo> tasklist = GlobalInfoController.getCastTasklistInfos();
        for (CastTaskInfo cInfo : tasklist) {
            if (Integer.toString(cInfo.getTaskid()).equals(taskId)) {
                List<TerminalInfo> tInfos = cInfo.getCastTeridlist();
                if (tInfos != null)
                    for (TerminalInfo tInfo : tInfos) {
                        alltidsInfo.add(tInfo.getTerid());
                    }
                TerminalInfo tInfo = cInfo.getMainTerm();
                if (tInfo != null) {
                    alltidsInfo.add(tInfo.getTerid());
                }
                List<String> domainlist = cInfo.getDomainidlist();
                if (domainlist != null)
                    alldomidsInfo.addAll(cInfo.getDomainidlist());
                break;
            }
        }
        JSONArray domarr = JSONArray.fromObject(domainsService.listAllDomains(""));// 所有分组
        JSONArray lastarr = new JSONArray();
        for (int i = 0; i < domarr.size(); i++) {// 设置jsonobject类型：0表示分组，1表示终端
            JSONObject obj = domarr.getJSONObject(i);
            String id = obj.getString("domainId");
            for (int j = 0; j < alldomidsInfo.size(); j++) {
                if (id.equals(alldomidsInfo.get(j))) {
                    obj.element("checked", true);
                    alldomidsInfo.remove(j);
                    break;
                }
            }
			/*if(id.length() <= 2) {
				obj.element("open", true);
			}*/
            obj.element("type", "0");
            obj.element("isParent", true);
            lastarr.add(obj);
        }

        List<PageData> termList = terminalsService.listAllTer("");// 所有终端
        JSONArray terarr = JSONArray.fromObject(termList);// 所有终端列表
        String terjson = terarr.toString().replaceAll("DomainId", "pId").replaceAll("TIDString", "tid")
                .replaceAll("TName", "name");
        terarr = JSONArray.fromObject(terjson);
        for (int i = 0; i < terarr.size(); i++) {
            JSONObject obj = terarr.getJSONObject(i);
            String tid = obj.getString("tid");
            for (String tidstr : alltidsInfo) {
                if (tid.equals(tidstr)) {
                    obj.element("checked", true);
                    alltidsInfo.remove(tid);
                    break;
                }
            }
            obj.element("type", "1");
            lastarr.add(obj);
        }
        String lastjson = lastarr.toString().replaceAll("parentDomainId", "pId").replaceAll("domainId", "id")
                .replaceAll("domainName", "name");
        mv.addObject("pd", pd);
        mv.addObject("zTreeNodes", lastjson);
        mv.setViewName("timedcast/finddomids");
        return mv;
    }

    /**
     * 查看广播任务列表
     *
     * @return
     */
    @RequestMapping("/taskcontroller")
    public ModelAndView castcontroller() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("taskmanage/listTask.do")) {
            return logout();
        } // 权限校验
        PageData pd = new PageData();
        pd = this.getPageData();
        String taskid = (null == pd.get("taskid") || "".equals(pd.get("taskid").toString())) ? "" : pd.get("taskid").toString();
        if (null != taskid && !"".equals(taskid)) {
            CastTaskInfo cInfo = GlobalInfoController.getCastTaskInfo(Integer.parseInt(taskid));
            mv.addObject("task", cInfo);
            mv.addObject("pd", pd);
            mv.setViewName("taskmanage/taskmusic");
            return mv;
        }
        return null;
    }

    /**
     * 查看广播任务列表
     *
     * @return
     */
    @RequestMapping("/toplay")
    @ResponseBody
    public Object toplay() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        Map<String, String> map = new HashMap<String, String>();
        pd = this.getPageData();
        String taskid = (null == pd.get("taskid") || "".equals(pd.get("taskid").toString())) ? "" : pd.get("taskid").toString();
        String commd = (null == pd.get("commd") || "".equals(pd.get("commd").toString())) ? "" : pd.get("commd").toString();//返回指令
        String renum = (null == pd.get("renum") || "".equals(pd.get("renum").toString())) ? "" : pd.get("renum").toString();//调节时间或者音量
        String statu = (null == pd.get("statu") || "".equals(pd.get("statu").toString())) ? "" : pd.get("statu").toString();//状态使能
        if (null != taskid && !"".equals(taskid)) {
            //控制部分
            int playtime = 0;//返回音频播放时间
            CastTaskInfo cInfo = GlobalInfoController.getCastTaskInfo(Integer.parseInt(taskid));
            if (cInfo != null) {
                try {
                    MulticastThread mct = cInfo.getMct();
                    if (!commd.equals("") && mct != null) {
                        switch (commd) {//控制指令
                            case "0":
                                cInfo.setIsStop(true);//暂停播放
                                map.put("result", "success");
                                logger.debug("暂停广播:" + cInfo.getTaskName());
                                return AppUtil.returnObject(new PageData(), map);
                            case "1":
                                cInfo.setIsStop(false);//播放
                                map.put("result", "success");
                                logger.debug("开启广播:" + cInfo.getTaskName());
                                return AppUtil.returnObject(new PageData(), map);
                            case "2":
                                mct.moveFileCast(true, false);//上一曲
                                logger.debug("上一曲:" + cInfo.getFilelist().get(mct.getFilelistin() - 1));
                                break;
                            case "3":
                                mct.moveFileCast(true, true);//下一曲
                                logger.debug("下一曲:" + cInfo.getFilelist().get(mct.getFilelistin() - 1));
                                break;
                            case "4":
                                if (!renum.equals("") && mct != null && mct.getIsopen()) {
                                    cInfo.setVol(Integer.parseInt(renum));//音量控制
                                    map.put("result", "success");
                                    logger.debug("音量控制:" + renum + "	任务:" + cInfo.getTaskName());
                                    return AppUtil.returnObject(new PageData(), map);
                                }
                                map.put("result", "error");
                                return AppUtil.returnObject(new PageData(), map);
                            case "5"://播放模式修改
                                if (!renum.equals("") && Integer.parseInt(renum) < 4) {
                                    cInfo.setTasktype(Integer.parseInt(renum));
                                    map.put("result", "success");
                                    return AppUtil.returnObject(new PageData(), map);
                                }
                                map.put("result", "error");
                                return AppUtil.returnObject(new PageData(), map);
                            default:
                                break;
                        }
                    } else if (!renum.equals("") && mct != null) {//音频位置调节
                        double time = Double.parseDouble(renum);
                        mct.castPosition(2, time);
                        playtime = (int) time;//直接获取音频时间，不再读取后台
                        map.put("result", "success");
                        logger.debug("调节音频:" + renum + "	任务:" + cInfo.getTaskName());
                        return AppUtil.returnObject(new PageData(), map);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    logError(e);
                    map.put("result", "error");
                    return AppUtil.returnObject(new PageData(), map);
                }
            } else {
                mv.addObject("error", "end");
            }
            //返回部分
            if (statu.equals("open")) {
                mv.addObject("statu", statu);
            } else {
                mv.addObject("statu", "lock");
            }
			/*if(mct.getIn() != null && playtime == 0)
				playtime = mct.getplaytime();//后台获取音频已经播放时长
*/
            mv.addObject("time", playtime);
            mv.addObject("test", "test");
            mv.addObject("task", cInfo);
            mv.addObject("pd", pd);
            mv.setViewName("taskmanage/taskmusic");
            return mv;
        } else {
            mv.addObject("statu", "lock");
            mv.addObject("pd", pd);
            mv.addObject("error", "notaskid");
            mv.setViewName("taskmanage/taskmusic");
            return mv;
        }
    }
}
