package com.audioweb.service;

import com.audioweb.entity.Page;
import com.audioweb.entity.Schedules;
import com.audioweb.util.PageData;

import java.util.List;

/**
 * 方案接口类
 */
public interface SchedulesManager {
    /**
     * 列出方案（分页）
     *
     * @param page
     * @return
     * @throws Exception
     */
    public List<Schedules> listSchedules(Page page) throws Exception;

    /**
     * 获取最大id
     */
    public String findMaxScheId(PageData pd) throws Exception;

    /**
     * 添加方案
     *
     * @param pd
     * @throws Exception
     */
    public void addSchedule(PageData pd) throws Exception;

    /**
     * 修改方案
     *
     * @param pd
     * @throws Exception
     */
    public void editSchedule(PageData pd) throws Exception;

    /**
     * 删除方案
     *
     * @param pd
     * @throws Exception
     */
    public void deleteSchedule(PageData pd) throws Exception;

    /**
     * 查询启用方案
     */
    public List<Schedules> listUsingSchedule(PageData pd) throws Exception;

    /**
     * 更改启用方案
     *
     * @param pd
     * @throws Exception
     */
    public void editUsingSchedule(PageData pd) throws Exception;

    /**
     * 查询所有方案
     */
    public List<Schedules> listSchedulesforTask(String str) throws Exception;

    /**
     * 通过ID查询方案
     */
    public Schedules findScheduleById(PageData pd) throws Exception;

}
