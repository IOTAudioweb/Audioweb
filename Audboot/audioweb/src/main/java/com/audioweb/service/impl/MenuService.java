package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.Menu;
import com.audioweb.service.MenuManager;
import com.audioweb.util.PageData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 类名称：MenuService 菜单处理
 */
@Service("menuService")
public class MenuService implements MenuManager {

    @Resource(name = "daoSupport")
    private DaoSupport dao;

    /**
     * 通过ID获取其子一级菜单
     *
     * @param parentId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<Menu> listSubMenuByParentId(String parentId) throws Exception {
        return (List<Menu>) dao.findForList("MenuMapper.listSubMenuByParentId", parentId);
    }

    /**
     * 通过菜单ID获取数据
     *
     * @param pd
     * @return
     * @throws Exception
     */
    public PageData getMenuById(PageData pd) throws Exception {
        return (PageData) dao.findForObject("MenuMapper.getMenuById", pd);
    }


    /**
     * 取最大ID
     *
     * @param pd
     * @return
     * @throws Exception
     */
    public PageData findMaxId(PageData pd) throws Exception {
        return (PageData) dao.findForObject("MenuMapper.findMaxId", pd);
    }


    /**
     * 获取所有菜单并填充每个菜单的子菜单列表(菜单管理)(递归处理)
     *
     * @param MENU_ID
     * @return
     * @throws Exception
     */
    public List<Menu> listAllMenu(String mid) throws Exception {
        List<Menu> menuList = this.listSubMenuByParentId(mid);
        for (Menu menu : menuList) {
//			menu.setMurl(menu.getMurl());
            menu.setSubMenu(this.listAllMenu(menu.getMid()));
            menu.setTarget("treeFrame");
        }
        return menuList;
    }


}
