package com.audioweb.controller;

import com.audioweb.entity.Domains;
import com.audioweb.entity.Page;
import com.audioweb.entity.Terminals;
import com.audioweb.service.DomainsManager;
import com.audioweb.service.TerminalsManager;
import com.audioweb.util.AppUtil;
import com.audioweb.util.Const;
import com.audioweb.util.Jurisdiction;
import com.audioweb.util.PageData;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Controller
@RequestMapping("/terminal")
public class TerminalController extends BaseController {
    private static final String FUNCTION = "终端管理";
    @Resource(name = "TerminalsService")
    private TerminalsManager TerminalsService;
    @Resource(name = "DomainsService")
    private DomainsManager DomainsService;

    /**
     * 显示区域列表
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/listAllTerm")
    public ModelAndView listAllTerm(Model model, String DomainId) throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("termstatus/listAllTerm.do")) {
            return logout();
        } // 权限校验
        try {
            JSONArray arrDomains = JSONArray.fromObject(DomainsService.listAllDomains(""));
            String json = arrDomains.toString();
            json = json.replaceAll("parentDomainId", "pId").replaceAll("domainId", "id").replaceAll("domainName", "name");
            model.addAttribute("zTreeNodes", json);
            mv.addObject("DomainId", DomainId);
            mv.setViewName("terminal/ter_ztree");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

//	@RequestMapping("/list")
//	public ModelAndView list(Page page) throws Exception{
//		ModelAndView mv = this.getModelAndView();
//		PageData pd = new PageData();
//		pd = this.getPageData();
//		String lastLoginStart = pd.getString("lastLoginStart");	//开始时间
//		String lastLoginEnd = pd.getString("lastLoginEnd");		//结束时间
//		if(lastLoginStart != null && !"".equals(lastLoginStart)){
//			pd.put("lastLoginStart", lastLoginStart+" 00:00:00");
//		}
//		if(lastLoginEnd != null && !"".equals(lastLoginEnd)){
//			pd.put("lastLoginEnd", lastLoginEnd+" 23:59:59");
//		} 
//		String userid = Jurisdiction.getUserid();
//		pd.put("userid", userid);
//		page.setPd(pd);
//		List<PageData> loglist= logService.getLogByUserId(page);
//		mv.setViewName("log/log_list");
//		mv.addObject("loglist",loglist);
//		return mv;
//	}

    /**
     * 显示终端列表
     *
     * @param model
     * @return
     */
    @RequestMapping("/list")
    public ModelAndView list(Page page) throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("terminal/listAllTerm.do")) {
            return logout();
        } // 权限校验
        PageData pd = new PageData();
        pd = this.getPageData();
        Domains term = new Domains();
        try {
            List<Terminals> termList = new ArrayList<Terminals>();
            String id = (null == pd.get("DomainId") || "".equals(pd.get("DomainId").toString())) ? "" : pd.get("DomainId").toString();
            term = DomainsService.getDomainsByDomainId(id);
            page.setPd(pd);
            termList = TerminalsService.listAllTerByDomainIdPage(page);
            pd.put("term", term);
            mv.addObject("pd", pd);    //传入父区域所有信息
            mv.addObject("termList", termList);
            mv.setViewName("terminal/ter_list");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 请求新增终端页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("terminal/listAllTerm.do")) {
            return logout();
        } // 权限校验
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            int id = Integer.parseInt(TerminalsService.findMaxTIDString(pd) != null ? TerminalsService.findMaxTIDString(pd) : "0") + 1;
            String Tid = String.valueOf(id);
            while (Tid.length() < 4) {
                Tid = "0" + Tid;
            }
            mv.addObject("TIDString", Tid);
            mv.addObject("DomainId", pd.getString("DomainId"));                    //传入区域ID，作为终端的父区域ID用
            mv.addObject("DomainName", pd.getString("DomainName"));                    //传入区域ID，作为终端的父区域ID用
            mv.addObject("MSG", "add");                            //执行状态 add 为添加
            mv.setViewName("terminal/ter_edit");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 请求批量新增终端页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toAddList")
    public ModelAndView toAddList() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("terminal/listAllTerm.do")) {
            return logout();
        } // 权限校验
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            String id = String.valueOf(Integer.parseInt(TerminalsService.findMaxTIDString(pd) != null ? TerminalsService.findMaxTIDString(pd) : "0") + 1);
            while (id.length() < 4) {
                id = "0" + id;
            }
            mv.addObject("TIDString", id);
            mv.addObject("DomainId", pd.getString("DomainId"));                    //传入区域ID，作为终端的父区域ID用
            mv.addObject("DomainName", pd.getString("DomainName"));                    //传入区域ID，作为终端的父区域ID用
            mv.addObject("MSG", "addlist");                            //执行状态 add 为添加
            mv.setViewName("terminal/ter_edit");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 添加终端信息
     *
     * @param term
     * @param model
     * @return
     */
    @RequestMapping(value = "/add")
    public ModelAndView add(Terminals term) throws Exception {
        logBefore(Jurisdiction.getUsername() + "添加终端");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pd.put("DomainId", term.getDomainId());
        if ((null != pd.get("ISAutoCast") && !"".equals(pd.get("ISAutoCast").toString()))) {
            pd.put("ISAutoCast", term.getISAutoCast());
        }
        if ((null != pd.get("ISCMIC") && !"".equals(pd.get("ISCMIC").toString()))) {
            pd.put("ISCMIC", term.getISCMIC());
        }
        try {
            String tid = term.getTIDString();
            while (tid.length() < 4) {
                tid = "0" + tid;
            }
            int num = Integer.parseInt(tid);
            if (num < 10000 && num >= 0) {
                pd.put("TIDString", tid);
                term.setTIDString(tid);
                term.setTIP(InetAddress.getByName(term.getTIP()).getHostAddress());
                pd.put("TIP", term.getTIP());
                PageData rePageData = TerminalsService.findUsingId(pd);
                if (rePageData != null) {
                    String TIDString = (null == rePageData.get("TIDString") || "".equals(rePageData.get("TIDString").toString())) ? "" : rePageData.get("TIDString").toString();
                    String isuse1 = (null == rePageData.get("isuse1") || "".equals(rePageData.get("isuse1").toString())) ? "" : rePageData.get("isuse1").toString();
                    String isuse2 = (null == rePageData.get("isuse2") || "".equals(rePageData.get("isuse2").toString())) ? "" : rePageData.get("isuse2").toString();
                    if (TIDString.equals("")) {
                        TerminalsService.saveTer(term); //保存终端
                    } else if (!TIDString.equals("") && !isuse1.equals("") && !isuse2.equals("")) {
                        if (!isuse1.equals("true") || !isuse2.equals("true")) {
                            TerminalsService.insertTerByTid(pd);
                        }
                    }
                } else {
                    TerminalsService.saveTer(term); //保存终端
                }
                //刷新内存信息
                GlobalInfoController.getTerinfoMap().put(tid, new TerminalInfo(false, term, null));
                //插入日志
                saveLog(Const.LOGTYPE[1], FUNCTION, "新增", this.getRemortIP(), term.getTName());
            }
        } catch (Exception e) {
            logError(e);
            mv.addObject("msg", "failed");
        }
        mv.setViewName("redirect:list.do?DomainId=" + term.getDomainId()); //保存成功跳转到列表页面
        return mv;
    }

    /**
     * 批量添加终端信息
     *
     * @param termlist
     * @param model
     * @return
     */
    @RequestMapping(value = "/addlist")
    public ModelAndView addlist(Terminals term) throws Exception {
        logBefore(Jurisdiction.getUsername() + "批量添加终端");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pd.put("DomainId", term.getDomainId());
        List<Terminals> ter = new ArrayList<Terminals>();
        try {
            term.setTIP(InetAddress.getByName(term.getTIP()).getHostAddress());
            ter = ModifyList(term);
            TerminalsService.saveTerlist(ter); //保存终端
            //刷新内存
            Map<String, TerminalInfo> map = GlobalInfoController.getTerinfoMap();
            for (Terminals t : ter) {
                map.put(t.getTIDString(), new TerminalInfo(false, t, null));
            }
            //插入日志
            saveLog(Const.LOGTYPE[1], FUNCTION, "批量新增", this.getRemortIP(), term.getTIDString() + "-" + ter.get(ter.size() - 1).getTIDString());
        } catch (Exception e) {
            logError(e);
            mv.addObject("msg", "failed");
        }
        mv.setViewName("redirect:list.do?DomainId=" + term.getDomainId()); //保存成功跳转到列表页面
        return mv;
    }

    private List<Terminals> ModifyList(Terminals term) {
        int tNameId = term.getTNameId();
        List<Terminals> list = new ArrayList<Terminals>();
        int[] tip = convertStrToArray(term.getTIP());
        int size = tip.length;
        String Tip = "";
        for (int count = 0; count < Integer.parseInt(term.getNumber()); count++) {
            list.add(Tern(term));
            String tid = String.valueOf(Integer.parseInt(term.getTIDString()) + 1);
            while (tid.length() < 4) {
                tid = 0 + tid;
            }
            term.setTIDString(tid);
            if (tNameId != -1)
                term.setTName(term.getTName().replace(Integer.toString(tNameId), Integer.toString(++tNameId)));
            if (tip[size - 1] < 255) {
                tip[size - 1]++;
            } else if (tip[size - 2] < 255) {
                tip[size - 2]++;
                tip[size - 1] = 2;
            }
            Tip = Integer.toString(tip[0]);
            for (int i = 1; i < size; i++) {
                Tip = Tip + "." + Integer.toString(tip[i]);
            }
            term.setTIP(Tip);

        }
        return list;
    }

    private Terminals Tern(Terminals term) {
        Terminals ter = new Terminals();
        ter.setDomainId(term.getDomainId());
        ter.setISAutoCast(term.getISAutoCast());
        ter.setISCMIC(term.getISCMIC());
        ter.setTIDString(term.getTIDString());
        ter.setTIP(term.getTIP());
        ter.setTName(term.getTName());
        ter.setDomainName(term.getDomainName());
        return ter;
    }

    private int[] convertStrToArray(String str) {
        StringTokenizer st = new StringTokenizer(str, ".");//把"."作为分割标志，然后把分割好的字符赋予StringTokenizer对象。
        int[] strArray = new int[st.countTokens()];//通过StringTokenizer 类的countTokens方法计算在生成异常之前可以调用此 tokenizer 的 nextToken 方法的次数。
        int i = 0;
        while (st.hasMoreTokens()) {//看看此 tokenizer 的字符串中是否还有更多的可用标记。
            strArray[i++] = Integer.parseInt(st.nextToken());//返回此 string tokenizer 的下一个标记。
        }
        return strArray;
    }

    /**
     * 显示区域列表ztree(个人信息修改的区域选择)
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/listSelectDomains")
    @ResponseBody
    public Object listSelectDomains() throws Exception {
        String json = null;
        try {
            JSONArray arr = JSONArray.fromObject(DomainsService.listAllDomains("")); //所有区域列表
            json = arr.toString();
            json = json.replaceAll("\"parentDomainId\"", "pId").replaceAll("\"domainId\"", "id").replaceAll("\"domainName\"", "name");
        } catch (Exception e) {
            logError(e);
        }
        return json;
    }

    /**
     * 删除终端
     *
     * @param TID
     * @param out
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete() throws Exception {
        logBefore(Jurisdiction.getUsername() + "删除终端");
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String errInfo = "";
        try {
            TerminalsService.deleteTerByTid(pd.getString("TIDString"));
            //更新内存信息
            GlobalInfoController.deleteTerminalInfo(pd.getString("TIDString"));
            errInfo = "success";
            //插入日志
            saveLog(Const.LOGTYPE[1], FUNCTION, "删除", this.getRemortIP(), pd.get("TIDString").toString());
        } catch (Exception e) {
            logError(e);
        }
        map.put("result", errInfo);
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 批量删除
     *
     * @throws Exception
     */
    @RequestMapping(value = "/deleteAllO")
    @ResponseBody
    public Object deleteAllO() throws Exception {
        logBefore(Jurisdiction.getUsername() + "批量删除终端");
        PageData pd = new PageData();
        Map<String, Object> map = new HashMap<String, Object>();
        List<PageData> pdList = new ArrayList<PageData>();
        pd = this.getPageData();
        String tids = pd.getString("TIDStrings");
        if (null != tids && !"".equals(tids)) {
            String Arraytids[] = tids.split(",");
            TerminalsService.deleteAllO(Arraytids);
            //更新内存信息
            GlobalInfoController.deleteTerminalInfo(Arraytids);
            pd.put("msg", "ok");
            //插入日志
            saveLog(Const.LOGTYPE[1], FUNCTION, "批量删除", this.getRemortIP(), tids.length() < 240 ? tids : "");
        } else {
            pd.put("msg", "no");
        }
        pdList.add(pd);
        map.put("list", pdList);
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 请求编辑区域页面
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(String TIDString) throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        try {
            pd = this.getPageData();
            pd.put("TIDString", TIDString);                //接收过来的要修改的ID
            Terminals terminals = TerminalsService.getTermByTIDString(TIDString);    //读取此ID的终端数据
            pd.put("term", terminals);
            Domains domains = DomainsService.getDomainsByDomainId(terminals.getDomainId());
            mv.addObject("pd", pd);            //放入视图容器
            mv.addObject("DomainId", terminals.getDomainId());    //传入父区域ID，作为终端父区域ID用
            mv.addObject("DomainName", domains.getDomainName());                    //传入区域名称，作为终端的父区域名称用
            mv.addObject("MSG", "edit");
            mv.addObject("user", Jurisdiction.getUserid());
            mv.setViewName("terminal/ter_edit");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 保存编辑
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/edit")
    public ModelAndView edit(Terminals term) throws Exception {
//		if(!Jurisdiction.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        try {
            if (null == pd.get("ISCMIC") || "".equals(pd.get("ISCMIC").toString()))
                pd.put("ISCMIC", 0);
            else
                pd.put("ISCMIC", 1);
            if (null == pd.get("ISAutoCast") || "".equals(pd.get("ISAutoCast").toString()))
                pd.put("ISAutoCast", 0);
            else
                pd.put("ISAutoCast", 1);
            term.setTIP(InetAddress.getByName(term.getTIP()).getHostAddress());
            pd.put("TIP", term.getTIP());
            TerminalsService.editTer(pd);
            Terminals terminals = TerminalsService.getTermByTid(pd.getString("TIDString"));
            TerminalInfo tInfo = GlobalInfoController.getTerminalInfo(terminals.getTIDString());
            //更新内存信息
            if (tInfo != null) {
					/*if(Jurisdiction.getUserid().equals("1"))
					if(null != pd.get("setTer") && !"".equals(pd.get("setTer").toString())) {//发送更改配置命令
						GlobalInfoController.SendData(InterCMDProcess.sendTerReset(terminals), tInfo.getSession(), 0);
						saveLog(Const.LOGTYPE[1],FUNCTION,"更改终端配置",this.getRemortIP(),pd.getString("tid"));
					}*/
                synchronized (tInfo) {
                    try {
                        tInfo.setDomainId(terminals.getDomainId());
                        tInfo.setIsAutoCast(terminals.getISAutoCast());
                        tInfo.setIsPaging(terminals.getISCMIC());
                        tInfo.setIpAddress(InetAddress.getByName(terminals.getTIP()));
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else {
                GlobalInfoController.getTerinfoMap().put(pd.getString("TIDString"), new TerminalInfo(false, terminals, null));
            }
            //插入日志
            saveLog(Const.LOGTYPE[1], FUNCTION, "编辑", this.getRemortIP(), pd.getString("tid"));
        } catch (Exception e) {
            logError(e);
        }
        mv.setViewName("redirect:list?DomainId=" + term.getDomainId()); //保存成功跳转到列表页面
        return mv;
    }

    /**
     * 检测是否输入正确
     *
     * @throws Exception
     */
    @RequestMapping(value = "/istrue")
    @ResponseBody
    public Object istrue() throws Exception {
        PageData pd = new PageData();
        Map<String, Object> map = new HashMap<String, Object>();
        pd = this.getPageData();
        String id = pd.getString("id");
        String value = pd.getString("value");
        String returndata = "error";
        switch (id) {
            case "TIDString":
                if (value != null && !value.equals("")) {
                    try {
                        int num = Integer.parseInt(value);
                        if (num > 10000 || num < 0) {
                            break;
                        }
                        while (value.length() < 4) value = "0" + value;
                        pd.put("TIDString", value);
                    } catch (Exception e) {
                        // TODO: handle exception
                        break;
                    }
                    PageData rePageData = TerminalsService.findUsingId(pd);
                    if (rePageData != null) {
                        String TIDString = (null == rePageData.get("TIDString") || "".equals(rePageData.get("TIDString").toString())) ? "" : rePageData.get("TIDString").toString();
                        String isuse1 = (null == rePageData.get("isuse1") || "".equals(rePageData.get("isuse1").toString())) ? "" : rePageData.get("isuse1").toString();
                        String isuse2 = (null == rePageData.get("isuse2") || "".equals(rePageData.get("isuse2").toString())) ? "" : rePageData.get("isuse2").toString();
                        if (TIDString.equals("")) {
                            returndata = "success";
                        } else if (!TIDString.equals("") && !isuse1.equals("") && !isuse2.equals("")) {
                            if (!isuse1.equals("true") || !isuse2.equals("true")) {
                                returndata = "success";
                            }
                        }
                    } else {
                        returndata = "success";
                    }
                }
                break;
            case "TName":
                if (value != null && !value.equals("")) {
                    returndata = "success";
                }
                break;
            case "TIP":
                if (value != null && !value.equals("")) {
                    String[] strings = value.split("\\.");
                    if (strings.length == 4) {
                        try {
                            String tip = InetAddress.getByName(value).getHostAddress();
                            Terminals terminals = TerminalsService.findTermByTip(tip);
                            if (terminals != null) {
                                returndata = "false";
                            }
                            if (!returndata.equals("false"))
                                returndata = "success";
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }
                }
                break;
            default:
                break;
        }
        map.put("result", returndata);
        return AppUtil.returnObject(pd, map);
    }

    /**
     * 获取分区列表
     *
     * @return
     */
    @RequestMapping("/finddomids")
    public ModelAndView list() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        JSONArray domarr = JSONArray.fromObject(DomainsService.listAllDomains(""));// 所有分组
        JSONArray lastarr = new JSONArray();
        for (int i = 0; i < domarr.size(); i++) {// 设置jsonobject类型：0表示分组，1表示终端
            JSONObject obj = domarr.getJSONObject(i);
            //obj.element("open", true);
            obj.element("type", "0");
            obj.element("isParent", true);
            //obj.element("chkDisabled", true);
            lastarr.add(obj);
        }
        String lastjson = lastarr.toString().replaceAll("parentDomainId", "pId").replaceAll("domainId", "id")
                .replaceAll("domainName", "name");
        mv.addObject("pd", pd);
        mv.addObject("zTreeNodes", lastjson);
        mv.setViewName("terminal/finddomids");
        return mv;
    }

    /**
     * 保存编辑
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/editdomain")
    public ModelAndView editdomain() throws Exception {
//		if(!Jurisdiction.buttonJurisdiction(menuUrl, "edit")){return null;} //校验权限
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        String domid = (null == pd.get("domids") || "".equals(pd.get("domids").toString())) ? "" : pd.get("domids").toString();//接收传过来的区域ID
        String domainname = (null == pd.get("domainname") || "".equals(pd.get("domainname").toString())) ? "" : pd.get("domainname").toString();//接收传过来的区域名
        String tids = (null == pd.get("tids") || "".equals(pd.get("tids").toString())) ? "" : pd.get("tids").toString();//接收传过来的终端ids
        try {
            List<String> terids = new ArrayList<>();
            String[] tStrings = tids.split(",");
            for (String str : tStrings) {
                terids.add(str);
            }
            pd.put("DomainId", domid);
            pd.put("tids", terids);
            TerminalsService.editTersDomainId(pd);//保存
            GlobalInfoController.updataTerminalInfo(tStrings, domid, domainname);//更新内存
            //插入日志
            saveLog(Const.LOGTYPE[1], FUNCTION, "更换分区", this.getRemortIP(), tids + "->" + domainname);
        } catch (Exception e) {
            logError(e);
        }
        mv.setViewName("redirect:list?DomainId=" + domid); //保存成功跳转到列表页面
        return mv;
    }
}
