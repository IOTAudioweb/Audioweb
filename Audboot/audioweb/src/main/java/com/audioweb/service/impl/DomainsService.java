package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.Domains;
import com.audioweb.service.DomainsManager;
import com.audioweb.util.PageData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service("DomainsService")
public class DomainsService implements DomainsManager {
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    /**
     * 通过id获取下级子区域
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Domains> listSubDomainsByParentDomainId(String ParentDomainId) throws Exception {
        return (List<Domains>) dao.findForList("DomainsMapper.listSubDomainIdByParentDomainId", ParentDomainId);
    }

    /**
     * 通过id获取下级子区域最大ID
     */
    @Override
    public PageData listSubMaxDomainsByParentDomainId(PageData pd) throws Exception {
        return (PageData) dao.findForList("DomainsMapper.findSubMaxDomainIdByParentDomainId", pd);
    }

    /**
     * 根据父区域编号获取所有子区域（级联）
     */
    @Override
    public List<Domains> listAllSubByDomainId(String DomainId) throws Exception {
        List<Domains> Domainslist = this.listSubDomainsByParentDomainId(DomainId);
        List<Domains> sublist = new ArrayList<Domains>();
        for (Domains domains : Domainslist) {
            sublist.addAll(this.listAllSubByDomainId(Integer.toString(domains.getDomainId())));
        }
        if (sublist != null) {
            Domainslist.addAll(sublist);
        }
        return Domainslist;
    }

    /**
     * 获得所有区域列表
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Domains> listAllDomains(String str) throws Exception {
        return (List<Domains>) dao.findForList("DomainsMapper.listAllDomains", str);
    }

    /**
     * 根据编号获取区域
     */
    @Override
    public Domains getDomainsByDomainId(String DomainId) throws Exception {
        return (Domains) dao.findForObject("DomainsMapper.getDomainsByDid", DomainId);
    }

    /**
     * 新增区域
     *
     * @param area
     * @throws Exception
     */
    @Override
    public void saveDomains(Domains domains) throws Exception {
        dao.save("DomainsMapper.insertDomains", domains);
    }

    /**
     * 删除区域
     *
     * @param aid
     * @throws Exception
     */
    @Override
    public void deleteDomainsByDomainId(String domainId) throws Exception {
        dao.save("DomainsMapper.deleteDomainsByDomainId", domainId);
    }

    /**
     * 编辑区域
     *
     * @param area
     * @return
     * @throws Exception
     */
    @Override
    public void editDomains(PageData pd) throws Exception {
        dao.update("DomainsMapper.updateDomains", pd);
    }

    @Override
    public PageData findCurLevelMaxId(PageData pd) throws Exception {
        return (PageData) dao.findForObject("DomainsMapper.findCurLevelMaxId", pd);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Domains> listAllDomainByIds(PageData pd) throws Exception {
        //  通过名称获取分区
        return (List<Domains>) dao.findForList("DomainsMapper.listAllDomainByIds", pd);
    }

}
