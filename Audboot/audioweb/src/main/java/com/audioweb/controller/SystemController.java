package com.audioweb.controller;

import com.audioweb.entity.Users;
import com.audioweb.service.QuartzManager;
import com.audioweb.service.SystemManager;
import com.audioweb.service.UsersManager;
import com.audioweb.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 类名称：SystemController
 */
@Controller
@RequestMapping(value = "/system")
public class SystemController extends BaseController {

    @Resource(name = "systemService")
    private SystemManager systemService;
    @Resource(name = "usersService")
    private UsersManager usersService;
    @Resource(name = "quartzService")
    private QuartzManager quartzService;

    /**
     * 去基础设置界面
     *
     * @return ModelAndView
     */
    @RequestMapping(value = "/toBaseset")
    public ModelAndView toBaseset() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("system/toBaseset.do")) {
            return logout();
        } // 权限校验
        PageData pd = new PageData();
        pd = this.getPageData();
        String userRid = Jurisdiction.getUserid();
        pd.put("userid", userRid);
        List<PageData> data = systemService.listBaseAttributes("");
        for (PageData item : data) {
            String valueid = item.get("valueid").toString();
            switch (valueid) {
                case "系统名称":
                    pd.put("systemname", item.get("valuename").toString());
                    break;
                case "系统所有者":
                    pd.put("owner", item.get("valuename").toString());
                    break;
                case "文件广播目录":
                    pd.put("filecastpath", item.get("valuename").toString());
                    break;
                case "终端点播目录":
                    pd.put("tercastpath", item.get("valuename").toString());
                    break;
                case "终端点播优先级":
                    pd.put("terunicast", item.get("valuename").toString());
                    break;
                case "终端采播优先级":
                    pd.put("tergathercast", item.get("valuename").toString());
                    break;
                case "寻呼话筒优先级":
                    pd.put("pagphone", item.get("valuename").toString());
                    break;
/*			case "serverIp":
				pd.put("serverIp", item.get("valuename").toString());
				break;
			case "gateway":
				pd.put("gateway", item.get("valuename").toString());
				break;
			case "netmask":
				pd.put("netmask", item.get("valuename").toString());
				break;*/
                default:
                    break;
            }
        }
        Users self = usersService.findByUserid(pd);
        if (null != self) {
            pd.put("realcasttype", self.getRealcasttype());
        }
        try {
            if (quartzService.getRefreshTask()) {
                pd.put("jobStatus", "NORMAL");
            } else {
                pd.put("jobStatus", "NONE");
            }
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
        }
        try {
            String statu = quartzService.getTerReboot();
            if (statu != null) {
                pd.put("RebootjobStatus", statu);
            }
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
        }
        mv.setViewName("system/base_setting");
        mv.addObject("pd", pd);
        return mv;
    }

    /**
     * 保存设置
     */
    @RequestMapping(value = "/saveEdit")
    @ResponseBody
    public Object saveEdit() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String returnData = "";
        boolean isuser = false;
        /*boolean isNet  =false;*/
        String name = (null == pd.get("name") || "".equals(pd.get("name").toString())) ? "" : pd.get("name").toString();
        String value = (null == pd.get("content") || "".equals(pd.get("content").toString())) ? "" : pd.get("content").toString();
        String istrue = (null == pd.get("istrue") || "".equals(pd.get("istrue").toString())) ? "" : pd.get("istrue").toString();
        if (!"".equals(name) && !"".equals(value))
            switch (name) {
                case "systemname":
                    pd.put("valueid", "系统名称");
                    break;
                case "owner":
                    pd.put("valueid", "系统所有者");
                    break;
                case "filecastpath":
                    if (FileUtil.findDir(value))
                        pd.put("valueid", "文件广播目录");
                    else if (!"".equals(istrue)) {
                        FileUtil.createDir(value);
                        returnData = "creatdir";
                        pd.put("valueid", "文件广播目录");
                    } else {
                        returnData = "isnodir";
                        map.put("result", returnData);
                        return AppUtil.returnObject(pd, map);
                    }
                    break;
                case "tercastpath":
                    if (FileUtil.findDir(value))
                        pd.put("valueid", "终端点播目录");
                    else if (!"".equals(istrue)) {
                        if (FileUtil.createDir(value)) {
                            returnData = "creatdir";
                            pd.put("valueid", "终端点播目录");
                        } else {
                            returnData = "error";
                        }
                    } else {
                        returnData = "isnodir";
                        map.put("result", returnData);
                        return AppUtil.returnObject(pd, map);
                    }
                    break;
                case "terunicast":
                    pd.put("valueid", "终端点播优先级");
                    break;
                case "tergathercast":
                    pd.put("valueid", "终端采播优先级");
                    break;
                case "pagphone":
                    pd.put("valueid", "寻呼话筒优先级");
                    break;
                case "realcasttype":
                    pd.put("realcasttype", value);
                    isuser = true;
                    break;
			/*case "serverIp":
				if(istrue(pd)) {
					pd.put("valueid", "serverIp");
					value = pd.get("content").toString();
					isNet = true;
				}else {
					pd.put("valueid", "");
					returnData = "error";
				}
				break;
			case "gateway":
				if(istrue(pd)) {
					pd.put("valueid", "gateway");
					value = pd.get("content").toString();
					isNet = true;
				}else {
					pd.put("valueid", "");
					returnData = "error";
				}
				break;
			case "netmask":
				if(istrue(pd)) {
					pd.put("valueid", "netmask");
					value = pd.get("content").toString();
					isNet = true;
				}else {
					pd.put("valueid", "");
					returnData = "error";
				}
				break;*/
                default:
                    pd.put("valueid", "");
                    returnData = "error";
                    break;
            }
        else {
            returnData = "error";
        }
        pd.put("valuename", value);
        if (!"error".equals(returnData)) {
            if (isuser) {
                pd.put("userid", Jurisdiction.getUserid());
                usersService.editUser(pd);
            }/*else if(isNet){
				systemService.saveAttri(pd);
				if(name.equals("serverIp")) {
					GlobalInfoController.SERVERIP = value;
				}else if(name.equals("gateway")) {
					GlobalInfoController.GATEWAY = value;
				}else if(name.equals("netmask")) {
					GlobalInfoController.NETMASK = value;
				} 
			}*/ else {
                systemService.saveAttri(pd);
            }
            saveLog(Const.LOGTYPE[1], "系统管理", "基本设置", this.getRemortIP(), isuser ? ("实时广播类型" + pd.getString("realcasttype")) : (pd.getString("valueid") + value));
        } else
            saveLog(Const.LOGTYPE[1], "系统管理", "基本设置", this.getRemortIP(), "修改错误");
        if ("".equals(returnData)) {
            returnData = "success";
        }
        map.put("result", returnData);
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 重置定时器
     */
    @RequestMapping(value = "/reset")
    @ResponseBody
    public Object resetTimer() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String returnData = "";
        try {
            quartzService.resumeRefreshTask();
            returnData = "success";
            saveLog(Const.LOGTYPE[1], "系统管理", "重置终端检测定时器", this.getRemortIP(), "");
        } catch (Exception e) {
            // TODO: handle exception
            logError(e);
            returnData = "error";
        }
        map.put("result", returnData);
        return AppUtil.returnObject(pd, map);
    }
    /**
     * 更新全部在线终端配置
     */
/*	@RequestMapping(value="/updata")
	@ResponseBody
	public Object updata() throws Exception{
		Map<String,String> map = new HashMap<String,String>();
		PageData pd = new PageData();
		pd = this.getPageData();
		String returnData = "error";
		try {
			if(Jurisdiction.getUserid().equals("1"))
				if(GlobalInfoController.TermUpdata()) {
					returnData = "success";
					saveLog(Const.LOGTYPE[1],"系统管理","更新全部在线终端配置",this.getRemortIP(),"");
				}
		} catch (Exception e) {
			// TODO: handle exception
			logError(e);
			returnData = "error";
		}
		map.put("result", returnData);
		return AppUtil.returnObject(pd, map);
	}
	private boolean istrue(PageData pd) {
		try {
			String value = pd.getString("content");
			if(Jurisdiction.getUserid().equals("1")) {
				String[] strings = value.split("\\.");
				if(strings.length == 4) {
						pd.put("content", InetAddress.getByName(value).getHostAddress());
						return true;
					
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}*/

    /**
     * 请求新增任务页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("system/toBaseset.do")) {
            /*String userid = Jurisdiction.getUserid();*/
            return logout();
        } // 权限校验
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            mv.addObject("pd", pd);
            mv.addObject("MSG", "add"); // 执行状态 add 为添加
            mv.setViewName("system/base_reboot");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 请求修改任务页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("system/toBaseset.do")) {
            /*String userid = Jurisdiction.getUserid();*/
            return logout();
        } // 权限校验
        try {
            PageData pd = new PageData();
            pd = quartzService.getTerRebootInfo();
            mv.addObject("pd", pd);
            mv.addObject("exectime", pd.get("exectime"));
            mv.addObject("MSG", "edit"); // 执行状态 edit
            mv.setViewName("system/base_reboot");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 请求修改任务页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toRead")
    public ModelAndView toRead() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("system/toBaseset.do")) {
            /*String userid = Jurisdiction.getUserid();*/
            return logout();
        } // 权限校验
        try {
            PageData pd = new PageData();
            pd = quartzService.getTerRebootInfo();
            mv.addObject("pd", pd);
            mv.addObject("MSG", "read"); // 执行状态 read
            mv.setViewName("system/base_reboot");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 添加任务信息
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add() throws Exception {
        logBefore(Jurisdiction.getUsername() + "添加终端定时重启任务");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String returnData = "error";
        try {
            pdChange(pd);
            if (pd.get("times") != null)
                quartzService.addOrEditTerReboot(pd);
            saveLog(Const.LOGTYPE[1], "系统管理", "添加终端定时重启任务", this.getRemortIP(), "");
            returnData = "success";
        } catch (Exception e) {
            logError(e);
        }
        map.put("result", returnData);
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 修改任务信息
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/edit")
    @ResponseBody
    public Object edit() throws Exception {
        logBefore(Jurisdiction.getUsername() + "修改终端定时重启任务");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String returnData = "error";
        try {
            pdChange(pd);
            if (pd.get("times") != null)
                quartzService.addOrEditTerReboot(pd);
            saveLog(Const.LOGTYPE[1], "系统管理", "修改终端定时重启任务", this.getRemortIP(), "");
            returnData = "success";
        } catch (Exception e) {
            logError(e);
        }
        map.put("result", returnData);
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 格式化添加页面传来的值
     *
     * @param model
     * @return
     */
    private void pdChange(PageData pd) {
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String weeks;
        if (pd.getString("0").equals("1")) {
            weeks = "1";
        } else {
            weeks = "0";
        }
        for (int i = 1; i < 8; i++) {
            if (pd.getString(String.valueOf(i)) != null) {
                weeks = weeks + 1;
            } else {
                weeks = weeks + 0;
            }
        }
        List<String> times = (ArrayList) PageData.getLikeByPangData(pd, "date_");
        if (times.size() > 0) {
            pd.put("times", times.size());
            for (int i = 0; i < times.size(); i++) {
                pd.put("ExecDate_" + i, times.get(times.size() - 1 - i));
            }
        }
        pd.put("Weeks", weeks);
        pd.put("upDateTime", df.format(day));
    }

    /**
     * 删除任务信息
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete() throws Exception {
        logBefore(Jurisdiction.getUsername() + "删除终端定时重启任务");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String returnData = "error";
        try {
            if (quartzService.deleteReboot()) {
                returnData = "success";
                saveLog(Const.LOGTYPE[1], "系统管理", "删除终端定时重启任务", this.getRemortIP(), "");
            }
        } catch (Exception e) {
            logError(e);
        }
        map.put("result", returnData);
        return AppUtil.returnObject(pd, map);
    }
}
