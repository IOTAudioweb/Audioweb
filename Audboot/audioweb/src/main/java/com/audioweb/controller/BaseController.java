package com.audioweb.controller;

import com.audioweb.entity.Page;
import com.audioweb.service.SystemManager;
import com.audioweb.util.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Shuofang
 * TODO 基本的控制器
 */
public class BaseController extends BaseLogger<BaseController> {
    @Resource(name = "systemService")
    protected SystemManager systemService;

    //protected Logger logger = Logger.getLogger(this.getClass());

    // private static final long serialVersionUID = 6357869213649815390L;

    /**
     * new PageData对象
     *
     * @return
     */
    public PageData getPageData() {
        return new PageData(this.getRequest());
    }

    /**
     * 得到ModelAndView
     *
     * @return
     */
    public ModelAndView getModelAndView() {
        return new ModelAndView();
    }

    /**
     * 得到request对象
     *
     * @return
     */
    public HttpServletRequest getRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        return request;
    }

    /**
     * 得到32位的uuid
     *
     * @return
     */
    public String get32UUID() {
        return UuidUtil.get32UUID();
    }

    /**
     * 得到分页列表的信息
     *
     * @return
     */
    public Page getPage() {
        return new Page();
    }

    /**
     * 获取IP
     *
     * @throws Exception
     */
    public String getRemortIP() throws Exception {
        HttpServletRequest request = this.getRequest();
        String ip = "";
        if (request.getHeader("x-forwarded-for") == null) {
            ip = request.getRemoteAddr();
        } else {
            ip = request.getHeader("x-forwarded-for");
        }
        return ip;
    }

    /**
     * 用户注销
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "/logout")
    public ModelAndView logout() {
        PageData pd = new PageData();
        pd = this.getPageData();
        //pd.put("MSG","1");
        ModelAndView mv = this.getModelAndView();
        try {
            String USERNAME = Jurisdiction.getUsername();    //当前登录的用户名
            logBefore(USERNAME + "退出系统");
            Session session = Jurisdiction.getSession();    //以下清除session缓存
            if (session != null) {
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
                List<Session> sessions = Jurisdiction.getSessions();
                for (Session sess : sessions) {
                    try {
                        if (sess.getStartTimestamp().equals(session.getStartTimestamp())) {
                            sessions.remove(sess);
                            break;
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        sessions.remove(sess);
                        logError(e);
                    }
                }
            }
            //shiro销毁登录
            Subject subject = SecurityUtils.getSubject();
            subject.logout();
        } catch (Exception e) {
            logError(e);
        }
//		pd.put("SYSNAME", Tools.readTxtFile(Const.SYSNAME)); //读取系统名称
        try {
            pd.put("MSG", pd.getString("MSG") != null ? pd.getString("MSG") : "");
            pd.put("SYSNAME", systemService.getBaseAttri("系统名称"));
            pd.put("SYSOWNER", systemService.getBaseAttri("系统所有者"));
        } catch (Exception e) {
            logError(e);
        } //读取系统名称
        mv.setViewName("index/login");
        mv.addObject("pd", pd);
        return mv;
    }
}
