package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.JobEntity;
import com.audioweb.entity.ScheTask;
import com.audioweb.entity.TermTask;
import com.audioweb.quartzjob.AudioWebJob;
import com.audioweb.service.*;
import com.audioweb.util.Const;
import com.audioweb.util.PageData;
import com.audioweb.util.Tools;
import com.niocast.util.GlobalInfoController;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时广播服务
 */
@Service("quartzService")
public class QuartzService implements QuartzManager {
    private static final String clazz = "com.audioweb.quartzjob.AudioWebJob";
    private static final String freshclazz = "com.audioweb.quartzjob.RefreshJob";
    private static final String rebootclazz = "com.audioweb.quartzjob.TerminalsRebootJob";
    public static final String JobTimeGroup = "定时任务";
    public static final String JobTermGroup = "采播任务";
    private static final String fresh = "RefreshJob";
    private static final String rebootGroup = "TerminalRebootJob";
    private static final int freshtime = Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "freshtime"));
    private static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Resource(name = "daoSupport")
    private DaoSupport dao;

    @Resource(name = "scheTaskService")
    private ScheTaskManager scheTaskService;

    @Resource(name = "scheduleService")
    private SchedulesManager scheduleService;

    @Resource(name = "TerminalsService")
    private TerminalsManager TerminalsService;

    @Resource(name = "systemService")
    private SystemManager systemService;

    @Resource(name = "logService")
    private LogManager logService;

    @Resource(name = "termTaskService")
    private TermTaskManager termTaskService;

    private static Logger logger = LoggerFactory.getLogger(QuartzService.class);

    @Autowired
    private Scheduler scheduler;// 获取调度器

    /**
     * 增加一个任务
     *
     * @param pd 创建job信息实体接口
     */

    @Override
    public void addJob(PageData pd) throws Exception {
        // 初始化JobEntity
        String id = (null == pd.get("TIDString") || "".equals(pd.get("TIDString").toString())) ? "" : pd.get("TIDString").toString();
        if ("".equals(id)) {
            ScheTask task = scheTaskService.getTaskByTaskId(pd);
            jobforlist(pd, task);
        } else {
            pd.put("ScheId", JobTermGroup);
            TermTask task = termTaskService.getTaskByTaskId(pd);
            jobforlist(pd, task);
        }
    }

    /**
     * 批量添加任务<Excel导入>
     *
     * @param pd 创建job信息实体
     */
    @Override
    public void addJobList(List<ScheTask> tasklist) throws Exception {
        for (ScheTask scheTask : tasklist) {
            PageData pd = new PageData();
            pd.put("ScheId", scheTask.getScheId());
            pd.put("Status", scheTask.getStatus() ? 1 : 0);
            jobforlist(pd, scheTask);
        }
    }

    //格式化pd传来的值并存入jobentity中
    private void jobforlist(PageData pd, ScheTask task) {
        JobEntity job = new JobEntity();
        try {
            job.setClazz(clazz);
            job.setJobGroupName(JobTimeGroup);
            job.setTriggerGroupName(task.getScheId());
            if (new Date().after(sf.parse(task.getStartDateTime())))
                job.setStartTime(new Date());
            else
                job.setStartTime(sf.parse(task.getStartDateTime()));
            job.setEndTime(sf.parse(task.getEndDateTime()));
            job.setJobName(task.getTaskId() + ":" + task.getTaskName());
            job.setTriggerName(task.getTaskId());
            job.setCronExpr(timeToCron(task));
            initializeJob(job, pd);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void jobforlist(PageData pd, TermTask task) {
        //格式化pd传来的值并存入jobentity中,采播
        JobEntity job = new JobEntity();
        try {
            job.setClazz(clazz);
            job.setJobGroupName(JobTermGroup);
            job.setTriggerGroupName(JobTermGroup);
            job.setStartTime(new Date());
            job.setJobName(task.getTaskId() + ":" + task.getTaskName());
            job.setTriggerName(String.valueOf(task.getTaskId()));
            job.setCronExpr(timeToCron(task));
            initializeJob(job, pd);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String timeToCron(ScheTask task) {
        //时间格式转换Cron
        String CronTime = "";
        String time = task.getExecTime();
        String date = task.getSingleDate();
        String[] exectime = time == null || time == "" ? null : time.split(":");
        String[] datetime = date == null || date == "" ? null : date.split("-");
        for (int i = exectime.length - 1; i >= 0; i--) {
            CronTime += exectime[i] + " ";
        }
        if (task.getWeeks().substring(0, 1).equals("1")) {
            if (date == null || date == "") {
                return null;
            } else {
                for (int i = datetime.length - 1; i >= 0; i--) {
                    if (i == 0) {
                        CronTime += "? ";
                    }
                    CronTime += datetime[i] + " ";
                }
            }
        } else {
            CronTime += "? * ";
            for (int i = 1; i < task.getWeeks().length(); i++) {
                if (task.getWeeks().charAt(i) == '1') {
                    if (i != 7) {
                        CronTime += (i + 1) + ",";
                    } else {
                        CronTime += i - 6 + ",";
                    }
                }
            }
        }
        CronTime = CronTime.substring(0, CronTime.length() - 1);
        if (time == null || time == "") {
            return null;
        }
        return CronTime;
    }

    private String timeToCron(TermTask task) {
        //时间格式转换Cron
        String CronTime = "";
        String time = task.getExecTime();
        String date = "2099-12-31";
        String[] exectime = time == null || time == "" ? null : time.split(":");
        String[] datetime = date == null || date == "" ? null : date.split("-");
        for (int i = exectime.length - 1; i >= 0; i--) {
            CronTime += exectime[i] + " ";
        }
        if (task.getWeeks().substring(0, 1).equals("1")) {
            if (date == null || date == "") {
                return null;
            } else {
                for (int i = datetime.length - 1; i >= 0; i--) {
                    if (i == 0) {
                        CronTime += "? ";
                    }
                    CronTime += datetime[i] + " ";
                }
            }
        } else {
            CronTime += "? * ";
            for (int i = 1; i < task.getWeeks().length(); i++) {
                if (task.getWeeks().charAt(i) == '1') {
                    if (i != 7) {
                        CronTime += (i + 1) + ",";
                    } else {
                        CronTime += i - 6 + ",";
                    }
                }
            }
        }
        CronTime = CronTime.substring(0, CronTime.length() - 1);
        if (time == null || time == "") {
            return null;
        }
        return CronTime;
    }

    /**
     * 初始化添加任务
     *
     * @param jobEntity 创建scheduleJob
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initializeJob(JobEntity jobEntity, PageData pd) {
        logger.debug("添加自动任务：" + jobEntity.getJobName());
        try {
            Class jobClass = Class.forName(jobEntity.getClazz());
            // 创建一项作业
            JobDetail job = JobBuilder.newJob(jobClass)
                    .withIdentity(jobEntity.getJobName(), jobEntity.getJobGroupName())
                    .build();// 任务名，任务组，任务执行类
            job.getJobDataMap().put(AudioWebJob.ISSKIP, "false");
            // 创建一个触发器
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobEntity.getTriggerName(), jobEntity.getTriggerGroupName())
                    .withSchedule(CronScheduleBuilder.cronSchedule(jobEntity.getCronExpr()).withMisfireHandlingInstructionDoNothing())
                    .startAt(jobEntity.getStartTime())
                    .endAt(jobEntity.getEndTime())
                    .build();
            // 告诉调度器使用该触发器来安排作业
            scheduler.scheduleJob(job, trigger);
            // 启动
            if (scheduler.isShutdown()) {
                scheduler.start();
            }
            if (!pd.getString("ScheId").equals(JobTermGroup) && !scheduleService.findScheduleById(pd).getIsExecSchd()) {
                pauseTrigger(jobEntity.getTriggerName(), jobEntity.getTriggerGroupName());
            } else if (null != pd.get("Status") && (int) pd.get("Status") == 0) {
                pauseTrigger(jobEntity.getTriggerName(), jobEntity.getTriggerGroupName());
            }
            updateTask(jobEntity.getTriggerName(), jobEntity.getTriggerGroupName(), false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 获取所有计划中的任务列表
     *
     * @return
     */
    public List<JobEntity> queryTriggerList() {
        List<JobEntity> jobList = null;
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
            jobList = new ArrayList<JobEntity>();
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    JobEntity job = new JobEntity();
                    job.setSchedulerName(scheduler.getSchedulerName());
                    job.setJobName(jobKey.getName());
                    job.setJobGroupName(jobKey.getGroup());
                    job.setTriggerName(trigger.getKey().getName());
                    job.setTriggerGroupName(trigger.getKey().getGroup());
                    job.setNextFireTime(trigger.getNextFireTime());
                    job.setStartTime(trigger.getStartTime());
                    job.setEndTime(trigger.getEndTime());
                    Date previousFireTime = trigger.getPreviousFireTime();
                    if (previousFireTime != null) {
                        job.setPreviousFireTime(previousFireTime);
                    }
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    job.setJobStatus(triggerState.name());
                    if (trigger instanceof CronTrigger) {
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        String cronExpression = cronTrigger.getCronExpression();
                        job.setCronExpr(cronExpression);
                    }
                    jobList.add(job);
                }
            }
        } catch (SchedulerException e) {
            logger.error("获取自动任务列表失败", e);
        }
        return jobList;
    }

    /**
     * 暂停一个任务
     *
     * @param triggerName
     * @param triggerGroupName
     */
    public void pauseTrigger(String triggerName, String triggerGroupName) {
        try {
            logger.debug("暂停任务触发器：" + triggerName);
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            scheduler.pauseTrigger(triggerKey);
            updateTask(triggerName, triggerGroupName, false);
        } catch (SchedulerException e) {
            logger.error("暂停任务触发器失败：" + triggerName, e);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 恢复一个任务
     *
     * @param triggerName
     * @param triggerGroup
     */
    public void resumeTrigger(String triggerName, String triggerGroupName) {
        try {
            logger.debug("启用任务触发器：" + triggerName);
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            scheduler.resumeTrigger(triggerKey);
            updateTask(triggerName, triggerGroupName, false);
        } catch (SchedulerException e) {
            logger.error("启用任务触发器失败：" + triggerName, e);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 立即执行一个job
     *
     * @param jobName
     * @param jobGroup
     */
    public void runjob(String jobName, String jobGroup, boolean isskip) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.triggerJob(jobKey);
            JobDetail detail = scheduler.getJobDetail(jobKey);
            if (isskip) {
                Thread.sleep(200);
                logger.debug("提前执行任务：" + jobName + detail.isPersistJobDataAfterExecution());
                List<JobExecutionContext> eContexts = scheduler.getCurrentlyExecutingJobs();
                //System.out.println("Size:"+eContexts.size());
                for (JobExecutionContext jContext : eContexts) {
                    if (jContext.getJobDetail().equals(detail)) {
                        synchronized (jContext) {
                            jContext.getJobDetail().getJobDataMap().put(AudioWebJob.ISSKIP, "true");
                            //System.out.println("Input"+"提前执行任务");
                        }
                        break;
                    }
                }
            } else {
                logger.debug("立即执行任务：" + jobName);
            }
        } catch (SchedulerException e) {
            logger.error("立即执行任务失败：" + jobName, e);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 修改 一个job的 时间表达式
     *
     * @param jobName
     * @param jobGroupName
     * @param CronExpr
     */
    public void updateJob(String triggerName, String triggerGroupName, String CronExpr) {
        try {
            logger.debug("修改任务时间：" + triggerName);
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(CronExpr)).build();
            // 重启触发器
            scheduler.rescheduleJob(triggerKey, trigger);
            updateTask(triggerName, triggerGroupName, false);
        } catch (SchedulerException e) {
            e.printStackTrace();
            logger.error("修改任务时间失败：" + triggerName, e);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 更新一个任务的信息
     *
     * @param jobName      ==>taskid
     * @param jobGroupName ==>schename
     * @param CronExpr
     */
    @Override
    public void updateTask(String triggerName, String triggerGroupName, boolean isRunning) {
        // 更新任务状态
        PageData pd = new PageData();
        try {
            if (isRunning) {
                pd.put("jobStatus", "RUNNING");
            } else {
                TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
                Trigger trigger = scheduler.getTrigger(triggerKey);
                Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
                if (triggerState.name().equals("NORMAL") && trigger.getNextFireTime() != null) {
                    pd.put("nextFireTime", sf.format(trigger.getNextFireTime()));
                }
                pd.put("jobStatus", triggerState.name());
            }
            pd.put("TaskId", triggerName);
            if (triggerGroupName.equals(JobTermGroup)) {
                termTaskService.updateTermTask(pd);
            } else {
                scheTaskService.updateScheTask(pd);
            }
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTrigger(String triggerName, String triggerGroupName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            // 停止触发器
            scheduler.pauseTrigger(triggerKey);
            // 移除触发器
            scheduler.unscheduleJob(triggerKey);
            // 删除任务
        } catch (Exception e) {
            // TODO: handle exception
            logger.error("error:", e);
        }

    }

    @Override
    public void deleteJob(String jobName, String jobGroup) {
        // 删除任务
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            logger.error("error:", e);
        }

    }

    @Override
    public void editSchedule(PageData pd) throws Exception {
        pd.put("newScheId", pd.get("ScheId"));
        List<ScheTask> tasklist = scheTaskService.getScheTasksByScheId(pd);
        // 方案更改
        if ((boolean) pd.get("isuse")) {
            GlobalInfoController.putThreadintoStandbyPool(new Runnable() {//调用1个线程处理方案改变时的任务启用和暂停
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    for (ScheTask scheTask : tasklist) {
                        try {
                            resumeTrigger(scheTask.getTaskId(), scheTask.getScheId());
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            GlobalInfoController.putThreadintoStandbyPool(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (ScheTask scheTask : tasklist) {
                            try {
                                pauseTrigger(scheTask.getTaskId(), scheTask.getScheId());
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void deleteSchedule(PageData pd) throws Exception {
        // 删除方案
        List<ScheTask> tasklist = scheTaskService.getScheTasksByScheId(pd);
        for (ScheTask scheTask : tasklist) {
            deleteTrigger(scheTask.getTaskId(), scheTask.getScheId());
            deleteJob(scheTask.getTaskId() + ":" + scheTask.getTaskName(), pd.getString("JobGroup"));
        }
    }

    @Override
    public void editScheTask(PageData pd) throws Exception {
        // 修改任务
        deleteTrigger(pd.getString("TaskId"), pd.getString("ScheId"));
        deleteJob(pd.getString("TaskId") + ":" + pd.getString("TaskName"), pd.getString("JobGroup"));
        addJob(pd);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void addRefreshJob() throws Exception {
        // 增加一个定时刷新任务
        logger.debug("添加定时刷新任务：" + fresh);
        try {
            Class jobClass = Class.forName(freshclazz);
            JobDetail job = JobBuilder.newJob(jobClass)
                    .withIdentity(fresh, fresh)
                    .build();
            // 创建一个触发器
            SimpleTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(fresh, fresh)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder
                            .repeatSecondlyForever(freshtime)
                            .withRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
                            .withMisfireHandlingInstructionNextWithRemainingCount())
                    .build();
            // 告诉调度器使用该触发器来安排作业
            scheduler.scheduleJob(job, trigger);
            // 启动
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resumeRefreshTask() throws Exception {
        // 恢复定时刷新任务
        deleteTrigger(fresh, fresh);
        deleteJob(fresh, fresh);
        addRefreshJob();
    }

    @Override
    public Boolean getRefreshTask() {
        // 获取定时刷新任务状态
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(fresh, fresh);
            Trigger trigger;
            trigger = scheduler.getTrigger(triggerKey);
            if (trigger != null) {
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                if (null != triggerState && triggerState.name().equals("NORMAL")) return true;
            }
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    //重置方案任务定时器信息
    @Override
    public void resetSche(PageData pd) throws Exception {
        // TODO Auto-generated method stub
        pd.put("newScheId", pd.getString("ScheId"));
        pd.put("upDateTime", new Date());
        List<ScheTask> newtasklist = scheTaskService.getScheTasksByScheId(pd);
        // 方案更改
        GlobalInfoController.putThreadintoStandbyPool(new Runnable() {//调用1个线程处理任务重置
            @Override
            public void run() {
                PageData pData = new PageData();
                // TODO Auto-generated method stub
                for (ScheTask scheTask : newtasklist) {
                    try {
                        if (scheTask.getWeeks().charAt(0) == '0') {
                            pData.put("TaskId", scheTask.getTaskId());
                            pData.put("ScheId", scheTask.getScheId());
                            pData.put("Status", scheTask.getStatus() ? 1 : 0);
                            // 修改任务
                            deleteTrigger(scheTask.getTaskId(), scheTask.getScheId());
                            deleteJob(scheTask.getTaskId() + ":" + scheTask.getTaskName(), JobTimeGroup);
                            addJob(pData);
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void editScheTaskList(PageData pd) throws Exception {
        // TODO Auto-generated method stub
        List<ScheTask> taskList = scheTaskService.getTaskListByIds(pd);
        GlobalInfoController.putThreadintoStandbyPool(new Runnable() {//调用1个线程处理任务重置
            @Override
            public void run() {
                PageData pData = new PageData();
                // TODO Auto-generated method stub
                for (ScheTask scheTask : taskList) {
                    try {
                        pData.put("TaskId", scheTask.getTaskId());
                        pData.put("ScheId", scheTask.getScheId());
                        pData.put("Status", scheTask.getStatus() ? 1 : 0);
                        // 修改任务
                        deleteTrigger(scheTask.getTaskId(), scheTask.getScheId());
                        deleteJob(scheTask.getTaskId() + ":" + scheTask.getTaskName(), JobTimeGroup);
                        addJob(pData);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void addOrEditTerReboot(PageData pd) throws Exception {
        JobKey jobKey = JobKey.jobKey(rebootGroup, rebootGroup);
        // TODO 添加或者修改终端定时重启任务
        JobDetail detail = scheduler.getJobDetail(jobKey);
        if (detail == null) {//直接新建
            Class jobClass = Class.forName(rebootclazz);
            detail = JobBuilder.newJob(jobClass)
                    .withIdentity(rebootGroup, rebootGroup)
                    .storeDurably()
                    .build();
            // 加入一个任务到Quartz框架中, 等待后面再绑定Trigger,
            scheduler.addJob(detail, false);
        } else {//重置trigger
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            if (triggers != null && triggers.size() > 0) {
                for (Trigger trigger : triggers) deleteTrigger(trigger.getKey().getName(), rebootGroup);
            }
        }
        // 增加一个定时刷新任务
        logger.debug("添加定时重启任务：" + rebootGroup);
        try {
            int size = Integer.parseInt(pd.get("times").toString());
            for (int i = 0; i < size; i++) {
                CronTrigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("ExecDate_" + i, rebootGroup)
                        .startNow()
                        .withSchedule(CronScheduleBuilder.cronSchedule(dateToCron(pd, i)).withMisfireHandlingInstructionDoNothing())
                        .startAt(sf.parse(pd.get("upDateTime").toString()))
                        .forJob(rebootGroup, rebootGroup)
                        .usingJobData("times", size)
                        .usingJobData("ExecDate_" + i, pd.getString("ExecDate_" + i))
                        .usingJobData("Weeks", pd.getString("Weeks"))
                        .build();
                // 告诉调度器使用该触发器来安排作业
                //scheduler.scheduleJob(detail, trigger);
                scheduler.scheduleJob(trigger); // 看这里
            }
            // 启动
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTerReboot() throws Exception {
        // TODO 获取终端定时重启任务状态
        try {
            JobKey jobKey = JobKey.jobKey(rebootGroup, rebootGroup);
            JobDetail detail = scheduler.getJobDetail(jobKey);
            if (detail != null) {//重置trigger
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                if (triggers != null && triggers.size() > 0) {
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(triggers.get(0).getKey());
                    if (null != triggerState) return triggerState.name();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Boolean deleteReboot() throws Exception {
        // TODO 删除终端定时重启任务
        try {
            JobKey jobKey = JobKey.jobKey(rebootGroup, rebootGroup);
            JobDetail detail = scheduler.getJobDetail(jobKey);
            if (detail != null) {//重置trigger
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                if (triggers != null && triggers.size() > 0) {
                    for (Trigger trigger : triggers) deleteTrigger(trigger.getKey().getName(), rebootGroup);
                }
                deleteJob(rebootGroup, rebootGroup);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return false;
    }

    private String dateToCron(PageData pd, int point) {
        //时间格式转换Cron
        String CronTime = "";
        String time = pd.getString("ExecDate_" + point);
        String week = pd.getString("Weeks");
        String[] exectime = time == null || time == "" ? null : time.split(":");
        for (int i = exectime.length - 1; i >= 0; i--) {
            CronTime += exectime[i] + " ";
        }
        if (week.substring(0, 1).equals("1")) {
            CronTime += "* * ? ";
        } else {
            CronTime += "? * ";
            for (int i = 1; i < week.length(); i++) {
                if (week.charAt(i) == '1') {
                    if (i != 7) {
                        CronTime += (i + 1) + ",";
                    } else {
                        CronTime += i - 6 + ",";
                    }
                }
            }
        }
        CronTime = CronTime.substring(0, CronTime.length() - 1);
        if (time == null || time == "") {
            return null;
        }
        return CronTime;
    }

    @Override
    public PageData getTerRebootInfo() throws Exception {
        // TODO 获取终端定时重启任务详细信息
        PageData pd = new PageData();
        try {
            JobKey jobKey = JobKey.jobKey(rebootGroup, rebootGroup);
            JobDetail detail = scheduler.getJobDetail(jobKey);
            if (detail != null) {//重置trigger
                List<String> exectime = new ArrayList<>();
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                if (triggers != null && triggers.size() > 0) {
                    for (Trigger trigger : triggers) {
                        pd.put(trigger.getKey().getName(), trigger.getJobDataMap().get(trigger.getKey().getName()));//获取当前trigger具体时间
                        pd.put("times", trigger.getJobDataMap().get("times"));
                        pd.put("Weeks", trigger.getJobDataMap().get("Weeks"));
                        if (pd.get("nextTime") == null) {
                            pd.put("nextTime", trigger.getNextFireTime());
                        } else if (trigger.getNextFireTime().before((Date) pd.get("nextTime"))) {
                            pd.put("nextTime", trigger.getNextFireTime());
                        }
                    }
                }
                if (triggers.size() > 1) {
                    for (int i = 1; i < triggers.size(); i++) {
                        exectime.add(pd.getString("ExecDate_" + i));
                    }
                }
                pd.put("exectime", exectime);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return pd;
    }
}
