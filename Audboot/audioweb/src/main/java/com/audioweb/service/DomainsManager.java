package com.audioweb.service;

import com.audioweb.entity.Domains;
import com.audioweb.util.PageData;

import java.util.List;

public interface DomainsManager {
    /**
     * 通过ID获取其子一级区域
     */
    public List<Domains> listSubDomainsByParentDomainId(String ParentDomainId) throws Exception;

    /**
     * 通过ID获取其子一级区域中最大id
     */
    public PageData listSubMaxDomainsByParentDomainId(PageData pd) throws Exception;

    /**
     * 查询所有区域列表
     */
    public List<Domains> listAllDomains(String str) throws Exception;

    /**
     * 通过ID获取分区
     */
    public List<Domains> listAllSubByDomainId(String domainId) throws Exception;

    /**
     * 根据编号获取区域
     */
    public Domains getDomainsByDomainId(String DomainId) throws Exception;

    /**
     * @param Domains
     * @throws Exception
     */
    public void saveDomains(Domains domains) throws Exception;


    /**
     * @param DomainId
     * @throws Exception
     */
    public void deleteDomainsByDomainId(String DomainId) throws Exception;

    /**
     * @param Domains
     * @throws Exception
     */
    public void editDomains(PageData pd) throws Exception;

    /**
     * 取父区域下最大分区编号
     */
    public PageData findCurLevelMaxId(PageData pd) throws Exception;

    /**
     * 通过名称获取分区
     */
    public List<Domains> listAllDomainByIds(PageData pd) throws Exception;
//	/**根据区域编号查询该区域下所有用户（分页)
//	 * @param Domains
//	 * @throws Exception
//	 */
//	
//	public List<PageData> listDomainsByDomainId(PageData pd) throws Exception;


}
