package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.Page;
import com.audioweb.entity.Schedules;
import com.audioweb.service.SchedulesManager;
import com.audioweb.util.PageData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("scheduleService")
public class ScheduleService implements SchedulesManager {

    @Resource(name = "daoSupport")
    private DaoSupport dao;

    @SuppressWarnings("unchecked")
    @Override
    public List<Schedules> listSchedules(Page page) throws Exception {
        // 列出所有方案（分页）
        return (List<Schedules>) dao.findForList("SchedulesMapper.listAllSchedulesPage", page);
    }

    @Override
    public String findMaxScheId(PageData pd) throws Exception {
        // 找到最大方案ID
        return (String) dao.findForObject("SchedulesMapper.findMaxScheId", pd);
    }

    @Override
    public void addSchedule(PageData pd) throws Exception {
        // 添加方案
        dao.save("SchedulesMapper.addSchedule", pd);
    }

    @Override
    public void editSchedule(PageData pd) throws Exception {
        // 修改方案
        dao.update("SchedulesMapper.editSchedule", pd);
    }

    @Override
    public void deleteSchedule(PageData pd) throws Exception {
        // 删除方案
        dao.update("SchedulesMapper.deleteSchedule", pd);
    }

    @Override
    public void editUsingSchedule(PageData pd) throws Exception {
        // 更改启用方案
        dao.update("SchedulesMapper.editUsingSchedule", pd);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Schedules> listUsingSchedule(PageData pd) throws Exception {
        // 查询启用方案ID
        return (List<Schedules>) dao.findForList("SchedulesMapper.listUsingSchedule", pd);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Schedules> listSchedulesforTask(String str) throws Exception {
        // 查询所有方案
        return (List<Schedules>) dao.findForList("SchedulesMapper.listSchedules", str);
    }

    @Override
    public Schedules findScheduleById(PageData pd) throws Exception {
        //通过ID查询方案
        return (Schedules) dao.findForObject("SchedulesMapper.findScheduleById", pd);
    }

}
