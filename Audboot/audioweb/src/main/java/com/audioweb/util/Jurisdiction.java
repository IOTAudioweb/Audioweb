package com.audioweb.util;

import com.audioweb.entity.Menu;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限处理
 */
public class Jurisdiction {

    private static Logger logger = LoggerFactory.getLogger(Jurisdiction.class);
    private static List<Session> sessions = new ArrayList<>();

    /**
     * 访问权限及初始化按钮权限(控制按钮的显示 增删改查)
     *
     * @param menuUrl 菜单路径
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean hasJurisdiction(String menuUrl) {
        //判断是否拥有当前点击菜单的权限（内部过滤,防止通过url进入跳过菜单权限）
        /**
         * 根据点击的菜单的xxx.do去菜单中的URL去匹配，当匹配到了此菜单，判断是否有此菜单的权限，没有的话跳转到404页面
         * 根据按钮权限，授权按钮(当前点的菜单和角色中各按钮的权限匹对)
         */
        String USERNAME = getUsername();    //获取当前登录者loginname
        Session session = getSession();
        List<Menu> menuList = (List<Menu>) session.getAttribute(USERNAME + Const.SESSION_allmenuList); //获取菜单列表
        return readMenu(menuList, menuUrl, session, USERNAME);
    }

    /**
     * 校验菜单权限并初始按钮权限用于页面按钮显示与否(递归处理)
     *
     * @param menuList:传入的总菜单(设置菜单时，.do前面的不要重复)
     * @param menuUrl:访问地址
     * @return
     */
    public static boolean readMenu(List<Menu> menuList, String menuUrl, Session session, String username) {
        if (menuList == null) {
            return false;
        }
        for (int i = 0; i < menuList.size(); i++) {
            if (menuList.get(i).getMurl().split(".do")[0].equals(menuUrl.split(".do")[0])) { //访问地址与菜单地址循环匹配，如何匹配到就进一步验证，如果没匹配到就不处理(可能是接口链接或其它链接)
                if (!menuList.get(i).isHasMenu()) {            //判断有无此菜单权限
                    return false;
//				}else{											//按钮判断
//					Map<String, String> map = (Map<String, String>)session.getAttribute(username + Const.SESSION_QX);//按钮权限(增删改查)
//					map.remove("add");
//					map.remove("del");
//					map.remove("edit");
//					map.remove("cha");
//					String MENU_ID =  menuList.get(i).getMid();
//					Boolean isAdmin = "admin".equals(username);
//					map.put("add", (RightsHelper.testRights(map.get("adds"), MENU_ID)) || isAdmin?"1":"0");
//					map.put("del", RightsHelper.testRights(map.get("dels"), MENU_ID) || isAdmin?"1":"0");
//					map.put("edit", RightsHelper.testRights(map.get("edits"), MENU_ID) || isAdmin?"1":"0");
//					map.put("cha", RightsHelper.testRights(map.get("chas"), MENU_ID) || isAdmin?"1":"0");
//					session.removeAttribute(username + Const.SESSION_QX);
//					session.setAttribute(username + Const.SESSION_QX, map);	//重新分配按钮权限
//					return true;
                }
            } else {
                if (!readMenu(menuList.get(i).getSubMenu(), menuUrl, session, username)) {//继续排查其子菜单
                    return false;
                }
            }
        }
        return true;
    }

//	/**
//	 * 按钮权限(方法中校验)
//	 * @param menuUrl  菜单路径
//	 * @param type  类型(add、del、edit、cha)
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	public static boolean buttonJurisdiction(String menuUrl, String type){
//		//判断是否拥有当前点击菜单的权限（内部过滤,防止通过url进入跳过菜单权限）
//		/**
//		 * 根据点击的菜单的xxx.do去菜单中的URL去匹配，当匹配到了此菜单，判断是否有此菜单的权限，没有的话跳转到404页面
//		 * 根据按钮权限，授权按钮(当前点的菜单和角色中各按钮的权限匹对)
//		 */
//		String USERNAME = getUsername();	//获取当前登录者loginname
//		Session session = getSession();
//		List<Menu> menuList = (List<Menu>)session.getAttribute(USERNAME + Const.SESSION_allmenuList); //获取菜单列表
//		return readMenuButton(menuList,menuUrl,session,USERNAME,type);
//	}
//	
//	/**校验按钮权限(递归处理)
//	 * @param menuList:传入的总菜单(设置菜单时，.do前面的不要重复)
//	 * @param menuUrl:访问地址
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	public static boolean readMenuButton(List<Menu> menuList,String menuUrl,Session session,String USERNAME, String type){
//		for(int i=0;i<menuList.size();i++){
//			if(menuList.get(i).getMurl().split(".do")[0].equals(menuUrl.split(".do")[0])){ //访问地址与菜单地址循环匹配，如何匹配到就进一步验证，如果没匹配到就不处理(可能是接口链接或其它链接)
//				if(!menuList.get(i).isHasMenu()){				//判断有无此菜单权限
//					return false;
//				}else{											//按钮判断
//					Map<String, String> map = (Map<String, String>)session.getAttribute(USERNAME + Const.SESSION_QX);//按钮权限(增删改查)
//					String MENU_ID =  menuList.get(i).getMid();
//					Boolean isAdmin = "admin".equals(USERNAME);
//					if("add".equals(type)){
//						return ((RightsHelper.testRights(map.get("adds"), MENU_ID)) || isAdmin);
//					}else if("del".equals(type)){
//						return ((RightsHelper.testRights(map.get("dels"), MENU_ID)) || isAdmin);
//					}else if("edit".equals(type)){
//						return ((RightsHelper.testRights(map.get("edits"), MENU_ID)) || isAdmin);
//					}else if("cha".equals(type)){
//						return ((RightsHelper.testRights(map.get("chas"), MENU_ID)) || isAdmin);
//					}
//				}
//			}else{
//				if(!readMenuButton(menuList.get(i).getSubMenu(),menuUrl,session,USERNAME,type)){//继续排查其子菜单
//					return false;
//				};
//			}
//		}
//		return true;
//	}

    /**
     * 获取当前登录的用户名
     *
     * @return
     */
    public static String getUsername() {
        return getSession().getAttribute(Const.SESSION_USERNAME).toString();
    }

    /**
     * 获取当前登录的登陆名
     *
     * @return
     */
    public static String getLoginid() {
        return getSession().getAttribute(Const.SESSION_LOGINID).toString();
    }

    /**
     * 获取当前登录的用户编号
     *
     * @return
     */
    public static String getUserid() {
        return getSession().getAttribute(Const.SESSION_USERID).toString();
    }

    /**
     * 获取当前登录的用户编号
     *
     * @return
     */
    public static String getUserlevel() {
        return getSession().getAttribute(Const.SESSION_USERLEVEL).toString();
    }

    /**
     * 获取当前登录的用户角色
     *
     * @return
     */
    public static String getUserRid() {
        return getSession().getAttribute(getLoginid() + Const.SESSION_USERROLEID).toString();
    }

    /**
     * 获取当前登录的用户角色
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Menu> getMenuList() {
        return (List<Menu>) getSession().getAttribute(getUsername() + Const.SESSION_allmenuList);
    }
//	/**获取当前登录的用户的所属区域编号
//	 * @return
//	 */
//	public static String getUserAid(){
//		User user =(User)getSession().getAttribute(Const.SESSION_USER);
//		return user.getAid();
//	}
//	/**获取当前登录的用户密码
//	 * @return
//	 */
//	public static String getUserPwd(){
//		User user =(User)getSession().getAttribute(Const.SESSION_USER);
//		return user.getUserinfo().getPassword();
//	}

//	/**获取当前按钮权限(增删改查)
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	public static Map<String, String> getHC(){
//		return (Map<String, String>)getSession().getAttribute(getUsername() + Const.SESSION_QX);
//	}

    /**
     * shiro管理的session
     *
     * @return
     */
    public static Session getSession() {
        //Subject currentUser = SecurityUtils.getSubject();
        if (SecurityUtils.getSubject().getSession() != null) {
            Session session = SecurityUtils.getSubject().getSession();
            int i;
            for (i = sessions.size() - 1; i >= 0; i--) {
                if (sessions.get(i).equals(session)) {
                    break;
                }
				/*if(sessions.get(i).getAttribute(Const.SESSION_Date).equals(session.getAttribute(Const.SESSION_Date))) {
					break;
				}*/
                try {
                    if (sessions.get(i) == null || sessions.get(i).getStartTimestamp() == null) {
                        sessions.get(i).stop();
                        sessions.remove(i);
                        break;
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    e.getStackTrace();
                    try {
                        sessions.get(i).stop();
                    } catch (Exception e2) {
                        // TODO: handle exception
                        e.getStackTrace();
                    }
                    sessions.remove(i);
                }
                if (sessions.get(i).getStartTimestamp().equals(session.getStartTimestamp())) {
                    break;
                }
                if (sessions.get(i).getAttribute(Const.SESSION_USERID).equals(session.getAttribute(Const.SESSION_USERID))
                        && !sessions.get(i).getStartTimestamp().equals(session.getStartTimestamp())/*!sessions.get(i).getAttribute(Const.SESSION_Date).equals(session.getAttribute(Const.SESSION_Date))*/) {
                    logger.debug("用户被挤下线" + sessions.get(i).getAttribute(Const.SESSION_LOGINID));
                    sessions.get(i).stop();
                    sessions.remove(i);
                }
            }
            try {
                if (i < 0 && session.getAttribute(Const.SESSION_USER) != null) {
                    sessions.add(session);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return session;
        } else
            return null;
    }

    public static String getFid() {
        return getSession().getAttribute(Const.SESSION_FID).toString();
    }

    public static String getFname() {
        // TODO Auto-generated method stub
        return getSession().getAttribute(Const.SESSION_FNAME).toString();
    }

    public static List<Session> getSessions() {
        return sessions;
    }
}
