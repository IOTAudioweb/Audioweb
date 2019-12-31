package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.Page;
import com.audioweb.entity.Terminals;
import com.audioweb.service.TerminalsManager;
import com.audioweb.util.PageData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service("TerminalsService")
public class TerminalsService implements TerminalsManager {
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    /**
     * 根据分区id 获取终端列表
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<PageData> listAllTerByDomainId(String DomainId) throws Exception {
        // TODO Auto-generated method stub
        return (List<PageData>) dao.findForList("TerminalsMapper.listAllTerByDomainId", DomainId);
    }

    /**
     * 根据分区id 获取终端列表<分页>
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Terminals> listAllTerByDomainIdPage(Page page) throws Exception {
        // TODO Auto-generated method stub
        return (List<Terminals>) dao.findForList("TerminalsMapper.listAllTerByDomainIdPage", page);
    }

    /**
     * 获得所有终端列表
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<PageData> listAllTer(String str) throws Exception {
        // TODO Auto-generated method stub
        return (List<PageData>) dao.findForList("TerminalsMapper.listAllTer", str);
    }

    /**
     * 获得所有终端列表
     */
    @SuppressWarnings("unchecked")
    public List<Terminals> listAllTerm(Page page) throws Exception {
        // TODO Auto-generated method stub
        return (List<Terminals>) dao.findForList("TerminalsMapper.listAllTerm", page);
    }

    /**
     * 获得所有终端列表
     */
    @SuppressWarnings("unchecked")
    public List<Terminals> listAllTerPage(Page page) throws Exception {
        // TODO Auto-generated method stub
        return (List<Terminals>) dao.findForList("TerminalsMapper.listAllTerPage", page);
    }

    /**
     * 根据编号获取终端信息
     */
    @Override
    public Terminals getTermByTIDString(String TIDString) throws Exception {
        // TODO Auto-generated method stub
        return (Terminals) dao.findForObject("TerminalsMapper.getTermByTIDString", TIDString);
    }

    /**
     * 模糊查询
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Terminals> getTermByATIDString(Page page) throws Exception {
        // TODO Auto-generated method stub
        return (List<Terminals>) dao.findForList("TerminalsMapper.listTermByATIDStringPage", page);
    }


    /**
     * 新增终端
     *
     * @param terminals
     * @throws Exception
     */
    @Override
    public void saveTer(Terminals ter) throws Exception {
        dao.save("TerminalsMapper.insertTer", ter);
    }

    /**
     * 批量新增终端
     *
     * @param terminals
     * @throws Exception
     */
    @Override
    public void saveTerlist(List<Terminals> ter) throws Exception {
        List<Terminals> updater = new ArrayList<>();
        PageData pd = new PageData();
        for (int i = ter.size() - 1; i >= 0; i--) {
            pd.put("TIDString", ter.get(i).getTIDString());
            PageData rePageData = findUsingId(pd);
            if (rePageData != null) {
                updater.add(ter.get(i));
                ter.remove(i);
            }
        }
        dao.batchSave("TerminalsMapper.insertTer", ter);
        dao.batchSave("TerminalsMapper.insertTerByTid", updater);
        ter = updater;
    }

    /**
     * 删除终端
     *
     * @param id
     * @throws Exception
     */
    @Override
    public void deleteTerByTid(String TIDString) throws Exception {
        dao.save("TerminalsMapper.deleteTerByTid", TIDString);
    }

    /**
     * 批量删除终端
     *
     * @param tids
     * @throws Exception
     */
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
                dao.delete("TerminalsMapper.deleteAllOS", array[i]);
            }
        } else {
            dao.delete("TerminalsMapper.deleteAllOS", tids);
        }
    }

    /**
     * 编辑终端
     *
     * @return
     * @throws Exception
     */
    @Override
    public void editTer(PageData pd) throws Exception {
        dao.update("TerminalsMapper.updateTer", pd);
    }

    /**
     * 取最大ID
     *
     * @return
     * @throws Exception
     */
    @Override
    public String findMaxTIDString(PageData pd) throws Exception {
        return (String) dao.findForObject("TerminalsMapper.findMaxTIDString", pd);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PageData> listAllTerByDomainsId(PageData pd) throws Exception {
        // 根据批量分区id 获取终端列表
        return (List<PageData>) dao.findForList("TerminalsMapper.listAllTerByDomainsId", pd);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Terminals> listAllTerByTidPage(Page page) throws Exception {
        //根据批量终端ID获取终端列表<分页>
        return (List<Terminals>) dao.findForList("TerminalsMapper.listAllTerByTidPage", page);
    }

    @Override
    public void editDate(PageData pd) throws Exception {
        // 更新终端最后一次下线时间
        dao.update("TerminalsMapper.updateTerDate", pd);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Terminals> listTerByDomainId(String DomainId) throws Exception {
        //根据分区id 获取本分区终端列表
        return (List<Terminals>) dao.findForList("TerminalsMapper.listTerByDomainId", DomainId);
    }

    @Override
    public Terminals getTermByTid(String TIDString) throws Exception {
        //根据编号获得终端特定信息
        return (Terminals) dao.findForObject("TerminalsMapper.getTermByTid", TIDString);
    }

    @Override
    public Terminals findTermByTip(String Tip) throws Exception {
        //根据tip获得特定终端信息
        return (Terminals) dao.findForObject("TerminalsMapper.findTermbyTip", Tip);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Terminals> listAllTerByIds(PageData pd) throws Exception {
        //  根据id或者终端名称 获取终端列表
        return (List<Terminals>) dao.findForList("TerminalsMapper.listAllTerByIds", pd);
    }

    @Override
    public PageData findUsingId(PageData pd) throws Exception {
        // 获取判断是否使用中的ID
        return (PageData) dao.findForObject("TerminalsMapper.findUsingId", pd);
    }

    @Override
    public void insertTerByTid(PageData pd) throws Exception {
        // 更新终端
        dao.update("TerminalsMapper.insertTerByTid", pd);
    }

    @Override
    public void editTersDomainId(PageData pd) throws Exception {
        //批量修改终端分区
        dao.update("TerminalsMapper.editTersDomainId", pd);
    }

}
