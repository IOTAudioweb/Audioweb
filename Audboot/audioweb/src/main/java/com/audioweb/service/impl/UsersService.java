package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.Page;
import com.audioweb.entity.Users;
import com.audioweb.service.UsersManager;
import com.audioweb.util.PageData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * 系统管理用户
 */
@Service("usersService")
public class UsersService implements UsersManager {

    @Resource(name = "daoSupport")
    private DaoSupport dao;

    /**
     * 登录判断
     *
     * @param pd
     * @return
     * @throws Exception
     */
    @Override
    public Users getUserByLoginAndPwd(PageData pd) throws Exception {
        Users user = (Users) dao.findForObject("UsersMapper.getUserByLoginAndPwd", pd);
        return user;
    }

    /**
     * 登陆名是否存在判断
     *
     * @param loginid
     * @return
     * @throws Exception
     */
    @Override
    public String checkLoginid(String loginid) throws Exception {
        return (String) dao.findForObject("UsersMapper.getUseridByLoginid", loginid);
    }

    /**
     * 用户注册
     *
     * @param pd
     * @throws Exception
     */
    @Override
    public void insertUser(PageData pd) throws Exception {
        dao.save("UsersMapper.insertUser", pd);
    }

    /**
     * 通过useid获取数据
     *
     * @param pd
     * @return
     * @throws Exception
     */
    @Override
    public Users findByUserid(PageData pd) throws Exception {
        return (Users) dao.findForObject("UsersMapper.findByUserid", pd);
    }

    /**
     * 修改用户
     *
     * @param pd
     * @throws Exception
     */
    @Override
    public void editUser(PageData pd) throws Exception {
        dao.update("UsersMapper.editUser", pd);
    }

    /**
     * 修改用户密码
     *
     * @param pd
     * @throws Exception
     */
    @Override
    public void editPwd(PageData pd) throws Exception {
        dao.update("UsersMapper.editPwd", pd);
    }

    /**
     * 删除用户信息
     */
    @Override
    public void deleteUser(String userid) throws Exception {
        dao.delete("UsersMapper.deleteUser", userid);
    }

    /**
     * 修改用户账号可用状态
     *
     * @param pd
     * @throws Exception
     */
    @Override
    public void editIsenabled(PageData pd) throws Exception {
        dao.update("UsersMapper.editIsenabled", pd);
    }

    /**
     * 查询用户列表
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Users> getUserList(Page page) throws Exception {
        return (List<Users>) dao.findForList("UsersMapper.getUserslistPage", page);
    }

    @Override
    public String findMaxuserId(PageData pd) throws Exception {
        //取最大Id
        return (String) dao.findForObject("UsersMapper.findMaxuserId", pd);
    }
}
