package com.audioweb.controller;

import com.audioweb.entity.Page;
import com.audioweb.entity.TermTask;
import com.audioweb.entity.Terminals;
import com.audioweb.service.*;
import com.audioweb.service.impl.QuartzService;
import com.audioweb.util.AppUtil;
import com.audioweb.util.Const;
import com.audioweb.util.Jurisdiction;
import com.audioweb.util.PageData;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 类名称：TimedCastController
 */
@Controller
@RequestMapping("/termcast")
public class TermCastController extends BaseController {
    private static final String TermTaskManager = "终端采播任务管理";
    @Resource(name = "termTaskService")
    private TermTaskManager termTaskService;
    @Resource(name = "quartzService")
    private QuartzManager quartzService;
    @Resource(name = "systemService")
    private SystemManager systemService;
    @Resource(name = "DomainsService")
    private DomainsManager domainsService;
    @Resource(name = "TerminalsService")
    private TerminalsManager terminalsService;

    /**
     * 查询所有任务
     *
     * @param @throws Exception
     * @return ModelAndView
     */
    @RequestMapping(value = "/listTermTasks")
    public ModelAndView listTermTasks(Page page) throws Exception {
        if (!Jurisdiction.hasJurisdiction("termcast/listTermTasks.do")) {
            return logout();
        } // 权限校验
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        page.setPd(pd);
        List<TermTask> termTaskslist = termTaskService.getTermTask(page);// 用户列表
        for (TermTask task : termTaskslist) {
            TerminalInfo terminalInfo = null;
            if ((terminalInfo = GlobalInfoController.getTerminalInfo(task.getTIDString())) != null) {
                task.setIsOnline(terminalInfo.getIsOnline() || terminalInfo.getIstrueOnline());
            }
            Terminals terminals = terminalsService.getTermByTid(task.getTIDString());
            if (terminals != null) {
                task.setIsJuris(terminals.getISAutoCast());
            }
        }
        mv.addObject("taskList", termTaskslist);
        mv.addObject("pd", pd);
        mv.setViewName("termcast/term_task");
        return mv;
    }

    /**
     * 修改任务是否可用
     *
     * @param @return
     * @param @throws Exception
     * @return Object
     * @Title: upTaskEnable
     * @Description: TODO
     */
    @RequestMapping("/upTaskEnable")
    @ResponseBody
    public Object upTaskEnable() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String result = "false";
        Boolean isenabled = Boolean.parseBoolean(pd.get("Status").toString());
        try {
            if (isenabled) {
                quartzService.resumeTrigger(pd.getString("TaskId"), QuartzService.JobTermGroup);
            } else {
                quartzService.pauseTrigger(pd.getString("TaskId"), QuartzService.JobTermGroup);
            }
            pd.put("isuse", isenabled ? "1" : "0");
            termTaskService.editTermTaskStatus(pd);
            result = "success";
        } catch (Exception e) {
            logError(e);
        }
        // 插入日志
        saveLog(Const.LOGTYPE[1], TermTaskManager, "状态修改", this.getRemortIP(),
                "采播任务ID" + pd.getString("TaskId") + ":" + pd.getString("isuse"));
        map.put("result", result);
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 请求新增任务页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() throws Exception {
        ModelAndView mv = this.getModelAndView();
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            String tid = (null == pd.get("TIDString") || "".equals(pd.get("TIDString").toString())) ? "" : pd.get("TIDString").toString();
            int id = Integer.parseInt(termTaskService.findMaxTaskId(pd) != null ? termTaskService.findMaxTaskId(pd) : "0") + 1;
            pd.put("TaskId", id);
            mv.addObject("pd", pd);
            if (tid != null) {
                mv.addObject("tid", tid); //给定id
            }
            mv.addObject("MSG", "add"); // 执行状态 add 为添加
            mv.setViewName("termcast/task_edit");
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return mv;
    }

    /**
     * 添加任务信息
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/add")
    public ModelAndView add(TermTask task) throws Exception {
        logBefore(Jurisdiction.getUsername() + "添加任务");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pdChange(pd);
        try {
            termTaskService.addTermTask(pd); // 保存任务
            quartzService.addJob(pd);// 创建对应定时器
            // 插入日志
            saveLog(Const.LOGTYPE[1], TermTaskManager, "新增", this.getRemortIP(), task.getTaskName());
        } catch (Exception e) {
            logError(e);
            mv.addObject("msg", "failed");
        }
        mv.setViewName("redirect:listTermTasks.do"); // 保存成功跳转到列表页面
        return mv;
    }

    /**
     * 格式化添加页面传来的值
     *
     * @param model
     * @return
     */
    private void pdChange(PageData pd) {
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (pd.getString("Status") != null) {
            pd.put("Status", 1);
        } else {
            pd.put("Status", 0);
        }
        if (pd.getString("isLooping") != null) {
            pd.put("isLooping", 1);
        } else {
            pd.put("isLooping", 0);
        }
        String weeks;
        if (pd.getString("0").equals("1")) {
            pd.put("LastingSeconds", -1);
            pd.put("ExecTime", "00:00:00");
            pd.put("Statu", 1);
            weeks = "1";
        } else {
            weeks = "0";
        }
        for (int i = 1; i < 8; i++) {
            if (pd.getString(String.valueOf(i)) != null) {
                weeks = weeks + 1;
            } else {
                weeks = weeks + 0;
            }
        }
        pd.put("Weeks", weeks);
        pd.put("upDateTime", df.format(day));
    }

    /**
     * 请求修改任务页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit() throws Exception {
        ModelAndView mv = this.getModelAndView();
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            TermTask termTask = termTaskService.getTaskByTaskId(pd);
            mv.addObject("pd", pd);
            mv.addObject("task", termTask);
            mv.addObject("MSG", "edit"); // 执行状态
            mv.setViewName("termcast/task_edit");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 修改任务信息
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/edit")
    public ModelAndView edit(TermTask task) throws Exception {
        logBefore(Jurisdiction.getUsername() + "编辑任务");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pdChange(pd);// 格式化pd中的值
        try {
            termTaskService.editTermTask(pd); // 保存任务
            pd.put("JobGroup", QuartzService.JobTermGroup);
            pd.put("ScheId", QuartzService.JobTermGroup);
            quartzService.editScheTask(pd); // 更新定时器信息
            // 插入日志
            saveLog(Const.LOGTYPE[1], TermTaskManager, "修改", this.getRemortIP(), task.getTaskName());
        } catch (Exception e) {
            logError(e);
            mv.addObject("msg", "failed");
        }
        mv.setViewName("redirect:listTermTasks.do"); // 保存成功跳转到列表页面
        return mv;
    }

    /**
     * 请求读取任务页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toRead")
    public ModelAndView toRead() throws Exception {
        ModelAndView mv = this.getModelAndView();
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            TermTask termTask = termTaskService.getTaskByTaskId(pd);
            mv.addObject("pd", pd);
            mv.addObject("task", termTask);
            mv.addObject("MSG", "read"); // 执行状态
            mv.setViewName("termcast/task_edit");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 立即执行任务
     *
     * @throws Exception
     */
    @RequestMapping(value = "/runjob")
    @ResponseBody
    public Object runjob() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        boolean isskip = (null == pd.get("isskip") || "".equals(pd.get("isskip").toString())) ? false : true;
        TermTask task = termTaskService.getTaskByTaskId(pd);
        if (isskip)
            logBefore(Jurisdiction.getUsername() + "提前执行任务");
        else
            logBefore(Jurisdiction.getUsername() + "立即执行任务");
        String errInfo = "false";
        TerminalInfo tInfo = GlobalInfoController.getTerminalInfo(task.getTIDString());
        if (task != null && null != tInfo && (tInfo.getIsOnline() || tInfo.getIstrueOnline())) {
            Terminals terminals = terminalsService.getTermByTid(task.getTIDString());
            if (terminals != null && terminals.getISAutoCast()) {
                try {
                    quartzService.runjob(pd.getString("TaskId") + ":" + pd.getString("TaskName"), QuartzService.JobTermGroup, isskip);
                    errInfo = "success";
                    // 插入日志
                    if (isskip)
                        saveLog(Const.LOGTYPE[1], TermTaskManager, "提前执行任务", this.getRemortIP(),
                                pd.getString("TaskId") + ":" + pd.getString("TaskName"));
                    else
                        saveLog(Const.LOGTYPE[1], TermTaskManager, "立即执行任务", this.getRemortIP(),
                                pd.getString("TaskId") + ":" + pd.getString("TaskName"));
                } catch (Exception e) {
                    logger.error(e.toString(), e);
                }
            }
        }
        if (errInfo.equals("false")) {
            saveLog(Const.LOGTYPE[1], TermTaskManager, "执行采播任务失败", this.getRemortIP(),
                    pd.getString("TaskId") + ":" + pd.getString("TaskName"));
        }
        map.put("result", errInfo);
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 删除任务
     *
     * @throws Exception
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete() throws Exception {
        logBefore(Jurisdiction.getUsername() + "删除任务");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String errInfo = "false";
        try {
            quartzService.deleteTrigger(pd.getString("TaskId"), QuartzService.JobTermGroup);
            quartzService.deleteJob(pd.getString("TaskId") + ":" + pd.getString("TaskName"), QuartzService.JobTermGroup);
            termTaskService.deleteTermTask(pd);
            errInfo = "success";
            // 插入日志
            saveLog(Const.LOGTYPE[1], TermTaskManager, "删除", this.getRemortIP(),
                    pd.get("TaskId").toString());
        } catch (Exception e) {
            logError(e);
        }
        map.put("result", errInfo);
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 批量删除
     *
     * @throws Exception
     */
    @RequestMapping(value = "/deleteAllO")
    @ResponseBody
    public Object deleteAllO() throws Exception {
        logBefore(Jurisdiction.getUsername() + "批量删除任务");
        PageData pd = new PageData();
        Map<String, Object> map = new HashMap<String, Object>();
        List<PageData> pdList = new ArrayList<PageData>();
        pd = this.getPageData();
        String tids = pd.getString("TaskIds");
        String TaskNames = pd.getString("TaskName");
        if (null != tids && !"".equals(tids)) {
            String Arraytids[] = tids.split(",");
            String Arraytnames[] = TaskNames.split(",");
            for (int i = 0; i < Arraytids.length; i++) {
                quartzService.deleteTrigger(Arraytids[i], QuartzService.JobTermGroup);
                quartzService.deleteJob(Arraytids[i] + ":" + Arraytnames[i], QuartzService.JobTermGroup);
            }
            termTaskService.deleteAllO(Arraytids);
            pd.put("msg", "ok");
            // 插入日志
            saveLog(Const.LOGTYPE[1], TermTaskManager, "批量删除", this.getRemortIP(), tids.length() < 240 ? tids : "");
        } else {
            pd.put("msg", "no");
        }
        pdList.add(pd);
        map.put("list", pdList);
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 分区列表
     *
     * @return
     */
    @RequestMapping("/finddomids")
    public ModelAndView list() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        String tids = (null == pd.get("domidInfo") || "".equals(pd.get("domidInfo").toString())) ? ""
                : pd.get("domidInfo").toString();
        JSONArray domarr = JSONArray.fromObject(domainsService.listAllDomains(""));// 所有分组
        JSONArray lastarr = new JSONArray();
        for (int i = 0; i < domarr.size(); i++) {// 设置jsonobject类型：0表示分组，1表示终端
            JSONObject obj = domarr.getJSONObject(i);
            //obj.element("open", true);
            obj.element("type", "0");
            obj.element("isParent", true);
            obj.element("chkDisabled", true);
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
            if (tid.equals(tids)) {
                obj.element("checked", true);
            }
            obj.element("type", "1");
            lastarr.add(obj);
        }
        String lastjson = lastarr.toString().replaceAll("parentDomainId", "pId").replaceAll("domainId", "id")
                .replaceAll("domainName", "name");
        mv.addObject("pd", pd);
        mv.addObject("zTreeNodes", lastjson);
        mv.setViewName("termcast/finddomids");
        return mv;
    }
}
