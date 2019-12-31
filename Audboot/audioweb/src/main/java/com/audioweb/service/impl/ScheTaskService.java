package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.Page;
import com.audioweb.entity.ScheTask;
import com.audioweb.service.ScheTaskManager;
import com.audioweb.util.PageData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("scheTaskService")
public class ScheTaskService implements ScheTaskManager {
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    @SuppressWarnings("unchecked")

    public List<ScheTask> getScheTaskByScheId(Page page) throws Exception {
        // 查询任务列表（分页）
        return (List<ScheTask>) dao.findForList("ScheTaskMapper.getScheTaskByScheIdPage", page);
    }

    @Override
    public String findMaxTaskId(PageData pd) throws Exception {
        //查询最大ID
        return (String) dao.findForObject("ScheTaskMapper.findMaxTaskId", pd);
    }

    @Override
    public void addScheTask(PageData pd) throws Exception {
        // 添加任务
        dao.save("ScheTaskMapper.insertTask", pd);
    }

    @Override
    public void editScheTask(PageData pd) throws Exception {
        // 修改任务
        dao.update("ScheTaskMapper.editScheTask", pd);
    }

    @Override
    public void editScheTaskList(PageData pd) throws Exception {
        // 修改任务
        dao.update("ScheTaskMapper.editTaskList", pd);
    }

    @Override
    public void editScheTaskVol(PageData pd) throws Exception {
        // 修改任务音量
        dao.update("ScheTaskMapper.editScheTaskVol", pd);
    }

    @Override
    public void deleteScheTask(PageData pd) throws Exception {
        //删除任务
        dao.update("ScheTaskMapper.deleteScheTask", pd);
    }

    @Override
    public void editScheTaskStatus(PageData pd) throws Exception {
        // 修改任务启用性
        dao.update("ScheTaskMapper.editScheTaskStatus", pd);
    }

    @Override
    public void deleteAllO(String[] tids) throws Exception {
        // 批量删除终端
        if (tids.length > 10) {//超过10个任务则分批删除
            int size = (int) Math.ceil(tids.length / 10.0);
            String[][] array = new String[size][10];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < 10 && j < tids.length - i * 10; j++) {
                    array[i][j] = tids[i * 10 + j];
                }
                dao.delete("ScheTaskMapper.deleteAllOS", array[i]);
            }
        } else {
            dao.delete("ScheTaskMapper.deleteAllOS", tids);
        }
    }

    @Override
    public ScheTask getTaskByTaskId(PageData pd) throws Exception {
        // 通过ID获取任务信息
        return (ScheTask) dao.findForObject("ScheTaskMapper.getTaskByTaskId", pd);
    }

    @SuppressWarnings("unchecked")
    public List<ScheTask> getTaskListByIds(PageData pd) throws Exception {
        //根据任务编号获取其信息(excel导出)
        return (List<ScheTask>) dao.findForList("ScheTaskMapper.getTaskListByIds", pd);
    }

    @Override
    public void addListTasksFromExcel(List<ScheTask> list) throws Exception {
        // TODO 批量添加任务<上传>
        dao.batchSave("ScheTaskMapper.insertTask", list);
    }

    @Override
    public void updateScheTask(PageData pd) throws Exception {
        // 更新任务定时状态
        dao.update("ScheTaskMapper.updateScheTask", pd);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ScheTask> getScheTasksByScheId(PageData pd) throws Exception {
        // 根据方案编号列出所有任务
        return (List<ScheTask>) dao.findForList("ScheTaskMapper.getScheTasksByScheId", pd);
    }
}



