package com.audioweb.util;

/**
 * 项目名称：
 */
public class Const {
    public static final String SESSION_SECURITY_CODE = "sessionSecCode";//验证码
    public static final String SESSION_USER = "sessionUser";            //session用的用户
    public static final String SESSION_MENU_RIGHTS = "sessionMenuRights";
    //	public static final String sSESSION_ROLE_RIGHTS = "sessionRoleRights";
    public static final String SESSION_menuList = "menuList";            //当前菜单
    public static final String SESSION_allmenuList = "allmenuList";        //全部菜单
    public static final String SESSION_nousermenuList = "nousermenuList";//未登录时的菜单
    public static final String SESSION_QX = "QX";
    public static final String SESSION_nouserQX = "nouserQX";
    //	public static final String SESSION_userpds = "userpds";
    public static final String SESSION_USERID = "USERID";                //用户对象
    public static final String SESSION_USERNAME = "USERNAME";            //用户名
    public static final String SESSION_LOGINID = "LOGINID";            //登陆名
    public static final String SESSION_USERROLEID = "USERROLEID";            //用户角色
    public static final String SESSION_USERLEVEL = "USERLEVEL";            //用户级别
    public static final String SESSION_USERAREALIST = "USERAREALIST";            //用户所管理的所有下属区域列表(用户管理用)
    public static final String TRUE = "T";
    public static final String FALSE = "F";
    public static final String LOGIN = "/login_toLogin.do";                //登录地址
    public static final String SYSNAME = "admin/config/SYSNAME.txt";    //系统名称路径
    public static final String PAGE = "admin/config/PAGE.txt";            //分页条数配置路径
    public static final String WEBSOCKET = "admin/config/WEBSOCKET.txt";//WEBSOCKET配置路径
    public static final String FILEPATH = "uploadFiles/";    //文件路径
    public static final String FILEPATHFILE = "uploadFiles/file/";        //文件上传路径
    public static final String FILEPATHPER = "uploadFiles/perfile/";    //节目文件夹
    public static final String CONFIG = "admin/config/config.properties";    //终端交互端口配置路径
    public static final String NO_INTERCEPTOR_PATH = ".*/((login)|(logout)|(code)|(app)|(weixin)|(static)|(main)|(websocket)).*";    //不对匹配该值的访问路径拦截（正则）
    public static final String SESSION_FID = "FID";

    public static final String SESSION_FNAME = "FNAME";

    public static final String[] LOGTYPE = new String[]{"登陆日志", "操作日志", "后台日志"};
    public static final String[] CASTTYPE = {"文件广播", "定时广播", "实时采播", "终端采播", "终端点播", "寻呼话筒", "控件广播"};
}
