package com.audioweb.service.impl;


import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.Page;
import com.audioweb.entity.Roles;
import com.audioweb.service.RolesManager;
import com.audioweb.util.PageData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * 角色
 */
@Service("roleService")
public class RolesService implements RolesManager {

    @Resource(name = "daoSupport")
    private DaoSupport dao;

    /**
     * 列出此组下级角色
     *
     * @param pd
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<Roles> listAllRoles(PageData pd) throws Exception {
        return (List<Roles>) dao.findForList("RolesMapper.listAllRoles", pd);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Roles> listRoles(Page page) throws Exception {
        return (List<Roles>) dao.findForList("RolesMapper.listAllRoleslistPage", page);
    }

    @SuppressWarnings("unchecked")
    public List<PageData> findmaxIdbyRoleId(String RoleId) throws Exception {
        return (List<PageData>) dao.findForList("RolesMapper.findmaxIdbyRoleId", RoleId);
    }

    @Override
    public void deleteRole(String RoleId) throws Exception {
        dao.update("RolesMapper.deleteRole", RoleId);
    }

    @Override
    public void editRole(PageData pd) throws Exception {
        dao.update("RolesMapper.editRole", pd);
    }

    @Override
    public void addRole(PageData pd) throws Exception {
        dao.save("RolesMapper.addRole", pd);
    }

    @Override
    public void editRoleRights(PageData pd) throws Exception {
        dao.update("RolesMapper.editRoleRights", pd);
    }

    @Override
    public Roles findrolebyRoleId(String str) throws Exception {
        //查询角色属性
        return (Roles) dao.findForObject("RolesMapper.findrolebyRoleId", str);
    }

}
