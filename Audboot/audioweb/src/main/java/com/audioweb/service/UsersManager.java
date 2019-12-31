package com.audioweb.service;

import com.audioweb.entity.Page;
import com.audioweb.entity.Users;
import com.audioweb.util.PageData;

import java.util.List;


/**
 * 用户接口类
 */
public interface UsersManager {

    /**
     * 登录判断
     *
     * @param pd
     * @return
     * @throws Exception
     */
    public Users getUserByLoginAndPwd(PageData pd) throws Exception;

    /**
     * 登陆名是否存在判断
     *
     * @param pd
     * @return
     * @throws Exception
     */
    public String checkLoginid(String username) throws Exception;

    /**
     * 用户信息列表
     *
     * @param page
     * @param @throws Exception
     * @return List<User>
     */
    public List<Users> getUserList(Page page) throws Exception;

    /**
     * 通过useid获取数据
     *
     * @param pd
     * @return
     * @throws Exception
     */
    public Users findByUserid(PageData pd) throws Exception;

    /**
     * 修改用户
     *
     * @param pd
     * @throws Exception
     */
    public void editUser(PageData pd) throws Exception;

    /**
     * 修改用户密码
     *
     * @param pd
     * @throws Exception
     */
    public void editPwd(PageData pd) throws Exception;

    /**
     * 删除用户
     *
     * @param userid
     */
    public void deleteUser(String userid) throws Exception;

    /**
     * 修改用户账号可用状态
     *
     * @param pd
     * @throws Exception
     */
    public void editIsenabled(PageData pd) throws Exception;

    /**
     * 添加用户
     *
     * @param pd
     * @throws Exception
     */
    public void insertUser(PageData pd) throws Exception;

    /**
     * 获取最大id
     */
    public String findMaxuserId(PageData pd) throws Exception;
}
