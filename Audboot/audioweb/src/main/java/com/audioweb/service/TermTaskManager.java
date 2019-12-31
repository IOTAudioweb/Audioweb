package com.audioweb.service;

import com.audioweb.entity.Page;
import com.audioweb.entity.TermTask;
import com.audioweb.util.PageData;

import java.util.List;

public interface TermTaskManager {
    /**
     * 列出所有任务<分页>
     *
     * @param page
     * @throws Exception
     */
    public List<TermTask> getTermTask(Page page) throws Exception;

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
    public void addTermTask(PageData pd) throws Exception;

    /**
     * 修改任务
     *
     * @param pd
     * @throws Exception
     */
    public void editTermTask(PageData pd) throws Exception;

    /**
     * 删除任务
     *
     * @param pd
     * @throws Exception
     */
    public void deleteTermTask(PageData pd) throws Exception;

    /**
     * 更改任务启用性
     *
     * @param pd
     * @throws Exception
     */
    public void editTermTaskStatus(PageData pd) throws Exception;

    /**
     * 批量删除终端
     */
    public void deleteAllO(String[] tids) throws Exception;

    /**
     * 通过ID获取任务信息
     */
    public TermTask getTaskByTaskId(PageData pd) throws Exception;

    /**
     * 更新任务定时状态
     *
     * @param pd
     * @throws Exception
     */
    public void updateTermTask(PageData pd) throws Exception;

}
