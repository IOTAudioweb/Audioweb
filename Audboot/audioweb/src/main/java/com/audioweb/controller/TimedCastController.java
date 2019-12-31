package com.audioweb.controller;

import com.audioweb.entity.Page;
import com.audioweb.entity.ScheTask;
import com.audioweb.entity.Schedules;
import com.audioweb.service.*;
import com.audioweb.service.impl.QuartzService;
import com.audioweb.util.*;
import com.niocast.util.GlobalInfoController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 类名称：TimedCastController
 */
@Controller
@RequestMapping("/timedcast")
public class TimedCastController extends BaseController {
    private static final String ScheTaskManager = "定时任务管理";
    private static final String SchedulesManager = "定时方案管理";
    @Resource(name = "scheduleService")
    private SchedulesManager scheduleService;
    @Resource(name = "scheTaskService")
    private ScheTaskManager scheTaskService;
    @Resource(name = "quartzService")
    private QuartzManager quartzService;
    @Resource(name = "systemService")
    private SystemManager systemService;
    @Resource(name = "DomainsService")
    private DomainsManager domainsService;
    @Resource(name = "TerminalsService")
    private TerminalsManager terminalsService;

    /**
     * 查询所有方案
     *
     * @param @throws Exception
     * @return ModelAndView
     */
    @RequestMapping(value = "/listSchedules")
    public ModelAndView listSchedules(Page page) throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("timedcast/listSchedules.do")) {
            return logout();
        } // 校验权限
        PageData pd = new PageData();
        pd = this.getPageData();
        page.setPd(pd);
        List<Schedules> scheduleslist = scheduleService.listSchedules(page);// 方案列表
        mv.addObject("schelist", scheduleslist);
        mv.addObject("pd", pd);
        mv.setViewName("timedcast/time_program");
        return mv;
    }

    /**
     * 修改方案是否可用
     *
     * @param @return
     * @param @throws Exception
     * @return Object
     * @Title: upEnable
     * @Description: TODO
     */
    @RequestMapping("/upEnable")
    @ResponseBody
    public Object upEnable() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String result = "false";
        try {
            String ScheId = pd.getString("ScheId");
            String checked = pd.getString("checked");
            if (ScheId != null && !ScheId.equals("") && checked != null && !checked.equals("")) {
                pd.put("isuse", "true".equals(checked));
                quartzService.editSchedule(pd);
                scheduleService.editUsingSchedule(pd);// 执行修改
                saveLog(Const.LOGTYPE[1], SchedulesManager, "修改", this.getRemortIP(), pd.getString("ScheId") + ":" + pd.getString("checked"));
                result = "success";
            }
            // 插入日志
        } catch (Exception e) {
            logError(e);
        }
        map.put("result", result);
        return map;
    }

    /**
     * 去添加方案
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addSchedule")
    public ModelAndView addSchedule() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        mv.setViewName("timedcast/program_edit");
        mv.addObject("pd", pd);
        mv.addObject("MSG", "addSche");
        return mv;
    }

    /**
     * 添加方案
     */
    @RequestMapping(value = "/addSche")
    @ResponseBody
    public Object addSche() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        int newScheId = scheduleService.findMaxScheId(pd) == null ? 1 : (Integer.parseInt(scheduleService.findMaxScheId(pd)) + 1);
        pd.put("ScheId", newScheId);
        pd.put("IsExecSchd", true);
        scheduleService.addSchedule(pd); // 执行修改
        // 插入日志
        saveLog(Const.LOGTYPE[1], SchedulesManager, "添加", this.getRemortIP(), pd.getString("ScheName"));

        map.put("result", "success");
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 添加方案
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @RequestMapping(value = "/addScheExcel", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Object addScheExcel(@RequestParam(value = "file") MultipartFile file) {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = this.getPageData();
        int newScheId;
        if (null != file && !file.isEmpty()) {
            try {
                String[] files = file.getOriginalFilename().split("\\.");
                newScheId = scheduleService.findMaxScheId(pd) == null ? 1 : (Integer.parseInt(scheduleService.findMaxScheId(pd)) + 1);
                pd.put("ScheId", newScheId);
                pd.put("ScheName", files[0].equals("") ? file.getOriginalFilename() : files[0]);
                pd.put("Priority", "4");
                pd.put("IsExecSchd", false);
                scheduleService.addSchedule(pd); // 执行修改
                // 插入日志
                saveLog(Const.LOGTYPE[1], SchedulesManager, "添加", this.getRemortIP(), pd.getString("ScheName"));
            } catch (Exception e) {
                // TODO: handle exception
                logError(e);
            }
            String filePath = PathUtil.getClasspath() + Const.FILEPATHFILE + "poinfo/"; // 文件上传路径
            String fileName = FileUpload.fileUp(file, filePath, "poinfoexcel"); // 执行上传
            List<PageData> listPd = (List) ObjectExcelRead.readExcel(filePath, fileName, 1, 0, 0); // 执行读EXCEL操作,读出的数据导入List
            // 1:从第2行开始；0:从第A列开始；0:第0个sheet
            /* 存入数据库操作====================================== */
            /**
             * var1 :标题 var2 :类型 var3 :内容
             */
            try {
                String log = "";
                String ScheId = pd.get("ScheId").toString();
                int taskId = Integer.parseInt(scheTaskService.findMaxTaskId(pd)) + 1;
                List<ScheTask> tasklist = new ArrayList<ScheTask>();
                Date day = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (int i = 0; i < listPd.size(); i++) {
                    ScheTask task = new ScheTask();
                    task.setTaskId(String.valueOf(taskId + i));
                    task.setScheId(ScheId);
                    task.setTaskName(listPd.get(i).getString("var0"));
                    task.setStatus(listPd.get(i).getString("var1").trim().equals("是") ? true : false);
                    task.setExecTime(listPd.get(i).getString("var2"));

                    if (!listPd.get(i).getString("var3").equals("") && listPd.get(i).getString("var3") != null)
                        task.setSingleDate(listPd.get(i).getString("var3"));

                    task.setLastingSeconds(listPd.get(i).getString("var4"));
                    task.setFilesInfo(listPd.get(i).getString("var5"));
                    task.setWeeks(listPd.get(i).getString("var6"));
                    task.setVols(listPd.get(i).getString("var7"));
                    task.setDomainsId(listPd.get(i).getString("var8"));

                    if (listPd.get(i).getString("var9") != "" && listPd.get(i).getString("var9") != null) {
                        task.setStartDateTime(listPd.get(i).getString("var9"));
                    } else {
                        task.setStartDateTime(df.format(day));
                    }

                    if (listPd.get(i).getString("var10") != "" && listPd.get(i).getString("var10") != null) {
                        task.setEndDateTime(listPd.get(i).getString("var10"));
                    } else {
                        task.setEndDateTime("2099-12-31 00:00:00");
                    }
                    if (listPd.get(i).getString("var11").trim().contains("顺序")) {
                        task.setTasktype(0);
                    } else if (listPd.get(i).getString("var11").trim().contains("单曲")) {
                        task.setTasktype(3);
                    } else if (listPd.get(i).getString("var11").trim().contains("随机")) {
                        task.setTasktype(2);
                    } else if (listPd.get(i).getString("var11").trim().contains("循环")) {
                        task.setTasktype(1);
                    } else {
                        task.setTasktype(0);
                    }
                    task.setNote(listPd.get(i).getString("var12"));
                    task.setUpDateTime(df.format(day));
                    formatTime(task);
                    tasklist.add(task);
                    log += task.getTaskName() + ",";
                }
                log = log.substring(0, log.length() - 1);
                scheTaskService.addListTasksFromExcel(tasklist);
                quartzService.addJobList(tasklist);
                // 插入日志
                saveLog(Const.LOGTYPE[1], ScheTaskManager, "Excel导入", this.getRemortIP(), log.length() < 240 ? log : "");
                map.put("result", "success");
                map.put("code", "0");
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                logError(e);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logError(e);
                map.put("msg", "error");
                map.put("code", "-1");
            }
        } else {
            map.put("msg", "error");
            map.put("code", "-1");
        }
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 去修改方案信息
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/editSchedule")
    public ModelAndView editSchedule() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        mv.setViewName("timedcast/program_edit");
        mv.addObject("ScheId", pd.getString("ScheId"));
        mv.addObject("ScheName", pd.getString("ScheName"));
        mv.addObject("Priority", pd.getString("Priority"));
        mv.addObject("MSG", "editSche");
        return mv;
    }

    /**
     * 修改方案
     */
    @RequestMapping(value = "/editSche")
    @ResponseBody
    public Object editSche() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        scheduleService.editSchedule(pd); // 执行修改
        String vols = (null == pd.get("Vols") || "".equals(pd.get("Vols").toString())) ? "" : pd.get("Vols").toString();
        if (!vols.equals("") && !vols.equals("0")) {
            List<ScheTask> scheTasks = scheTaskService.getScheTasksByScheId(pd);
            GlobalInfoController.putThreadintoStandbyPool(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        for (ScheTask task : scheTasks) {
                            PageData newpd = new PageData();
                            newpd.put("TaskId", task.getTaskId());
                            if (Integer.parseInt(task.getVols()) + Integer.parseInt(vols) > 0) {
                                if (Integer.parseInt(task.getVols()) + Integer.parseInt(vols) < 40) {
                                    newpd.put("Vols", Integer.parseInt(task.getVols()) + Integer.parseInt(vols));
                                } else {
                                    newpd.put("Vols", 40);
                                }
                            } else {
                                newpd.put("Vols", 0);
                            }
                            newpd.put("TaskId", task.getTaskId());
                            //newpd.put("ScheId", task.getScheId());
                            //newpd.put("TaskName", task.getTaskName());
                            //newpd.put("ScheName", task.getScheName());
                            //newpd.put("Status", task.getStatus()?1:0);
                            scheTaskService.editScheTaskVol(newpd);
                            //quartzService.editScheTask(newpd); // 更新定时器信息
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        logError(e);
                    }
                }
            });
            Thread.sleep(200);//延迟一下
        }
        // 插入日志
        saveLog(Const.LOGTYPE[1], SchedulesManager, "修改", this.getRemortIP(), pd.getString("ScheName"));
        map.put("result", "success");
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 删除方案
     *
     * @param out
     * @throws Exception
     */
    @RequestMapping(value = "/deleteSchedule")
    @ResponseBody
    public Object deleteSchedule() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        try {
            quartzService.deleteSchedule(pd);
            scheduleService.deleteSchedule(pd);
            // 插入日志
            saveLog(Const.LOGTYPE[1], SchedulesManager, "删除", this.getRemortIP(), pd.getString("ScheId"));
            map.put("result", "success");
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
            map.put("result", "error");
        }
        return AppUtil.returnObject(pd, map);
    }
    /*
     * **********************************************************************************************************************************
     * ************************************************以下是任务管理部分*****************************************************************
     * **********************************************************************************************************************************
     */
    /**
     * 显示任务列表
     *
     * @param model
     * @return
     *//*
	@RequestMapping(value = "/listSchedulesforTask")
	public ModelAndView listAllTerm(Model model, String ScheId) throws Exception {
		if (!Jurisdiction.hasJurisdiction("timedcast/listSchedulesforTask.do")) {
			return null;
		} // 权限校验
		ModelAndView mv = this.getModelAndView();
		try {
			List<Schedules> scheduleslist = scheduleService.listSchedulesforTask("");
			for (Schedules schedules : scheduleslist) {
				schedules.setTaskNum("0");
			}
			Schedules schedule = new Schedules();
			schedule.setScheId("0");
			schedule.setScheName("所有方案");
			schedule.setTaskNum("-1");// 初始化方案树的根目录
			scheduleslist.add(0, schedule);
			JSONArray arrSches = JSONArray.fromObject(scheduleslist);

			String json = arrSches.toString();
			json = json.replaceAll("taskNum", "pId").replaceAll("scheId", "id").replaceAll("scheName", "name");
			model.addAttribute("zTreeNodes", json);
			mv.addObject("ScheId", ScheId);
			mv.setViewName("timedcast/time_ztree");
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
		return mv;
	}
*/

    /**
     * 查询所有任务
     *
     * @param @throws Exception
     * @return ModelAndView
     */
    @RequestMapping(value = "/listScheTasks")
    public ModelAndView listScheTasks(Page page) throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("timedcast/listScheTasks.do")) {
            /*String userid = Jurisdiction.getUserid();*/
            return logout();
        } // 权限校验
        PageData pd = new PageData();
        pd = this.getPageData();
        String ScheId = (null == pd.get("ScheId") || "".equals(pd.get("ScheId").toString())) ? "" : pd.get("ScheId").toString();
        String ScheName = (null == pd.get("ScheName") || "".equals(pd.get("ScheName").toString())) ? "" : pd.get("ScheName").toString();
        List<Schedules> schedules = scheduleService.listUsingSchedule(pd);
        if (!"".equals(ScheId)) {

        } else if (schedules != null && schedules.size() == 1) {
            pd.put("ScheId", schedules.get(0).getScheId());
            pd.put("ScheName", schedules.get(0).getScheName());
        } else {
            pd.put("ScheId", "0");
        }
        page.setPd(pd);
        List<ScheTask> scheTaskslist = scheTaskService.getScheTaskByScheId(page);// 用户列表
        mv.addObject("taskList", scheTaskslist);
        pd.put("isuse", false);
        for (Schedules sche : schedules) {
            if (sche.getScheId().equals(pd.get("ScheId"))) {
                pd.put("isuse", true);
            }
        }
        mv.addObject("pd", pd);
        mv.setViewName("timedcast/time_task");
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
        Boolean isenabled = Boolean.parseBoolean(pd.get("Status").toString());
        String result = "false";
        try {
            if (isenabled && scheduleService.findScheduleById(pd).getIsExecSchd()) {
                quartzService.resumeTrigger(pd.getString("TaskId"), pd.getString("ScheId"));
            } else {
                quartzService.pauseTrigger(pd.getString("TaskId"), pd.getString("ScheId"));
            }
            pd.put("isuse", isenabled ? "1" : "0");
            scheTaskService.editScheTaskStatus(pd);// 执行修改
            result = "success";
        } catch (Exception e) {
            logError(e);
        }
        // 插入日志
        saveLog(Const.LOGTYPE[1], ScheTaskManager, "状态修改", this.getRemortIP(),
                "任务ID" + pd.getString("TaskId") + ":" + pd.getString("isuse"));
        map.put("result", result);
        return map;
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
        if (!Jurisdiction.hasJurisdiction("timedcast/listScheTasks.do")) {
            /*String userid = Jurisdiction.getUserid();*/
            return logout();
        } // 权限校验
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            int id = Integer.parseInt(scheTaskService.findMaxTaskId(pd) == null ? "0" : scheTaskService.findMaxTaskId(pd)) + 1;
            pd.put("TaskId", id);
            mv.addObject("pd", pd);
            mv.addObject("MSG", "add"); // 执行状态 add 为添加
            mv.setViewName("timedcast/task_edit");
        } catch (Exception e) {
            logError(e);
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
    public ModelAndView add(ScheTask task) throws Exception {
        logBefore(Jurisdiction.getUsername() + "添加任务");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pdChange(pd);
        try {
            scheTaskService.addScheTask(pd); // 保存任务
            quartzService.addJob(pd);// 创建对应定时器
            // 插入日志
            saveLog(Const.LOGTYPE[1], ScheTaskManager, "新增", this.getRemortIP(), task.getTaskName());
        } catch (Exception e) {
            logError(e);
            mv.addObject("msg", "failed");
        }
        mv.setViewName("redirect:listScheTasks.do?ScheId=" + task.getScheId() + "&ScheName=" + pd.getString("ScheName")); // 保存成功跳转到列表页面
        return mv;
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
        Date nowDate = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            ScheTask scheTask = scheTaskService.getTaskByTaskId(pd);
            scheTask.setStartDateTime(df.format(nowDate));
            if (!scheTask.getEndDateTime().isEmpty()) {
                scheTask.setEndDateTime(scheTask.getEndDateTime().substring(0, scheTask.getEndDateTime().length() - 2));
            }
            mv.addObject("pd", pd);
            mv.addObject("task", scheTask);
            mv.addObject("MSG", "edit"); // 执行状态
            mv.setViewName("timedcast/task_edit");
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
    public ModelAndView edit(ScheTask task) throws Exception {
        logBefore(Jurisdiction.getUsername() + "编辑任务");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pdChange(pd);// 格式化pd中的值
        try {
            scheTaskService.editScheTask(pd); // 保存任务
            quartzService.editScheTask(pd); // 更新定时器信息
            // 插入日志
            saveLog(Const.LOGTYPE[1], ScheTaskManager, "修改", this.getRemortIP(), task.getTaskName());
        } catch (Exception e) {
            logger.error(e.toString(), e);
            mv.addObject("msg", "failed");
        }
        mv.setViewName("redirect:listScheTasks.do?ScheId=" + task.getScheId() + "&ScheName=" + pd.getString("ScheName")); // 保存成功跳转到列表页面
        return mv;
    }

    /**
     * 请求修改任务页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toEditList")
    public ModelAndView toEditList() throws Exception {
        ModelAndView mv = this.getModelAndView();
        //Date nowDate = new Date();
        //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            String TaskId = (null == pd.get("TaskIds") || "".equals(pd.get("TaskIds").toString())) ? "" : pd.get("TaskIds").toString();
			/*ScheTask scheTask = scheTaskService.getTaskByTaskId(pd);
			scheTask.setStartDateTime(df.format(nowDate));
			if (!scheTask.getEndDateTime().isEmpty()) {
				scheTask.setEndDateTime(scheTask.getEndDateTime().substring(0, scheTask.getEndDateTime().length() - 2));
			}*/
            mv.addObject("pd", pd);
            mv.addObject("taskIds", TaskId);
            mv.addObject("MSG", "editList"); // 执行状态
            mv.setViewName("timedcast/task_edit");
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
    @RequestMapping(value = "/editList")
    public ModelAndView editList() throws Exception {
        logBefore(Jurisdiction.getUsername() + "编辑任务");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pdChange(pd);// 格式化pd中的值
        try {
            String TaskIds = (null == pd.get("TaskId") || "".equals(pd.get("TaskId").toString())) ? "" : pd.get("TaskId").toString();
            if (null != TaskIds && !"".equals(TaskIds)) {
                String Arraytaskids[] = TaskIds.split(",");
                List<String> taskids = new ArrayList<String>();
                for (int i = 0; i < Arraytaskids.length; i++) {
                    taskids.add(Arraytaskids[i]);
                }
                pd.put("taskids", taskids);
            } else {
                pd.put("taskids", null);
            }
            //pd.put("taskIds", idStrings);//添加批量ID值
            scheTaskService.editScheTaskList(pd); // 保存任务
            quartzService.editScheTaskList(pd); // 更新定时器信息
            // 插入日志
            saveLog(Const.LOGTYPE[1], ScheTaskManager, "批量修改", this.getRemortIP(), TaskIds.length() > 240 ? "" : TaskIds);
            Thread.sleep(500);
        } catch (Exception e) {
            logError(e);
            mv.addObject("msg", "failed");
        }
        mv.setViewName("redirect:listScheTasks.do?ScheId=" + pd.get("ScheId") + "&ScheName=" + pd.getString("ScheName")); // 保存成功跳转到列表页面
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
            ScheTask scheTask = scheTaskService.getTaskByTaskId(pd);
            if (!scheTask.getStartDateTime().isEmpty()) {
                scheTask.setStartDateTime(
                        scheTask.getStartDateTime().substring(0, scheTask.getStartDateTime().length() - 2));
            }
            if (!scheTask.getEndDateTime().isEmpty()) {
                scheTask.setEndDateTime(scheTask.getEndDateTime().substring(0, scheTask.getEndDateTime().length() - 2));
            }
            mv.addObject("pd", pd);
            mv.addObject("task", scheTask);
            mv.addObject("MSG", "read"); // 执行状态
            mv.setViewName("timedcast/task_edit");
        } catch (Exception e) {
            logError(e);
        }
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
		/*if (pd.getString("isLooping") != null) {
			pd.put("isLooping", 1);
		} else {
			pd.put("isLooping", 0);
		}*/
        String weeks;
        if (pd.getString("0").equals("1")) {
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
        if (pd.getString("SingleDate").length() < 2) {
            pd.put("SingleDate", null);
        }
        if (pd.getString("EndDateTime").length() < 2) {
            pd.put("EndDateTime", "2099-12-31 00:00:00");
        }
        if (pd.getString("StartDateTime").length() < 2) {
            pd.put("StartDateTime", df.format(day));
        }
        pd.put("Weeks", weeks);
        pd.put("upDateTime", df.format(day));
    }

    /**
     * 显示区域列表ztree(区域选择)
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/listSelectSchedules")
    @ResponseBody
    public Object listSelectSchedules() throws Exception {
        String json = null;
        try {
            List<Schedules> scheduleslist = scheduleService.listSchedulesforTask("");
            for (Schedules schedules : scheduleslist) {
                schedules.setTaskNum("0");
            }
            Schedules schedule = new Schedules();
            schedule.setScheId("0");
            schedule.setScheName("所有方案");
            schedule.setTaskNum("-1");// 初始化方案树的根目录
            scheduleslist.add(0, schedule);
            JSONArray arrSches = JSONArray.fromObject(scheduleslist);
            json = arrSches.toString();
            json = json.replaceAll("taskNum", "pId").replaceAll("scheId", "id").replaceAll("scheName", "name");
        } catch (Exception e) {
            logError(e);
        }
        return json;
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
        if (isskip)
            logBefore(Jurisdiction.getUsername() + "提前执行任务");
        else
            logBefore(Jurisdiction.getUsername() + "立即执行任务");
        String errInfo = "false";
        try {
            quartzService.runjob(pd.getString("TaskId") + ":" + pd.getString("TaskName"), QuartzService.JobTimeGroup, isskip);
            errInfo = "success";
            // 插入日志
            if (isskip)
                saveLog(Const.LOGTYPE[1], ScheTaskManager, "提前执行任务", this.getRemortIP(),
                        pd.getString("TaskId") + ":" + pd.getString("TaskName"));
            else
                saveLog(Const.LOGTYPE[1], ScheTaskManager, "立即执行任务", this.getRemortIP(),
                        pd.getString("TaskId") + ":" + pd.getString("TaskName"));
        } catch (Exception e) {
            logError(e);
            saveLog(Const.LOGTYPE[1], ScheTaskManager, "执行任务失败", this.getRemortIP(),
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
            quartzService.deleteTrigger(pd.getString("TaskId"), pd.getString("ScheId"));
            quartzService.deleteJob(pd.getString("TaskId") + ":" + pd.getString("TaskName"), QuartzService.JobTimeGroup);
            scheTaskService.deleteScheTask(pd);
            errInfo = "success";
            // 插入日志
            saveLog(Const.LOGTYPE[1], ScheTaskManager, "删除", this.getRemortIP(),
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
        String ScheId = pd.getString("ScheId");
        String TaskNames = pd.getString("TaskName");
        if (null != tids && !"".equals(tids)) {
            String Arraytids[] = tids.split(",");
            String Arraytnames[] = TaskNames.split(",");
            for (int i = 0; i < Arraytids.length; i++) {
                quartzService.deleteTrigger(Arraytids[i], ScheId);
                quartzService.deleteJob(Arraytids[i] + ":" + Arraytnames[i], QuartzService.JobTimeGroup);
            }
            scheTaskService.deleteAllO(Arraytids);
            pd.put("msg", "ok");
            // 插入日志
            if (tids.length() < 240) {
                saveLog(Const.LOGTYPE[1], ScheTaskManager, "批量删除", this.getRemortIP(), tids);
            } else {
                saveLog(Const.LOGTYPE[1], ScheTaskManager, "批量删除", this.getRemortIP(), "");
            }
        } else {
            pd.put("msg", "no");
        }
        pdList.add(pd);
        map.put("list", pdList);
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 请求选择方案
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/selectSchedules")
    public ModelAndView selectSchedules() throws Exception {
        ModelAndView mv = this.getModelAndView();
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            List<Schedules> scheduleslist = scheduleService.listSchedulesforTask("");
            mv.addObject("pd", pd);
            mv.addObject("schelist", scheduleslist);
            mv.setViewName("timedcast/program_select");
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return mv;
    }

    /**
     * 导出任务信息到EXCEL
     *
     * @return
     */
    @RequestMapping(value = "/excel")
    public ModelAndView exportExcel() {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        List<ScheTask> taskList;
        ObjectExcelView erv;
        try {
            String ids = (null == pd.get("checkedidlist") || "".equals(pd.get("checkedidlist").toString())) ? "" : pd.get("checkedidlist").toString();
            String ScheId = (null == pd.get("ScheId") || "".equals(pd.get("ScheId").toString())) ? "" : pd.get("ScheId").toString();
            if (null != ids && !"".equals(ids)) {
                String Arraytaskids[] = ids.split(",");
                List<String> taskids = new ArrayList<String>();
                for (int i = 0; i < Arraytaskids.length; i++) {
                    taskids.add(Arraytaskids[i]);
                }
                pd.put("taskids", taskids);
                taskList = scheTaskService.getTaskListByIds(pd);
                erv = new ObjectExcelView(); // 执行excel操作
            } else if (null != ScheId && !"".equals(ScheId)) {
                taskList = scheTaskService.getScheTasksByScheId(pd);
                erv = new ObjectExcelView(pd.getString("ScheName")); // 执行excel操作
            } else {
                pd.put("taskids", null);
                taskList = new ArrayList<>();
                erv = new ObjectExcelView(); // 执行excel操作
            }
            Map<String, Object> dataMap = new HashMap<String, Object>();
            List<String> titles = new ArrayList<String>();
            //titles.add("方案编号"); // 2
            titles.add("任务名称"); // 3
            titles.add("是否启用"); // 4
            titles.add("广播时间"); // 5
            titles.add("单次广播日期"); // 6
            titles.add("播放持续时间"); // 7
            titles.add("选中音频"); // 8
            titles.add("每周安排"); // 9
            titles.add("默认音量"); // 10
            titles.add("广播分区"); // 11
            titles.add("开始时间"); // 12
            titles.add("结束时间"); // 13
            titles.add("广播类型"); // 14
            titles.add("备注"); // 15
            dataMap.put("titles", titles);
            List<PageData> varList = new ArrayList<PageData>();
            for (ScheTask task : taskList) {
                PageData vpd = new PageData();
                //vpd.put("var1", task.getScheId()); //2
                vpd.put("var1", task.getTaskName()); // 3
                vpd.put("var2", task.getStatus() ? "是" : "否"); // 4
                vpd.put("var3", task.getExecTime()); // 5
                vpd.put("var4",
                        task.getSingleDate() == null || task.getSingleDate().equals("") ? "" : task.getSingleDate()); // 6
                vpd.put("var5", task.getLastingSeconds()); // 7
                vpd.put("var6", task.getFilesInfo()); // 8
                vpd.put("var7", task.getWeeks()); // 9
                vpd.put("var8", task.getVols()); // 10
                vpd.put("var9", task.getDomainsId()); // 11
                vpd.put("var10", task.getStartDateTime().substring(0, task.getStartDateTime().length() - 2)); // 12
                vpd.put("var11", task.getEndDateTime().substring(0, task.getEndDateTime().length() - 2)); // 13
                vpd.put("var12", task.getTasktype() == 0 ? "顺序" : (task.getTasktype() == 1 ? "循环" : (task.getTasktype() == 3 ? "单曲" : "随机"))); // 14
                vpd.put("var13", task.getNote()); // 14
                varList.add(vpd);
            }
            dataMap.put("varList", varList);
            mv = new ModelAndView(erv, dataMap);
            // 插入日志
            saveLog(Const.LOGTYPE[1], ScheTaskManager, "Excel导出", this.getRemortIP(), ids.length() < 240 ? ids : "");
            // }
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 请求批量新增任务页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toAddList")
    public ModelAndView toAddList() throws Exception {
        ModelAndView mv = this.getModelAndView();
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            int id = Integer.parseInt(scheTaskService.findMaxTaskId(pd) == null ? "0" : scheTaskService.findMaxTaskId(pd)) + 1;
            pd.put("TaskId", id);
            mv.addObject("pd", pd);
            mv.addObject("MSG", "addList"); // 执行状态 addList 为添加
            mv.setViewName("timedcast/task_edit");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 批量添加任务信息
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/addList")
    public ModelAndView addList(ScheTask task) throws Exception {
        logBefore(Jurisdiction.getUsername() + "批量添加任务");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pdChange(pd);
        try {
            int[] time = convertStrToArray(pd.getString("Interval"));
            int[] etime = convertStrToArray(pd.getString("ExecTime"));
            int step = time[0] * 60 * 60 + time[1] * 60 + time[2];
            int exectime = etime[0] * 60 * 60 + etime[1] * 60 + etime[2];
            pd.put("step", step);
            pd.put("time", exectime);
            List<ScheTask> tasks = ModifyList(pd);
            GlobalInfoController.putThreadintoStandbyPool(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        scheTaskService.addListTasksFromExcel(tasks); // 批量保存任务
                        quartzService.addJobList(tasks);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        logError(e);
                    }
                }
            });
            Thread.sleep(40 * (Integer.parseInt(pd.getString("TasksNumber")) + 5));//延迟一下
            // 插入日志
            saveLog(Const.LOGTYPE[1], ScheTaskManager, "批量新增", this.getRemortIP(), tasks.get(0).getTaskName() + "-" + tasks.get(tasks.size() - 1).getTaskName());
        } catch (Exception e) {
            logger.error(e.toString(), e);
            mv.addObject("msg", "failed");
        }
        mv.setViewName("redirect:listScheTasks.do?ScheId=" + task.getScheId() + "&ScheName=" + pd.getString("ScheName")); // 保存成功跳转到列表页面
        return mv;
    }

    private List<ScheTask> ModifyList(PageData pd) {
        // 批量添加部分:解压并格式化批量添加的数据信息
        List<ScheTask> list = new ArrayList<ScheTask>();
        for (int count = 0; count < Integer.parseInt(pd.getString("TasksNumber")); count++) {
            ScheTask scheTask = new ScheTask();
            scheTask.setNote(pd.getString("note"));
            scheTask.setLastingSeconds(pd.getString("LastingSeconds"));
            scheTask.setTaskId(pd.getString("TaskId"));
            pd.put("TaskId", String.valueOf(Integer.parseInt(pd.getString("TaskId")) + 1));
            scheTask.setDomainsId(pd.getString("DomainsId"));
            scheTask.setEndDateTime(pd.getString("EndDateTime"));
            scheTask.setStatus((int) (pd.get("Status")) == 1 ? true : false);
            scheTask.setUpDateTime(pd.getString("upDateTime"));
            scheTask.setFilesInfo(pd.getString("FilesInfo"));
            scheTask.setWeeks(pd.getString("Weeks"));
            scheTask.setScheId(pd.getString("ScheId"));
            scheTask.setScheName(pd.getString("ScheName"));
            scheTask.setStartDateTime(pd.getString("StartDateTime"));
            if (pd.getString("tasktype").contains("顺序")) {
                scheTask.setTasktype(0);
            } else if (pd.getString("tasktype").contains("单曲")) {
                scheTask.setTasktype(3);
            } else if (pd.getString("tasktype").contains("随机")) {
                scheTask.setTasktype(2);
            } else if (pd.getString("tasktype").contains("循环")) {
                scheTask.setTasktype(1);
            } else {
                scheTask.setTasktype(0);
            }

            scheTask.setExecTime(pd.getString("ExecTime"));
            scheTask.setVols(pd.getString("Vols"));
            scheTask.setSingleDate(pd.getString("SingleDate"));
            if (pd.getString("Tnumber").length() > 0)
                scheTask.setTaskName(pd.getString("Prefix")
                        + String.valueOf(Integer.parseInt(pd.getString("Tnumber")) + count) + pd.getString("Suffix"));
            else if (pd.getString("Prefix").length() > 0 && pd.getString("Suffix").length() > 0) {
                scheTask.setTaskName(pd.getString("Prefix") + pd.getString("Suffix"));
            } else if (pd.getString("Prefix").length() > 0) {
                scheTask.setTaskName(pd.getString("Prefix"));
            } else {
                scheTask.setTaskName(pd.getString("Suffix"));
            }
            list.add(scheTask);
            int time = (int) pd.get("time") + (int) pd.get("step");
            String timestr = exectime(time);
            pd.put("ExecTime", timestr);
            pd.put("time", time);
        }
        return list;
    }

    private String exectime(int time) {
        String exString = "";
        int hour = (time / 3600) % 24;
        int min = (time / 60) % 60;
        int sec = time % 60;
        exString += (hour >= 10 ? hour : ("0" + hour)) + ":";
        exString += (min >= 10 ? min : ("0" + min)) + ":";
        exString += sec >= 10 ? sec : ("0" + sec);
        return exString;
    }

    private int[] convertStrToArray(String str) {
        StringTokenizer st = new StringTokenizer(str, ":");// 把"."作为分割标志，然后把分割好的字符赋予StringTokenizer对象。
        int[] strArray = new int[st.countTokens()];// 通过StringTokenizer 类的countTokens方法计算在生成异常之前可以调用此 tokenizer 的
        // nextToken 方法的次数。
        int i = 0;
        while (st.hasMoreTokens()) {// 看看此 tokenizer 的字符串中是否还有更多的可用标记。
            strArray[i++] = Integer.parseInt(st.nextToken());// 返回此 string tokenizer 的下一个标记。
        }
        return strArray;
    }

    /**
     * 获取所有计划中的任务列表
     *
     * @param model
     * @return
     * @throws SchedulerException
     *//*
	@RequestMapping("/test")
	public ModelAndView getAllJobTest(Page page) throws SchedulerException {
		if (!Jurisdiction.hasJurisdiction("timedcast/test.do")) {
			return null;
		} // 校验权限
		ModelAndView mv = this.getModelAndView();
		List<JobEntity> jobInfos = new ArrayList<JobEntity>();
		jobInfos = quartzService.queryTriggerList();
		mv.addObject("jobInfos", jobInfos);
		mv.setViewName("timedcast/list");
		return mv;
	}
*/

    /**
     * 从EXCEL导入到数据库
     *
     * @param file
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @RequestMapping(value = "/readExcel", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Object readExcel(@RequestParam(value = "file") MultipartFile file) {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = this.getPageData();
        if (null != file && !file.isEmpty()) {
            String filePath = PathUtil.getClasspath() + Const.FILEPATHFILE + "poinfo/"; // 文件上传路径
            String fileName = FileUpload.fileUp(file, filePath, "poinfoexcel"); // 执行上传
            List<PageData> listPd = (List) ObjectExcelRead.readExcel(filePath, fileName, 1, 0, 0); // 执行读EXCEL操作,读出的数据导入List
            // 1:从第2行开始；0:从第A列开始；0:第0个sheet
            /* 存入数据库操作====================================== */
            /**
             * var1 :标题 var2 :类型 var3 :内容
             */
            try {
                String log = "";
                String ScheId = pd.getString("ScheId");
                int taskId = Integer.parseInt(scheTaskService.findMaxTaskId(pd)) + 1;
                List<ScheTask> tasklist = new ArrayList<ScheTask>();
                Date day = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (int i = 0; i < listPd.size(); i++) {
                    ScheTask task = new ScheTask();
                    task.setTaskId(String.valueOf(taskId + i));
                    task.setScheId(ScheId);
                    task.setTaskName(listPd.get(i).getString("var0"));
                    task.setStatus(listPd.get(i).getString("var1").trim().equals("是") ? true : false);
                    task.setExecTime(listPd.get(i).getString("var2"));

                    if (!listPd.get(i).getString("var3").equals("") && listPd.get(i).getString("var3") != null)
                        task.setSingleDate(listPd.get(i).getString("var3"));

                    task.setLastingSeconds(listPd.get(i).getString("var4"));
                    task.setFilesInfo(listPd.get(i).getString("var5"));
                    task.setWeeks(listPd.get(i).getString("var6"));
                    task.setVols(listPd.get(i).getString("var7"));
                    task.setDomainsId(listPd.get(i).getString("var8"));

                    if (listPd.get(i).getString("var9") != "" && listPd.get(i).getString("var9") != null) {
                        task.setStartDateTime(listPd.get(i).getString("var9"));
                    } else {
                        task.setStartDateTime(df.format(day));
                    }

                    if (listPd.get(i).getString("var10") != "" && listPd.get(i).getString("var10") != null) {
                        task.setEndDateTime(listPd.get(i).getString("var10"));
                    } else {
                        task.setEndDateTime("2099-12-31 00:00:00");
                    }
                    if (listPd.get(i).getString("var11").trim().contains("顺序")) {
                        task.setTasktype(0);
                    } else if (listPd.get(i).getString("var11").trim().contains("单曲")) {
                        task.setTasktype(3);
                    } else if (listPd.get(i).getString("var11").trim().contains("随机")) {
                        task.setTasktype(2);
                    } else if (listPd.get(i).getString("var11").trim().contains("循环")) {
                        task.setTasktype(1);
                    } else {
                        task.setTasktype(0);
                    }
                    task.setNote(listPd.get(i).getString("var12"));
                    task.setUpDateTime(df.format(day));
                    formatTime(task);
                    tasklist.add(task);
                    log += task.getTaskName() + ",";
                }
                log = log.substring(0, log.length() - 1);
                scheTaskService.addListTasksFromExcel(tasklist);
                quartzService.addJobList(tasklist);
                // 插入日志
                saveLog(Const.LOGTYPE[1], ScheTaskManager, "Excel导入", this.getRemortIP(), log.length() < 240 ? log : "");
                map.put("result", "success");
                map.put("code", "0");
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                logError(e);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logError(e);
                map.put("msg", "error");
                map.put("code", "-1");
            }

        } else {
            map.put("msg", "error");
            map.put("code", "-1");
        }
        return AppUtil.returnObject(new PageData(), map);
    }

    // 格式化excel导入时间
    private void formatTime(ScheTask task) {
        task.setSingleDate(formatDateTime(task.getSingleDate(), "-"));
        task.setExecTime(formatDateTime(task.getExecTime(), ":"));
        task.setStartDateTime(formatDateTime(task.getStartDateTime(), ""));
        task.setEndDateTime(formatDateTime(task.getEndDateTime(), ""));
    }

    // 格式化excel导入时间日期
    private String formatDateTime(String datatime, String statu) {
        if (datatime == null || datatime.equals("")) {
            return null;
        }
        Pattern p = Pattern.compile("\\d{1,}");// 这个2是指连续数字的最少个数
        Matcher m = p.matcher(datatime);
        int i = 0;
        String time = "";
        while (m.find()) {
            if (i == 3 && statu.equals(""))
                time = time.substring(0, time.length() - 1) + " ";
            if (i < 3 && !statu.equals("")) {
                if (m.group().length() < 2)
                    time += "0" + m.group() + statu;
                else
                    time += m.group() + statu;
            } else if (i < 3) {
                if (m.group().length() < 2)
                    time += "0" + m.group() + "-";
                else if (m.group().length() < 4 && i == 0) {
                    time += "20" + m.group() + "-";
                } else
                    time += m.group() + "-";
            } else if (i < 6) {
                if (m.group().length() < 2)
                    time += "0" + m.group() + ":";
                else
                    time += m.group() + ":";
            }
            i++;
        }
        time = time.substring(0, time.length() - 1);
        return time;
    }

    /**
     * 文件列表
     *
     * @return
     */
    @RequestMapping("/findFile")
    public ModelAndView list(Page page) throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        page.setPd(pd);
        ArrayList<String> filelist = FileUtil.getFileslist(systemService.getBaseAttri("文件广播目录"), page);
        mv.addObject("filelist", filelist);
        mv.addObject("pd", pd);
        mv.setViewName("timedcast/findfile");
        return mv;
    }

    /**
     * 分区列表
     *
     * @return
     */
    @RequestMapping("/finddomids")
    public ModelAndView finddomids() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        List<String> domidsInfo = new ArrayList<String>();
        List<String> alldomidsInfo = new ArrayList<String>();
        String domids = (null == pd.get("domidInfo") || "".equals(pd.get("domidInfo").toString())) ? ""
                : pd.get("domidInfo").toString();
		/*String statu = (null == pd.get("statu") || "".equals(pd.get("statu").toString())) ? ""
				: pd.get("statu").toString();*/
        String[] grouplist = domids == null ? null : domids.split("//");
        String[] domidlist = grouplist[0] == null ? null : grouplist[0].split(",");
        String[] tids = new String[]{};
        if (grouplist.length > 1) {
            tids = grouplist[1] == null ? null : grouplist[1].split(",");
        }
        for (int i = 0; i < domidlist.length; i++) {
            if (!domidlist[i].contains("*")) {
                domidsInfo.add(domidlist[i]);
                alldomidsInfo.add(domidlist[i]);
            } else {
                alldomidsInfo.add(domidlist[i].substring(1, domidlist[i].length()));
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
			/*if (id.length() <= 2) {
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
            int j;
            JSONObject obj = terarr.getJSONObject(i);
            String did = obj.getString("pId");
            String tid = obj.getString("tid");
            for (j = 0; j < domidsInfo.size(); j++) {
                if (did.equals(domidsInfo.get(j))) {
                    obj.element("checked", true);
                    break;
                }
            }
            if (j >= domidsInfo.size())
                for (j = 0; j < tids.length; j++) {
                    if (tid.equals(tids[j])) {
                        obj.element("checked", true);
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
     * 重置定时任务状态
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/resetSche")
    @ResponseBody
    public Object resetSche() throws Exception {
        logBefore(Jurisdiction.getUsername() + "重置定时任务");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String returndata = "error";
        try {
            String ScheId = (null == pd.get("ScheId") || "".equals(pd.get("ScheId").toString())) ? "" : pd.get("ScheId").toString();
            if (!"".equals(ScheId) && !"0".equals(ScheId))
                quartzService.resetSche(pd);
            saveLog(Const.LOGTYPE[1], ScheTaskManager, "重置定时任务", this.getRemortIP(), ScheId);
            returndata = "success";
            Thread.sleep(500);//休眠500ms保证任务有已被更新的。
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
            saveLog(Const.LOGTYPE[1], ScheTaskManager, "重置定时任务失败", this.getRemortIP(), "");
        }
        map.put("result", returndata);
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 读取方案信息
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/readSche")
    @ResponseBody
    public Object readSche() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String returndata = "error";
        try {
            String ScheId = (null == pd.get("ScheId") || "".equals(pd.get("ScheId").toString())) ? "" : pd.get("ScheId").toString();
            JSONArray sList = JSONArray.fromObject(scheduleService.listSchedulesforTask(ScheId));
            String lastjson = sList.toString();
            map.put("sList", lastjson);
            map.put("scheId", ScheId);
            returndata = "success";
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
        }
        map.put("result", returndata);
        return AppUtil.returnObject(new PageData(), map);
    }
}
