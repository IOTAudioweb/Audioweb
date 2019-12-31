package com.audioweb.service;


import com.audioweb.entity.Page;
import com.audioweb.entity.Terminals;
import com.audioweb.util.PageData;

import java.util.List;


/**
 * 日志接口类
 */
public interface TerminalsManager {


    /**
     * 根据分区id 获取终端列表
     */
    public List<PageData> listAllTerByDomainId(String DomainId) throws Exception;

    /**
     * 根据分区id 获取终端列表<分页>
     */
    public List<Terminals> listAllTerByDomainIdPage(Page page) throws Exception;

    /**
     * 获得所有终端列表
     */
    public List<PageData> listAllTer(String str) throws Exception;

    /**
     * 获得所有终端列表
     */
    public List<Terminals> listAllTerm(Page page) throws Exception;

    /**
     * 获得所有终端列表<分页>
     */
    public List<Terminals> listAllTerPage(Page page) throws Exception;

    /**
     * 获取判断是否使用中的ID
     */
    public PageData findUsingId(PageData pd) throws Exception;

    /**
     * 获取最大id
     */
    public String findMaxTIDString(PageData pd) throws Exception;

    /**
     * 新增终端
     */
    public void saveTer(Terminals ter) throws Exception;

    /**
     * 批量新增终端
     */
    public void saveTerlist(List<Terminals> ter) throws Exception;

    /**
     * 删除终端
     */
    public void deleteTerByTid(String tid) throws Exception;

    /**
     * 批量删除终端
     */
    public void deleteAllO(String[] tids) throws Exception;

    /**
     * 编辑终端
     */
    public void editTer(PageData pd) throws Exception;

    /**
     * 根据编号获得终端信息
     */
    public Terminals getTermByTIDString(String TIDString) throws Exception;

    /**
     * 根据编号获得终端特定信息
     */
    public Terminals getTermByTid(String TIDString) throws Exception;

    /**
     * 模糊查询
     */
    public List<Terminals> getTermByATIDString(Page page) throws Exception;

    /**
     * 根据批量分区id 获取终端列表
     */
    public List<PageData> listAllTerByDomainsId(PageData pd) throws Exception;

    /**
     * 根据批量终端id 获取终端列表<分页>
     */
    public List<Terminals> listAllTerByTidPage(Page page) throws Exception;

    /**
     * 更新终端最后一次下线时间
     */
    public void editDate(PageData pd) throws Exception;

    /**
     * 更新终端
     */
    public void insertTerByTid(PageData pd) throws Exception;


    /**
     * 根据分区id 获取本分区终端列表
     */
    public List<Terminals> listTerByDomainId(String DomainId) throws Exception;

    /**
     * 根据tip获得特定终端信息
     */
    public Terminals findTermByTip(String Tip) throws Exception;

    /**
     * 根据id或者终端名称 获取终端列表
     */
    public List<Terminals> listAllTerByIds(PageData pd) throws Exception;

    /**
     * 批量修改终端分区
     */
    public void editTersDomainId(PageData pd) throws Exception;
}
