package com.audioweb.service;

import com.audioweb.entity.Page;
import com.audioweb.entity.ScheTask;
import com.audioweb.util.PageData;

import java.util.List;

public interface ScheTaskManager {

    /**
     * 根据方案编号列出所有任务<分页>
     *
     * @param page
     * @throws Exception
     */
    public List<ScheTask> getScheTaskByScheId(Page page) throws Exception;

    /**
     * 根据方案编号列出所有任务
     *
     * @param pd
     * @throws Exception
     */
    public List<ScheTask> getScheTasksByScheId(PageData pd) throws Exception;

    /**
     * 获取最大id
     */
    public String findMaxTaskId(PageData pd) throws Exception;

    /**
     * 添加任务
     *
     * @param pd
     * @throws Exception
     */
    public void addScheTask(PageData pd) throws Exception;

    /**
     * 修改任务
     *
     * @param pd
     * @throws Exception
     */
    public void editScheTask(PageData pd) throws Exception;

    /**
     * 修改任务
     *
     * @param pd
     * @throws Exception
     */
    public void editScheTaskList(PageData pd) throws Exception;

    /**
     * 修改任务音量
     *
     * @param pd
     * @throws Exception
     */
    public void editScheTaskVol(PageData pd) throws Exception;

    /**
     * 删除任务
     *
     * @param pd
     * @throws Exception
     */
    public void deleteScheTask(PageData pd) throws Exception;

    /**
     * 更改任务启用性
     *
     * @param pd
     * @throws Exception
     */
    public void editScheTaskStatus(PageData pd) throws Exception;

    /**
     * 批量删除终端
     */
    public void deleteAllO(String[] tids) throws Exception;

    /**
     * 通过ID获取任务信息
     */
    public ScheTask getTaskByTaskId(PageData pd) throws Exception;

    /**
     * 通过ID获取任务信息<分页>
     */
    public List<ScheTask> getTaskListByIds(PageData pd) throws Exception;

    /**
     * 批量添加任务<上传>
     *
     * @param list
     * @throws Exception
     */
    public void addListTasksFromExcel(List<ScheTask> list) throws Exception;

    /**
     * 更新任务定时状态
     *
     * @param pd
     * @throws Exception
     */
    public void updateScheTask(PageData pd) throws Exception;

}
