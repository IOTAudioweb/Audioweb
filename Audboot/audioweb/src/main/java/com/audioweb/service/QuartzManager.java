package com.audioweb.service;

import com.audioweb.entity.JobEntity;
import com.audioweb.entity.ScheTask;
import com.audioweb.util.PageData;

import java.util.List;

public interface QuartzManager {

    /**
     * 增加一个任务
     *
     * @param pd 创建job信息实体接口
     */
    public void addJob(PageData pd) throws Exception;

    /**
     * 增加一个定时刷新任务
     */
    public void addRefreshJob() throws Exception;

    /**
     * 初始化添加任务
     *
     * @param jobEntity 创建scheduleJob
     */
    public void initializeJob(JobEntity jobEntity, PageData pd);

    /**
     * 获取所有计划中的任务列表
     *
     * @return
     */
    public List<JobEntity> queryTriggerList();

    /**
     * 暂停一个任务
     *
     * @param triggerName
     * @param triggerGroupName
     */
    public void pauseTrigger(String triggerName, String triggerGroupName);

    /**
     * 恢复一个任务
     *
     * @param triggerName
     * @param triggerGroupName
     */
    public void resumeTrigger(String triggerName, String triggerGroupName);

    /**
     * 立即执行一个job
     *
     * @param jobName
     * @param jobGroup
     * @param isskip   是否跳过下次
     */
    public void runjob(String jobName, String jobGroup, boolean isskip);

    /**
     * 修改 一个job的 时间表达式
     *
     * @param triggerName
     * @param triggerGroupName
     * @param CronExpr
     */
    public void updateJob(String triggerName, String triggerGroupName, String CronExpr);

    /**
     * 更新任务状态
     *
     * @param triggerName
     * @param triggerGroupName
     */
    public void updateTask(String triggerName, String triggerGroupName, boolean isRunning);

    /**
     * 删除一个任务触发
     *
     * @param jobName
     * @param triggerName
     * @param triggerGroupName
     */
    public void deleteTrigger(String triggerName, String triggerGroupName);

    /**
     * 删除一个任务对象
     *
     * @param jobName
     * @param triggerName
     * @param triggerGroupName
     */
    public void deleteJob(String jobName, String jobGroup);

    /**
     * 批量添加任务<Excel导入>
     *
     * @param tasklist 创建job信息实体
     */
    public void addJobList(List<ScheTask> tasklist) throws Exception;

    /**
     * 修改方案
     *
     * @param pd 方案更改
     */
    public void editSchedule(PageData pd) throws Exception;

    /**
     * 删除方案
     *
     * @param pd 方案删除
     */
    public void deleteSchedule(PageData pd) throws Exception;

    /**
     * 修改任务
     *
     * @param pd
     */
    public void editScheTask(PageData pd) throws Exception;

    /**
     * 批量修改任务
     *
     * @param pd
     */
    public void editScheTaskList(PageData pd) throws Exception;

    /**
     * 恢复定时刷新任务
     */
    public void resumeRefreshTask() throws Exception;

    /**
     * 获取定时刷新任务状态
     */
    public Boolean getRefreshTask() throws Exception;

    /**
     * 重置方案任务定时器信息
     *
     * @param pd 重置方案任务定时器信息
     */
    public void resetSche(PageData pd) throws Exception;

    /**
     * 添加或者修改终端定时重启任务
     */
    public void addOrEditTerReboot(PageData pd) throws Exception;

    /**
     * 获取终端定时重启任务状态
     */
    public String getTerReboot() throws Exception;

    /**
     * 获取终端定时重启任务详细信息
     */
    public PageData getTerRebootInfo() throws Exception;

    /**
     * 删除终端定时重启任务
     */
    public Boolean deleteReboot() throws Exception;
}
