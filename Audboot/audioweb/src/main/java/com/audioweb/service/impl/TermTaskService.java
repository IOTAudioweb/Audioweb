package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.Page;
import com.audioweb.entity.TermTask;
import com.audioweb.service.TermTaskManager;
import com.audioweb.util.PageData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("termTaskService")
public class TermTaskService implements TermTaskManager {
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    @SuppressWarnings("unchecked")
    @Override
    public List<TermTask> getTermTask(Page page) throws Exception {
        //列出所有任务<分页>
        return (List<TermTask>) dao.findForList("TermTaskMapper.getTermTaskPage", page);
    }

    @Override
    public String findMaxTaskId(PageData pd) throws Exception {
        //获取最大id
        return (String) dao.findForObject("TermTaskMapper.findMaxTaskId", pd);
    }

    @Override
    public void addTermTask(PageData pd) throws Exception {
        //添加任务
        dao.save("TermTaskMapper.insertTask", pd);
    }

    @Override
    public void editTermTask(PageData pd) throws Exception {
        //修改任务
        dao.update("TermTaskMapper.editTermTask", pd);
    }

    @Override
    public void deleteTermTask(PageData pd) throws Exception {
        //删除任务
        dao.update("TermTaskMapper.deleteTermTask", pd);
    }

    @Override
    public void editTermTaskStatus(PageData pd) throws Exception {
        // 更改任务启用性
        dao.update("TermTaskMapper.editTermTaskStatus", pd);
    }

    @Override
    public void deleteAllO(String[] tids) throws Exception {
        // 批量删除终端
        dao.delete("TermTaskMapper.deleteAllOS", tids);
    }

    @Override
    public TermTask getTaskByTaskId(PageData pd) throws Exception {
        // 通过ID获取任务信息
        return (TermTask) dao.findForObject("TermTaskMapper.getTaskByTaskId", pd);
    }

    @Override
    public void updateTermTask(PageData pd) throws Exception {
        // 更新任务定时状态
        dao.update("TermTaskMapper.updateTermTask", pd);
    }

}
