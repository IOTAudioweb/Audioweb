package com.audioweb.controller;

import com.audioweb.entity.Domains;
import com.audioweb.entity.Terminals;
import com.audioweb.service.DomainsManager;
import com.audioweb.service.TerminalsManager;
import com.audioweb.util.AppUtil;
import com.audioweb.util.Const;
import com.audioweb.util.Jurisdiction;
import com.audioweb.util.PageData;
import com.niocast.util.GlobalInfoController;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/domain")
public class DomainController extends BaseController {
    private static final String FUNCTION = "分区管理";
    @Resource(name = "TerminalsService")
    private TerminalsManager TerminalsService;
    @Resource(name = "DomainsService")
    private DomainsManager DomainsService;

    /**
     * 显示区域列表ztree(分区信息修改的区域选择)
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/listSelectdomain")
    @ResponseBody
    public Object listSelectArea() throws Exception {
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
     * 显示区域列表ztree(分区管理)
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/listAllDomains")
    public ModelAndView listAllArea(Model model) throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("domain/listAllDomains.do")) {
            return logout();
        } // 权限校验
        PageData pd = new PageData();
        pd = this.getPageData();
        String domainId = pd.getString("DomainId");
        try {
            JSONArray arr = JSONArray.fromObject(DomainsService.listAllDomains(""));
            String json = arr.toString();
            json = json.replaceAll("parentDomainId", "pId").replaceAll("domainId", "id").replaceAll("domainName", "name");
            model.addAttribute("zTreeNodes", json);
            mv.addObject("DomainId", domainId);
            mv.setViewName("domain/dom_ztree");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 显示区域列表
     *
     * @param model
     * @return
     */
    @RequestMapping("/list")
    public ModelAndView list() throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        try {
            String did = (null == pd.get("DomainId") || "".equals(pd.get("DomainId").toString())) ? "0" : pd.get("DomainId").toString();
            List<Domains> DomainList = new ArrayList<Domains>();
            DomainList = DomainsService.listSubDomainsByParentDomainId(did);
            Domains domains = DomainsService.getDomainsByDomainId(did);
            pd.put("Domains", domains);
            mv.addObject("pd", pd);    //传入父区域所有信息
            mv.addObject("did", did);
            mv.addObject("MSG", null == pd.get("MSG") ? did : pd.get("MSG").toString()); //MSG=change 则为编辑或删除后跳转过来的
            mv.addObject("DomainList", DomainList);
            mv.setViewName("domain/dom_list");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 请求新增区域页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() throws Exception {
        ModelAndView mv = this.getModelAndView();
        try {
            PageData pd = new PageData();
            pd = this.getPageData();
            Domains pareDomain = new Domains();
            String did = (null == pd.get("DomainId") || "".equals(pd.get("DomainId").toString())) ? "0" : pd.get("DomainId").toString();//接收传过来的上级区域ID,如果上级为顶级就取值“0”
            System.out.println(did);
            pd.put("DomainId", did);
            if (did == "0") {
                pareDomain.setDomainName(null);
            } else {
                pareDomain = DomainsService.getDomainsByDomainId(did);
            }
            System.out.println(pareDomain.toString());
            pd.put("pareDomain", pareDomain);//传入父区域所有信息
            mv.addObject("pd", pd);                //传入父区域所有信息
            mv.addObject("DomainId", did);                    //传入区域ID，作为子区域的父区域ID用
            mv.addObject("MSG", "add");                            //执行状态 add 为添加
            mv.setViewName("domain/dom_edit");
        } catch (Exception e) {
            logError(e);
        }
        return mv;
    }

    /**
     * 添加区域信息
     *
     * @param area
     * @param model
     * @return
     */
    @RequestMapping(value = "/add")
    public ModelAndView add(Domains domains) throws Exception {
        logBefore(Jurisdiction.getUsername() + "添加区域");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        int count;
        //如果父分区id为空
        if (Integer.toString(domains.getParentDomainId()) == null || Integer.toString(domains.getParentDomainId()).equals("")) {
            pd.put("ParentDomainId", "0");
            domains.setParentDomainId(0);//给父分区赋值0
        } else {
            pd.put("ParentDomainId", domains.getParentDomainId());
        }
        try {
            if (domains.getParentDomainId() == 0) {//如果父分区为0，代表此分区为根分区
                PageData dPageData = DomainsService.findCurLevelMaxId(pd);
                String id = (null == dPageData) || (null == dPageData.get("DOMAINID") || "".equals(dPageData.get("DOMAINID").toString())) ? "0" : dPageData.get("DOMAINID").toString();
                count = Integer.parseInt(id) + 1;
            } else {
                //父分区不为0，查询它父分区下所有子分区
                Domains pareDomain = DomainsService.getDomainsByDomainId(Integer.toString(domains.getParentDomainId()));
                List<Domains> DomainList = new ArrayList<Domains>();
                DomainList = DomainsService.listSubDomainsByParentDomainId(Integer.toString(pareDomain.getDomainId()));
                //如果它父分区的子分区没有分区，则以它父分区的id为基础，后面加上01作为id
                if (DomainList.size() == 0) {
                    count = pareDomain.getDomainId() * 100 + 1;
                } else {
                    //如果父分区下有子分区，获得子分区中最大id并加1作为id
                    count = Integer.parseInt(DomainsService.findCurLevelMaxId(pd).get("DOMAINID").toString()) + 1;
                }
            }
            domains.setDomainId(count);
            DomainsService.saveDomains(domains); //保存区域
            //插入日志
            //String logstr ="添加分区："+domains.getDomainName();
            saveLog(Const.LOGTYPE[1], FUNCTION, "添加", this.getRemortIP(), domains.getDomainName());
        } catch (Exception e) {
            logError(e);
            mv.addObject("msg", "failed");
        }
        mv.setViewName("redirect:list.do?MSG='change'&DomainId=" + domains.getDomainId()); //保存成功跳转到列表页面
        return mv;
    }

    /**
     * 删除区域
     *
     * @param domainId
     * @param out
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete() throws Exception {
        logBefore(Jurisdiction.getUsername() + "删除区域");
        Map<String, String> map = new HashMap<String, String>();
        String errInfo = "";
        PageData pd = new PageData();
        pd = this.getPageData();
        String domainId = pd.get("DomainId").toString();
        try {
            if (DomainsService.listSubDomainsByParentDomainId(domainId).size() > 0) {//判断是否有子菜单，是：不允许删除
                errInfo = "false";
            } else {
                String terminalIds = "";
                List<Terminals> terminals = TerminalsService.listTerByDomainId(domainId);
                DomainsService.deleteDomainsByDomainId(domainId);
                for (Terminals ter : terminals) {
                    terminalIds += ter.getTIDString() + ",";
                }
                GlobalInfoController.deleteTerminalInfo(terminalIds.split(","));
                errInfo = "success";
                //插入日志
                saveLog(Const.LOGTYPE[1], FUNCTION, "删除", this.getRemortIP(), pd.get("DomainId").toString());
            }
        } catch (Exception e) {
            logError(e);
        }
        map.put("result", errInfo);
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 请求编辑区域页面
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(String DomainId) throws Exception {
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        try {
            pd = this.getPageData();
            Domains parentDom = new Domains();
            pd.put("DomainId", DomainId);                //接收过来的要修改的ID
            Domains domains = DomainsService.getDomainsByDomainId(DomainId);    //读取此ID的区域数据
            if (domains.getParentDomainId() == 0) {
                parentDom = DomainsService.getDomainsByDomainId(Integer.toString(domains.getDomainId()));
                parentDom.setDomainName(null);
            } else {
                parentDom = DomainsService.getDomainsByDomainId(Integer.toString(domains.getParentDomainId()));
            }
            pd.put("Domains", domains);
            pd.put("pareDomain", parentDom);//传入父区域所有信息
            mv.addObject("pd", pd);            //放入视图容器
            mv.addObject("DomainId", domains.getParentDomainId());    //传入父区域ID，作为子区域的父区域ID用
            mv.addObject("MSG", "edit");
            mv.setViewName("domain/dom_edit");
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
    public ModelAndView edit(Domains domains) throws Exception {
        logBefore(Jurisdiction.getUsername() + "修改区域");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        try {
            String DomainId = (null == pd.get("DomainId") || "".equals(pd.get("DomainId").toString())) ? "" : pd.get("DomainId").toString();//接收传过来的区域ID
            String DomainName = (null == pd.get("DomainName") || "".equals(pd.get("DomainName").toString())) ? "" : pd.get("DomainName").toString();//接收传过来的区域名
            String tids = "";
            DomainsService.editDomains(pd);
            List<PageData> terminals = TerminalsService.listAllTerByDomainId(DomainId);
            for (PageData data : terminals) {
                tids += "," + data.getString("TIDString");
            }
            String[] terids = tids.split(",");
            GlobalInfoController.updataTerminalInfo(terids, DomainId, DomainName);
            saveLog(Const.LOGTYPE[1], FUNCTION, "修改", this.getRemortIP(), DomainId + ":" + DomainName);
        } catch (Exception e) {
            logError(e);
        }
        mv.setViewName("redirect:list?MSG='change'&DomainId=" + domains.getDomainId()); //保存成功跳转到列表页面
        return mv;
    }

}
