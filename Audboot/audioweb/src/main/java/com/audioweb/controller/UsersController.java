/**
 *
 */
package com.audioweb.controller;

/**
 * @author shuofang
 */

import com.audioweb.entity.Menu;
import com.audioweb.entity.Page;
import com.audioweb.entity.Roles;
import com.audioweb.entity.Users;
import com.audioweb.service.MenuManager;
import com.audioweb.service.RolesManager;
import com.audioweb.service.UsersManager;
import com.audioweb.util.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类名称：UsersController
 *
 * @version
 */
@Controller
@RequestMapping(value = "/users")
public class UsersController extends BaseController {

    @Resource(name = "usersService")
    private UsersManager usersService;
    @Resource(name = "roleService")
    private RolesManager roleService;
    @Resource(name = "menuService")
    private MenuManager menuService;

    /**
     * 去角色授权界面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/editRoleRight")
    public ModelAndView editRoleRight(Model model) throws Exception {
        //String userRid = Jurisdiction.getUserRid();
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        try {
            List<Menu> menuList = menuService.listAllMenu("0");
            String roleId = pd.getString("rid");
            Roles roles = roleService.findrolebyRoleId(roleId);
            String menuRights = "";
            if (roles != null) {
                menuRights = roles.getMenuRights();
            }
            menuList = this.readMenu(menuList, menuRights); // 根据角色权限处理菜单权限状态(递归处理)
            JSONArray arr = JSONArray.fromObject(menuList);
            for (int i = 0; i < arr.size(); i++) {
                JSONObject menu = (JSONObject) arr.get(i);
                menu.element("open", true);
            }
            String json = arr.toString();
            json = json.replaceAll("parentmid", "pId").replaceAll("mid", "id").replaceAll("mname", "name")
                    .replaceAll("subMenu", "children").replaceAll("hasMenu", "checked");
            model.addAttribute("zTreeNodes", json);
            mv.addObject("rid", pd.get("rid"));
            mv.setViewName("user/role_accredit");
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return mv;
    }

    /**
     * 保存菜单权限
     *
     * @param rid
     *            角色ID
     * @param menuIds
     *            菜单ID集合
     * @param out
     * @throws Exception
     */
    @RequestMapping(value = "/accredit")
    @ResponseBody
    public Object accredit() throws Exception {
        logBefore(Jurisdiction.getUsername() + "修改菜单权限");
        PageData pd = new PageData();
        pd = this.getPageData();
        Map<String, String> map = new HashMap<String, String>();
        String returnData = "error";
        try {
            String menuIds = pd.getString("menuIds");
            if (null != menuIds && !"".equals(menuIds.trim())) {
                BigInteger rights = RightsHelper.sumRights(Tools.str2StrArray(menuIds));// 用菜单ID做权处理
                pd.put("MenuRights", rights.toString());
                roleService.editRoleRights(pd); // 更新当前用户菜单权限
            } else {
                pd.put("MenuRights", "");
                roleService.editRoleRights(pd); // 更新当前角色菜单权限(没有任何勾选)
            }
            // 插入日志
            saveLog(Const.LOGTYPE[1], "角色管理", "授权", this.getRemortIP(), pd.getString("RoleId"));
            returnData = "success";
        } catch (Exception e) {
            logError(e);
        }
        map.put("result", returnData);
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 根据菜单权限获取本权限的菜单列表(递归处理)
     *
     * @param menuList：传入的总菜单
     * @param menuRights：权限字符串
     * @return
     */
    public List<Menu> readMenu(List<Menu> menuList, String menuRights) {
        for (int i = 0; i < menuList.size(); i++) {
            menuList.get(i).setHasMenu(RightsHelper.testRights(menuRights, menuList.get(i).getMid()));
            this.readMenu(menuList.get(i).getSubMenu(), menuRights); // 是：继续排查其子菜单
        }
        return menuList;
    }

    /**
     * 查询所有角色
     *
     * @param @throws
     *            Exception
     * @return ModelAndView
     */
    @RequestMapping(value = "/listRole")
    public ModelAndView listRole(Page page) throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("users/listRole.do")) {
            return logout();
        } // 校验权限
        String userRid = Jurisdiction.getUserRid();
        Roles roles = roleService.findrolebyRoleId(userRid);
        PageData pd = new PageData();
        pd = this.getPageData();
        pd.put("RoleLevel", Integer.parseInt(roles.getRoleLevel()));
        page.setPd(pd);
        List<Roles> rolelist = roleService.listRoles(page);// 用户列表
        mv.addObject("rolelist", rolelist);
        mv.addObject("pd", pd);
        mv.setViewName("user/role_list");
        return mv;
    }

    /**
     * 查询所有用户
     *
     * @param @throws
     *            Exception
     * @return ModelAndView
     */
    @RequestMapping(value = "/listAllUsers")
    public ModelAndView listAllUsers(Page page) throws Exception {
        ModelAndView mv = this.getModelAndView();
        /*String userid = Jurisdiction.getUserid();*/
        if (!Jurisdiction.hasJurisdiction("users/listAllUsers.do")) {
            return logout();
        } // 校验权限
        String userRid = Jurisdiction.getUserRid();
        Roles roles = roleService.findrolebyRoleId(userRid);
        PageData pd = new PageData();
        pd = this.getPageData();
        pd.put("RoleLevel", roles.getRoleLevel());
        page.setPd(pd);
        List<Users> userlist = usersService.getUserList(page);// 用户列表
        mv.addObject("userlist", userlist);
        mv.addObject("pd", pd);
        mv.setViewName("user/user_list");
        return mv;
    }

    /**
     * 去添加用户信息
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addUser")
    public ModelAndView addUser() throws Exception {
        ModelAndView mv = this.getModelAndView();
        /*String userid = Jurisdiction.getUserid();*/
        if (!Jurisdiction.hasJurisdiction("users/listAllUsers.do")) {
            return logout();
        } // 校验权限
        String userRid = Jurisdiction.getUserRid();
        Roles roles = roleService.findrolebyRoleId(userRid);
        PageData pd = new PageData();
        pd = this.getPageData();
        try {

            pd.put("RoleLevel", roles.getRoleLevel());
            List<Roles> rolelist = roleService.listAllRoles(pd);
            mv.addObject("rolelist", rolelist);
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
        }
        mv.setViewName("user/user_edit");
        mv.addObject("MSG", "addU");
        return mv;
    }

    /**
     * 添加用户信息
     */
    @RequestMapping(value = "/addU")
    @ResponseBody
    public Object addU() throws Exception {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        Date date = new Date();
        pd.put("CreatDate", sf.format(date));
        pd.put("LastPasswordChangeDate", sf.format(date));
        pd.put("userid", Integer.parseInt(usersService.findMaxuserId(pd)) + 1);
        pd.put("password", new SimpleHash("SHA-1", pd.getString("loginid"), pd.getString("password")).toString());
        usersService.insertUser(pd);
        // 插入日志
        saveLog(Const.LOGTYPE[1], "用户管理", "添加", this.getRemortIP(), pd.getString("username"));

        map.put("result", "success");
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 去修改用户信息
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/editUser")
    public ModelAndView editUser() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        String userRid = Jurisdiction.getUserRid();
        Roles roles = roleService.findrolebyRoleId(userRid);
        pd.put("RoleLevel", roles.getRoleLevel());
        Users user = usersService.findByUserid(pd); // 根据ID读取
        List<Roles> rolelist = roleService.listAllRoles(pd);
        mv.setViewName("user/user_edit");
        mv.addObject("from", pd.get("from"));
        mv.addObject("MSG", "editU");
        mv.addObject("rolelist", rolelist);
        mv.addObject("user", user);
        return mv;
    }

    /**
     * 修改用户个人信息
     */
    @RequestMapping(value = "/editU")
    @ResponseBody
    public Object editU() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        usersService.editUser(pd); // 执行修改
        // 插入日志
        saveLog(Const.LOGTYPE[1], "用户管理", "修改", this.getRemortIP(), pd.getString("userid"));

        map.put("result", "success");
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 去修改用户角色
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/editRole")
    public ModelAndView editRole() throws Exception {
        String userRid = Jurisdiction.getUserRid();
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        String rid = (null == pd.get("RoleId") || "".equals(pd.get("RoleId").toString())) ? "" : pd.get("RoleId").toString();
        Roles role = roleService.findrolebyRoleId(rid);
        mv.setViewName("user/role_edit");
        mv.addObject("RoleId", role.getRoleId());
        mv.addObject("RoleName", role.getRoleName());
        mv.addObject("RoleLevel", role.getRoleLevel());
        mv.addObject("Description", role.getDescription());
        mv.addObject("UserRoleLevel", Jurisdiction.getUserlevel());
        mv.addObject("MSG", "editR");
        return mv;
    }

    /**
     * 修改用户角色
     */
    @RequestMapping(value = "/editR")
    @ResponseBody
    public Object editR() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        roleService.editRole(pd); // 执行修改
        // 插入日志
        saveLog(Const.LOGTYPE[1], "角色管理", "修改", this.getRemortIP(), pd.getString("rid"));

        map.put("result", "success");
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 去添加用户角色
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addRole")
    public ModelAndView addRole() throws Exception {
        String userRid = Jurisdiction.getUserRid();
        Roles roles = roleService.findrolebyRoleId(userRid);
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        mv.setViewName("user/role_edit");
        mv.addObject("pd", pd);
        mv.addObject("UserRoleLevel", roles.getRoleLevel());
        mv.addObject("MSG", "addR");
        return mv;
    }

    /**
     * 添加用户角色
     */
    @RequestMapping(value = "/addR")
    @ResponseBody
    public Object addR() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        int userRid = Integer.parseInt(Jurisdiction.getUserRid());
        pd = this.getPageData();
        String menuIds = pd.getString("menuRights");
        String newRid = roleService.findmaxIdbyRoleId(String.valueOf(userRid)).get(0).get("RoleId").toString();// 获取本用户下最大Rid号
        if (Integer.parseInt(newRid) == userRid) {
            newRid = newRid + "01";
        } else {
            newRid = String.valueOf(Integer.parseInt(newRid) + 1);
        }
        pd.put("RoleId", newRid);
        roleService.addRole(pd); // 执行修改
        if (null != menuIds && !"".equals(menuIds.trim())) {//角色授权
            BigInteger rights = RightsHelper.sumRights(Tools.str2StrArray(menuIds));// 用菜单ID做权处理
            pd.put("MenuRights", rights.toString());
            roleService.editRoleRights(pd); // 更新当前用户菜单权限
        } else {
            pd.put("MenuRights", "");
            roleService.editRoleRights(pd); // 更新当前角色菜单权限(没有任何勾选)
        }
        // 插入日志
        saveLog(Const.LOGTYPE[1], "角色管理", "添加", this.getRemortIP(), pd.getString("rname"));

        map.put("result", "success");
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 删除用户
     *
     * @param out
     * @throws Exception
     */
    @RequestMapping(value = "/deleteU")
    @ResponseBody
    public Object deleteU() throws Exception {
        logBefore(Jurisdiction.getUsername() + "删除user");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        try {
            usersService.deleteUser(pd.getString("userid")); // 要删除的用户id
            // 插入日志
            saveLog(Const.LOGTYPE[1], "用户管理", "删除", this.getRemortIP(), pd.getString("userid"));
            map.put("result", "success");
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
            map.put("result", "error");
        }
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 删除角色
     *
     * @param out
     * @throws Exception
     */
    @RequestMapping(value = "/deleteRole")
    @ResponseBody
    public Object deleteRole() throws Exception {
        PageData pd = new PageData();
        Map<String, String> map = new HashMap<String, String>();
        pd = this.getPageData();
        try {
            roleService.deleteRole(pd.getString("rid")); // 要删除的用户id
            // 插入日志
            saveLog(Const.LOGTYPE[1], "角色管理", "删除", this.getRemortIP(), pd.getString("rid"));
            map.put("result", "success");
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
            map.put("result", "error");
        }
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 导出用户信息到EXCEL
     *
     * @return
     */
    /*
     * @RequestMapping(value="/excel") public ModelAndView exportExcel(){
     * ModelAndView mv = this.getModelAndView(); PageData pd = new PageData(); pd =
     * this.getPageData(); pd.put("userid", Jurisdiction.getUserid()); String ids =
     * pd.getString("checkedidlist"); if(null != ids && !"".equals(ids)){ String
     * Arrayuserids[] = ids.split(","); List<String> userids = new
     * ArrayList<String>(); for(int i=0;i<Arrayuserids.length;i++){
     * userids.add(Arrayuserids[i]); } pd.put("userids", userids); }else{
     * pd.put("userids", null); } try{ // List<User> userList =
     * userService.getUserList(pd); List<Users> userList =
     * usersService.getUserList(pd); Map<String,Object> dataMap = new
     * HashMap<String,Object>(); List<String> titles = new ArrayList<String>();
     * titles.add("用户姓名"); //1 titles.add("用户类型"); //2 titles.add("登陆账号"); //3
     * titles.add("所属区域"); //4 titles.add("用户角色"); //4 titles.add("联系电话"); //5
     * titles.add("联系地址"); //6 titles.add("账号状态"); //7 titles.add("备注"); //8
     * dataMap.put("titles", titles); List<PageData> varList = new
     * ArrayList<PageData>(); for(Users user : userList){ PageData vpd = new
     * PageData(); vpd.put("var1", user.getUserid()); //1 vpd.put("var2",
     * user.getUsertypes()); //2 vpd.put("var3", user.getLoginid()); //3
     * vpd.put("var4", user.getAname()); //4 vpd.put("var5", user.getRname()); //4
     * vpd.put("var6", user.getLinktel()); //5 vpd.put("var7", user.getLinkaddr());
     * //6 vpd.put("var8", user.getIsuse()?"正常":"停用"); //7 vpd.put("var9",
     * user.getNote()); //8 varList.add(vpd); } dataMap.put("varList", varList);
     * ObjectExcelView erv = new ObjectExcelView(); //执行excel操作 mv = new
     * ModelAndView(erv,dataMap); // } } catch(Exception e){
     * logger.error(e.toString(), e); } return mv; }
     */

    /**
     * 修改用户账号是否可用
     *
     * @Title: upEnable
     * @Description: TODO
     * @param @return
     * @param @throws
     *            Exception
     * @return Object
     */
    @RequestMapping("/upEnable")
    @ResponseBody
    public Object upEnable() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        String returndata = "error";
        pd = this.getPageData();
        try {
            Boolean isenabled = Boolean.parseBoolean(pd.get("isuse").toString());
            pd.put("isuse", isenabled ? 1 : 0);
            usersService.editIsenabled(pd); // 执行修改
            // 插入日志
            saveLog(Const.LOGTYPE[1], "用户管理", "修改", this.getRemortIP(), pd.getString("userid"));
            returndata = "success";
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
        }
        map.put("result", returndata);
        return map;
    }

    /**
     * 去修改密码页面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/goEditPassword")
    public ModelAndView goEditPassword() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pd.put("userid", Jurisdiction.getUserid());
        Users user = usersService.findByUserid(pd);
        mv.setViewName("user/pwd_edit");
        mv.addObject("msg", "editPassword");
        mv.addObject("user", user);
        return mv;
    }

    /**
     * 修改用户密码
     */
    @RequestMapping(value = "/editUserPwd")
    @ResponseBody
    public Object editPassword() throws Exception {
        logBefore(Jurisdiction.getUsername() + "修改user密码");
        Map<String, String> map = new HashMap<String, String>();
        //ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pd.put("oldpassword", new SimpleHash("SHA-1", Jurisdiction.getLoginid(), pd.getString("oldpwd")).toString());
        pd.put("password", new SimpleHash("SHA-1", Jurisdiction.getLoginid(), pd.getString("newpwd")).toString());
        pd.put("userid", Jurisdiction.getUserid());
        usersService.editPwd(pd);
        // 插入日志
        saveLog(Const.LOGTYPE[1], "系统设置", "修改个人密码", this.getRemortIP(), pd.getString("userid"));
        map.put("result", "success");
        return map;
    }

    /**
     * 判断登录名是否存在
     */
    @RequestMapping(value = "/checkloginid")
    @ResponseBody
    public Object checkloginid() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String userid = usersService.checkLoginid(pd.getString("loginid"));
        if (userid != null && userid != "" && !userid.equals(pd.getString("userid"))) {
            map.put("result", "error");
        } else {
            map.put("result", "success");
        }
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 判断用户密码
     */
    @RequestMapping(value = "/judgePwd")
    @ResponseBody
    public Object judgePwd() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        pd.put("password", new SimpleHash("SHA-1", Jurisdiction.getLoginid(), pd.getString("password")).toString());
        pd.put("loginid", Jurisdiction.getLoginid());
        Users user = usersService.getUserByLoginAndPwd(pd);
        if (user != null) {
            map.put("userid", user.getUserid());
            map.put("result", "success");
            return AppUtil.returnObject(pd, map);
        }
        return null;
    }
    /**
     * 修改用户密码
     *//*
	@RequestMapping(value = "/editUserPwd")
	@ResponseBody
	public Object editUserPwd() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("oldpassword", new SimpleHash("SHA-1", Jurisdiction.getLoginid(), pd.getString("oldpwd")).toString());
		pd.put("password", new SimpleHash("SHA-1", Jurisdiction.getLoginid(), pd.getString("newpwd")).toString());
		pd.put("loginid", Jurisdiction.getLoginid());
		usersService.editPwd(pd);
		map.put("result", "success");
		return AppUtil.returnObject(pd, map);
		
	}*/
}
