package com.audioweb.service;


import com.audioweb.entity.Page;
import com.audioweb.entity.Roles;
import com.audioweb.util.PageData;

import java.util.List;


/**
 * 角色接口类
 */
public interface RolesManager {

    /**
     * 列出所有角色
     *
     * @param str
     * @return
     * @throws Exception
     */
    public List<Roles> listAllRoles(PageData pd) throws Exception;

    /**
     * 列出角色（分页）
     *
     * @param page
     * @return
     * @throws Exception
     */
    public List<Roles> listRoles(Page page) throws Exception;

    /**
     * 查询本角色属下最大ID
     *
     * @param str
     * @return
     * @throws Exception
     */
    public List<PageData> findmaxIdbyRoleId(String str) throws Exception;

    /**
     * 查询角色属性
     *
     * @param str
     * @return
     * @throws Exception
     */
    public Roles findrolebyRoleId(String str) throws Exception;

    /**
     * 删除角色
     *
     * @param RoleId
     * @throws Exception
     */
    public void deleteRole(String RoleId) throws Exception;

    /**
     * 修改角色
     *
     * @param pd
     * @throws Exception
     */
    public void editRole(PageData pd) throws Exception;

    /**
     * 角色授权
     *
     * @param pd
     * @throws Exception
     */
    public void editRoleRights(PageData pd) throws Exception;

    /**
     * 添加角色
     *
     * @param pd
     * @throws Exception
     */
    public void addRole(PageData pd) throws Exception;

}
