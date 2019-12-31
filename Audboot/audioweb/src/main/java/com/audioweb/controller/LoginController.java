package com.audioweb.controller;

import com.audioweb.entity.*;
import com.audioweb.service.*;
import com.audioweb.util.*;
import com.niocast.entity.MonitorInfoBean;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;
import net.sf.json.JSONArray;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class LoginController extends BaseController {

    @Resource(name = "usersService")
    private UsersManager usersService;
    @Resource(name = "menuService")
    private MenuManager menuService;
    @Resource(name = "roleService")
    private RolesManager roleService;
    @Resource(name = "systemService")
    private SystemManager systemService;
    @Resource(name = "monitorService")
    private IMonitorManager monitorService;
    @Resource(name = "scheTaskService")
    private ScheTaskManager scheTaskService;
    @Resource(name = "scheduleService")
    private SchedulesManager scheduleService;

    /**
     * 访问登录页
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login_toLogin")
    public ModelAndView toLogin() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
//		pd.put("SYSNAME", Tools.readTxtFile(Const.SYSNAME)); //读取系统名称
        pd.put("SYSNAME", systemService.getBaseAttri("系统名称")); //读取系统名称
        pd.put("SYSOWNER", systemService.getBaseAttri("系统所有者"));
        mv.setViewName("index/login");
        mv.addObject("pd", pd);
        logger.info("*************" + GlobalInfoController.SERVERIP + "," + GlobalInfoController.GATEWAY + "," + GlobalInfoController.NETMASK);
        return mv;
    }

    /**
     * 请求登录，验证用户
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login_login", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Object login() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String errInfo = "";
        String KEYDATA[] = pd.getString("KEYDATA").replaceAll("infopublic", "").split(",info,");
        if (null != KEYDATA && KEYDATA.length == 3) {
            Session session = Jurisdiction.getSession();
            String sessionCode = (String) session.getAttribute(Const.SESSION_SECURITY_CODE);        //获取session中的验证码
            String code = KEYDATA[2];
            if (null == code || "".equals(code)) {//判断效验码
                errInfo = "nullcode";            //效验码为空
            } else {
                String username = KEYDATA[0];    //登录过来的用户名
                String password = KEYDATA[1];    //登录过来的密码
                pd.put("loginid", username);
                Users user = new Users();
                if (Tools.notEmpty(sessionCode) && sessionCode.equalsIgnoreCase(code)) {        //判断登录验证码
                    String passwd = new SimpleHash("SHA-1", username, password).toString();    //密码加密
                    pd.put("password", passwd);
                    user = usersService.getUserByLoginAndPwd(pd);    //根据用户名和密码去读取用户信息
                    if (user != null) {
                        session.setAttribute(Const.SESSION_USER, user);            //把用户信息放session中
                        session.setAttribute(Const.SESSION_USERID, user.getUserid());                //放入用户名到session
                        session.setAttribute(username + Const.SESSION_USERROLEID, user.getRoleId());            //把用户信息放session中
                        session.removeAttribute(Const.SESSION_SECURITY_CODE);    //清除登录验证码的session
                        //shiro加入身份验证
                        Subject subject = SecurityUtils.getSubject();
                        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
                        try {
                            subject.login(token);
                        } catch (AuthenticationException e) {
                            errInfo = "身份验证失败！";
                        }
                    } else {
                        errInfo = "usererror";                //用户名或密码有误
                        logBefore(username + "登录系统密码或用户名错误");
                    }
                } else {
                    errInfo = "codeerror";                    //验证码输入有误
                }
                if (Tools.isEmpty(errInfo)) {
                    errInfo = "success";                    //验证成功
                    logBefore(username + "登录系统");
                    //插入日志
                    saveLog(Const.LOGTYPE[0], "系统登录", "", this.getRemortIP(), username);
                }
            }
        } else {
            errInfo = "error";    //缺少参数
        }
        map.put("result", errInfo);
        return AppUtil.returnObject(new PageData(), map);
    }


    /**
     * 访问首页
     *
     * @return
     */
    @RequestMapping(value = "/main/index")
    @SuppressWarnings("unchecked")
    public ModelAndView main_index() {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Session session = Jurisdiction.getSession();
            Users user = (Users) session.getAttribute(Const.SESSION_USER);    //读取session中的用户信息(单独用户信息)
            String roleRights = "";
            List<Menu> allmenuList = new ArrayList<Menu>();
            if (user != null) {
                String username = user.getUsername();
                String loginid = user.getLoginid();
                roleRights = user.getMenuRights();
                session.setAttribute(Const.SESSION_USERLEVEL, user.getRoleLevel());                //放入用户级别到session
                session.setAttribute(username + Const.SESSION_MENU_RIGHTS, roleRights); //将菜单权限存入session
                session.setAttribute(Const.SESSION_USERNAME, username);                //放入用户名到session
                session.setAttribute(Const.SESSION_LOGINID, loginid);                //放入用户名到session

                if (null == session.getAttribute(username + Const.SESSION_allmenuList)) {
                    allmenuList = menuService.listAllMenu("0");                    //获取所有菜单
                    if (Tools.notEmpty(roleRights)) {
                        if (user.getRealcasttype() == 10) {
                            allmenuList = this.readMenu(allmenuList, roleRights, true);        //根据角色权限获取本权限的菜单列表
                        } else {
                            allmenuList = this.readMenu(allmenuList, roleRights, false);        //根据角色权限获取本权限的菜单列表
                        }
                    }
                    session.setAttribute(username + Const.SESSION_allmenuList, allmenuList);//菜单权限放入session中
                } else {
                    allmenuList = (List<Menu>) session.getAttribute(username + Const.SESSION_allmenuList);
                }
                if (user.getRealcasttype() == 10) {//为采播本地模式

                }
                mv.setViewName("index/main");
                mv.addObject("user", user);
                mv.addObject("systemtime", df.format(new Date()));
                mv.addObject("menuList", allmenuList);
            } else {
                mv.setViewName("index/login");//session失效后跳转登录
            }
        } catch (Exception e) {
            mv.setViewName("index/login");
            logError(e);
        }
//		pd.put("SYSNAME", Tools.readTxtFile(Const.SYSNAME)); //读取系统名称
        try {
            pd.put("SYSNAME", systemService.getBaseAttri("系统名称"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logError(e);
        }
        mv.addObject("pd", pd);
        return mv;
    }

    /**
     * 根据菜单权限获取本权限的菜单列表(递归处理)
     *
     * @param menuList：传入的总菜单
     * @param menuRights：权限字符串
     * @return
     */
    public List<Menu> readMenu(List<Menu> menuList, String menuRights, boolean realcast) {
        for (int i = 0; i < menuList.size(); i++) {
            menuList.get(i).setHasMenu(RightsHelper.testRights(menuRights, menuList.get(i).getMid()));
            if (realcast && menuList.get(i).getMid().equals("14")) {
                menuList.get(i).setMurl("realtimecast/openFile");
                menuList.get(i).setRefresh(true);
            }
            this.readMenu(menuList.get(i).getSubMenu(), menuRights, realcast);                    //是：继续排查其子菜单
        }
        return menuList;
    }

    /**
     * 进入tab标签
     *
     * @return
     */
    @RequestMapping(value = "/tab")
    public String tab() {
        return "index/tab";
    }

    /**
     * 进入首页后的默认页面
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login_default")
    public ModelAndView defaultPage() throws Exception {
        String userRid = Jurisdiction.getUserRid();
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pd.put("SYSNAME", systemService.getBaseAttri("系统名称"));
        pd.put("sys", "true");
        int pass = 0, nopass = 0, online = 0;
        Map<String, TerminalInfo> tInfos = GlobalInfoController.getTerinfoMap();
        for (Map.Entry<String, TerminalInfo> tInfo : tInfos.entrySet()) {
            if (tInfo.getValue().getIsOnline() || tInfo.getValue().getIstrueOnline()) {
                pass++;
            } else {
                nopass++;
            }
        }
        online = Jurisdiction.getSessions().size();
        Page page = new Page();
        page.setPd(pd);
        List<ScheTask> tasks = scheTaskService.getScheTaskByScheId(page);
        mv.addObject("tasks", tasks);
//		pd = userService.getUserCount("");
//		pd.put("userSuperCount", Integer.parseInt(userService.getUserCount("").get("userCount").toString())-1);				//系统用户数
//		pd.put("userAreaCount", Integer.parseInt(userService.getUserCount("").get("userCount").toString())-1);				//系统用户数
//		pd.put("userNomalCount", Integer.parseInt(userService.getUserCount("").get("userCount").toString())-1);				//系统用户数
        //List<MonitorInfoBean> beans = GlobalInfoController.getmInfoBeans();
        mv.addObject("pd", pd);
        mv.addObject("pass", pass);
        mv.addObject("nopass", nopass);
        mv.addObject("online", online);
        mv.addObject("jurisToLog", userRid.equals("1") ? true : false);
        mv.setViewName("index/default");
        return mv;
    }
    /**
     * 用户注销
     * @param session
     * @return
     */
	/*@RequestMapping(value="/logout")
	public ModelAndView logout(){
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("msg","1");
		ModelAndView mv = this.getModelAndView();
		try {
			String USERNAME = Jurisdiction.getUsername();	//当前登录的用户名
			logBefore(logger, USERNAME+"退出系统");
			Session session = Jurisdiction.getSession();	//以下清除session缓存
			if(session != null) {
				session.removeAttribute(Const.SESSION_USER);
				session.removeAttribute(USERNAME + Const.SESSION_MENU_RIGHTS);
				session.removeAttribute(USERNAME + Const.SESSION_allmenuList);
				session.removeAttribute(USERNAME + Const.SESSION_menuList);
				session.removeAttribute(USERNAME + Const.SESSION_QX);
				session.removeAttribute(USERNAME + Const.SESSION_USERROLEID);
				session.removeAttribute(USERNAME + Const.SESSION_USERAREALIST);
				session.removeAttribute(Const.SESSION_LOGINID);
				session.removeAttribute(Const.SESSION_USERNAME);
		//		session.removeAttribute(Const.SESSION_USERROL);
				session.removeAttribute("changeMenu");
			}
			//shiro销毁登录
			Subject subject = SecurityUtils.getSubject(); 
			subject.logout();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pd.put("msg", pd.getString("msg") != null?pd.getString("msg"):"");
//		pd.put("SYSNAME", Tools.readTxtFile(Const.SYSNAME)); //读取系统名称
		try {
			pd.put("SYSNAME", systemService.getBaseAttri("系统名称"));
		} catch (Exception e) {
			e.printStackTrace();
		} //读取系统名称
		mv.setViewName("index/login");
		mv.addObject("pd",pd);
		return mv;
	}*/
    /**获取用户按钮权限
     * @param session
     * @return
     */
	/*public Map<String, String> getUQX(String username){
		PageData pd = new PageData();
		Map<String, String> map = new HashMap<String, String>();
		try {
			map.put("adds", pd.getString("ADD_QX"));	//增
			map.put("dels", pd.getString("DEL_QX"));	//删
			map.put("edits", pd.getString("EDIT_QX"));	//改
			map.put("chas", pd.getString("CHA_QX"));	//查
			List<PageData> buttonQXnamelist = new ArrayList<PageData>();
			if("admin".equals(username)){
//				buttonQXnamelist = buttonService.listAll(pd);					//admin用户拥有所有按钮权限
			}else{
//				buttonQXnamelist = buttonrightsService.listAllBrAndQxname(pd);	//此角色拥有的按钮权限标识列表
			}
			for(int i=0;i<buttonQXnamelist.size();i++){
				map.put(buttonQXnamelist.get(i).getString("QX_NAME"),"1");		//按钮权限
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}	
		return map;
	}*/

    /**
     * 前端定时获取后台信息
     *
     * @param @return
     * @param @throws Exception
     * @return Object
     * @Title: upTaskEnable
     * @Description: TODO
     */
    @RequestMapping("/getSysInfo")
    @ResponseBody
    public Object getSysInfo() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        //String info = (null == pd.get("info") || "".equals(pd.get("info").toString()))?"":pd.get("info").toString();
        String result = "";
        try {
            pd.put("sys", "true");
            Page page = new Page();
            page.setPd(pd);
            JSONArray tasks = JSONArray.fromObject(scheTaskService.getScheTaskByScheId(page));
            String json = tasks.toString();
            map.put("tasks", json);
        } catch (Exception e) {
            logError(e);
            result += "task";
        }
        try {
            int pass = 0, nopass = 0;
            Map<String, TerminalInfo> tInfos = GlobalInfoController.getTerinfoMap();
            for (Map.Entry<String, TerminalInfo> tInfo : tInfos.entrySet()) {
                if (tInfo.getValue().getIsOnline() || tInfo.getValue().getIstrueOnline()) {
                    pass++;
                } else {
                    nopass++;
                }
            }
            map.put("pass", String.valueOf(pass));
            map.put("nopass", String.valueOf(nopass));
        } catch (Exception e) {
            logError(e);
            result += "pass";
        }
        try {
            JSONArray allsysinfo = JSONArray.fromObject(GlobalInfoController.getmInfoBeans());
            String json = allsysinfo.toString();
            map.put("info", json);
        } catch (Exception e) {
            logError(e);
            result += "bean";
        }
        if (result.equals("")) {
            result = "success";
        }
        map.put("result", result);
        return map;
    }

    /**
     * 查询系统信息
     *
     * @param @throws Exception
     * @return ModelAndView
     */
    @RequestMapping(value = "/sysInfo")
    public ModelAndView listSysInfo(Page page) throws Exception {
        ModelAndView mv = this.getModelAndView();
        List<MonitorInfoBean> beans = GlobalInfoController.getmInfoBeans();
        mv.addObject("beans", beans);
        mv.setViewName("index/sysinfo");
        return mv;
    }

    /**
     * 查询所有日志
     *
     * @param @throws Exception
     * @return ModelAndView
     */
    @RequestMapping(value = "/logs")
    public ModelAndView listAllLogs(Page page) throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        String start = (null == pd.get("StartDateTime") || "".equals(pd.get("StartDateTime").toString())) ? "" : pd.get("StartDateTime").toString();
        String end = (null == pd.get("EndDateTime") || "".equals(pd.get("EndDateTime").toString())) ? "" : pd.get("EndDateTime").toString();
        try {
            if (!start.equals("")) {
                pd.put("Start", start + " 00:00:00");
            }
            if (!end.equals("")) {
                pd.put("End", end + " 23:59:59");
            }
            page.setPd(pd);
            List<Log> logs = logservice.getLogs(page);
            mv.addObject("pd", pd);
            mv.addObject("logs", logs);
            mv.setViewName("index/logs");
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return mv;
    }
//	/** 更新登录用户的IP
//	 * @param USERNAME
//	 * @throws Exception
//	 */
//	public void getRemortIP(String USERNAME) throws Exception {  
//		PageData pd = new PageData();
//		HttpServletRequest request = this.getRequest();
//		String ip = "";
//		if (request.getHeader("x-forwarded-for") == null) {  
//			ip = request.getRemoteAddr();  
//	    }else{
//	    	ip = request.getHeader("x-forwarded-for");  
//	    }
//		pd.put("USERNAME", USERNAME);
//		pd.put("IP", ip);
//		userService.saveIP(pd);
//	}  

}
