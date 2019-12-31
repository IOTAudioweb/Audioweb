package com.audioweb.controller;

import com.audioweb.entity.Domains;
import com.audioweb.entity.MusicFile;
import com.audioweb.entity.Page;
import com.audioweb.service.*;
import com.audioweb.util.*;
import com.niocast.entity.CastTaskInfo;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/filecast")
public class FileCastController extends BaseController {
    private static final String FUNCTION = "文件广播";
    @Resource(name = "logService")
    private LogManager logService;
    @Resource(name = "systemService")
    private SystemManager systemService;
    @Resource(name = "DomainsService")
    private DomainsManager domainsService;
    @Resource(name = "TerminalsService")
    private TerminalsManager terminalsService;
    @Resource(name = "castTaskService")
    private CastTaskManager castTaskService;

    /**
     * 去文件广播
     *
     * @return
     */
    @RequestMapping("/toCast")
    public ModelAndView list() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("filecast/toCast.do")) {
            return logout();
        } // 权限校验
        PageData pd = new PageData();
        pd = this.getPageData();
        List<Domains> alldomain = domainsService.listAllDomains("");
        //添加根目录
        Domains domains = new Domains();
        domains.setDomainId(0);
        domains.setParentDomainId(-1);
        domains.setDomainName("根分区");
        alldomain.add(domains);
        JSONArray domarr = JSONArray.fromObject(alldomain);//所有分组
        JSONArray lastarr = new JSONArray();
        for (int i = 0; i < domarr.size(); i++) {//设置jsonobject类型：0表示分组，1表示终端
            JSONObject obj = domarr.getJSONObject(i);
            obj.element("type", "0");
            obj.element("isParent", true);
            if (obj.getString("domainId").equals("0")) {
                obj.element("open", true);
            }
            lastarr.add(obj);
        }
        List<PageData> termList = terminalsService.listAllTer("");//所有终端
        JSONArray terarr = JSONArray.fromObject(termList);//所有终端列表
        String terjson = terarr.toString().replaceAll("DomainId", "pId").replaceAll("TIDString", "tid").replaceAll("TName", "name");
        terarr = JSONArray.fromObject(terjson);
        for (int i = 0; i < terarr.size(); i++) {
            JSONObject obj = terarr.getJSONObject(i);
            obj.element("type", "1");
            lastarr.add(obj);
        }
        String lastjson = lastarr.toString().replaceAll("parentDomainId", "pId").replaceAll("domainId", "id").replaceAll("domainName", "name");

        ArrayList<String> filelist = FileUtil.getFiles(systemService.getBaseAttri("文件广播目录"), null, 0);
        mv.addObject("filelist", filelist);
        mv.addObject("pd", pd);
        mv.addObject("zTreeNodes", lastjson);
        mv.setViewName("filecast/filecast");
        return mv;
    }

    /**
     * 开始广播
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/startCast")
    @ResponseBody
    public Object startCast() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        try {
            //初始化广播基础信息
            List<String> types = new ArrayList<String>();
            types.add(Const.CASTTYPE[0]);
            //获取广播ID
            int taskid = GlobalInfoController.getTaskId();
            types.add("文件广播" + taskid);
            //获取终端、分区节点
            List<String> domainidlist = castTaskService.InfoAnalyze(pd.getString("domids"));
            List<String> terlist = castTaskService.InfoAnalyze(pd.getString("terids"));
            List<TerminalInfo> terminalInfos = castTaskService.TermAnalyze(terlist, domainidlist, null);
            //获取文件信息、优先级、音量
            List<String> filelist = castTaskService.FileAnalyze(Const.CASTTYPE[0], pd.getString("filename"));
            int castlevel = Integer.parseInt(pd.get("castlevel").toString());
            int vol = Integer.parseInt(pd.get("vol").toString());
            //获取组播端口以及IP
            String multicastaddress = GlobalInfoController.getMulticastAddress();
            int multicastport = castTaskService.PortAnalyze();
            //创建广播对象
            CastTaskInfo castTaskInfo = new CastTaskInfo(taskid, types, castlevel, domainidlist, terminalInfos, vol, multicastaddress, multicastport, 0, -1, filelist);
            //开始广播
            boolean is = castTaskService.startCommCastTask(castTaskInfo);
            if (is) {
                logService.saveLog(Const.LOGTYPE[1], FUNCTION, "开始文件广播", this.getRemortIP(), taskid + "");
                map.put("taskid", taskid + "");
                map.put("result", "success");
            } else {
                map.put("result", "error");
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            map.put("result", "error");
        }
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 结束广播,添加日志
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/stopCast")
    @ResponseBody
    public Object stopCast() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String taskid = pd.get("taskid").toString();
        if (taskid != null && !"".equals(taskid)) {
            GlobalInfoController.stopCastTaskInList(Integer.parseInt(taskid));
            logService.saveLog(Const.LOGTYPE[1], FUNCTION, "结束广播", this.getRemortIP(), taskid);
            map.put("result", "success");
        } else {
            map.put("result", "error");
        }
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 去文件管理界面
     *
     * @return
     */
    @RequestMapping("/toFileManage")
    public ModelAndView toFileManage() throws Exception {
        ModelAndView mv = this.getModelAndView();
        if (!Jurisdiction.hasJurisdiction("filecast/toFileManage.do")) {
            return logout();
        } // 权限校验
        PageData pd = new PageData();
        pd = this.getPageData();
        ArrayList<String> filelist = FileUtil.getFiles(systemService.getBaseAttri("文件广播目录"), null, 0);
        mv.addObject("filelist", filelist);
        mv.addObject("pd", pd);
        mv.setViewName("filecast/file_upload");
        return mv;
    }

    /**
     * 删除文件
     *
     * @param TID
     * @param out
     */
    @RequestMapping(value = "/deletefile")
    @ResponseBody
    public Object deletefile() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String errInfo = "error";
        try {
            String filename = pd.get("filename").toString();
            FileUtil.delFile(systemService.getBaseAttri("文件广播目录").replace("\\", "\\\\") + "/" + filename);
            errInfo = "success";
            //插入日志
            logService.saveLog(Const.LOGTYPE[1], "文件列表管理", "删除", this.getRemortIP(), pd.get("filename").toString());
        } catch (Exception e) {
            // TODO: handle exception
        }
        map.put("result", errInfo);
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 批量删除音频文件
     *
     * @param TID
     * @param out
     */
    @RequestMapping(value = "/deleteAll")
    @ResponseBody
    public Object deleteAll() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String errInfo = "error";
        try {
            String resource = systemService.getBaseAttri("文件广播目录").replace("\\", "\\\\");
            String filename = pd.get("filenames").toString();
            String[] filenames = filename.split("//");
            String failnames = "";
            for (String path : filenames) {
                try {
                    if (!FileUtil.delFile(resource + "/" + path)) {
                        failnames += path + ",";
                    }
                } catch (Exception e) {
                    e.getStackTrace();
                    // TODO: handle exception
                    failnames += path + ",";
                }
            }
            errInfo = "success";
            //插入日志
            logService.saveLog(Const.LOGTYPE[1], "文件列表管理", "批量删除", this.getRemortIP(), pd.get("filename").toString());
            if (!"".equals(failnames)) {
                map.put("failnames", failnames.substring(0, failnames.length() - 1));
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.getStackTrace();
        }
        map.put("result", errInfo);
        return AppUtil.returnObject(new PageData(), map);
    }
    /**
     * 上传音频文件
     * @param
     * @return
     * @throws Exception
     */
	/*@RequestMapping(value="/addfile")
	public ModelAndView addPer(@RequestParam("file") MultipartFile[] files) throws Exception{
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		//		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String filename="";
		String failfile="";//添加失败的节目
		if(files!=null && files.length>0){
			for(int i=0;i<files.length;i++){
				if (null != files[i] && !files[i].isEmpty()) {
			        CommonsMultipartFile cf= (CommonsMultipartFile)files[i]; 
			        DiskFileItem fi = (DiskFileItem)cf.getFileItem(); 
			        File f = fi.getStoreLocation();
					if(!MP3AudioUtil.isMP3File(f)) {
						
					}
					String failcur="";
					String filePath = systemService.getBaseAttri("文件广播目录")+"\\";			//文件上传路径
					String fname =files[i].getOriginalFilename();
					if(FileUpload.iscreated(filePath, fname)) {
						filename = fname.substring(0, fname.lastIndexOf("."));
					}else {
						filename = fname.substring(0, fname.lastIndexOf("."))+"_"+DateUtil.getSdfTimes();
					}
					String saveName =  FileUpload.fileUp(files[i], filePath,filename);	//执行上传
					String biteRate = MP3AudioUtil.getAudioBitRate(filePath+saveName);
					if(biteRate != null && Integer.parseInt(biteRate)%16 != 0){
						failcur = fname;
						FileUtil.delFilePath(filePath+saveName);
					}else if(biteRate != null){
						logService.saveLog(Const.LOGTYPE[1], FUNCTION, "添加", this.getRemortIP(), saveName);
					}else {
						failcur = fname;
						FileUtil.delFilePath(filePath+saveName);
					}
					if(!failcur.equals(""))
						failfile += failcur+",";
				}
			}
		}
		mv.addObject("pd", pd);
		String msg = "";
		if(failfile.equals("")){
			msg="?msg=success";
		}else{
			msg = "?msg=bitrateerror&files="+failfile;
//			msg=failfile+"添加失败!";
//			mv.addObject("files", failfile);
		}
//		mv.addObject("msg", msg);
		mv.setViewName("redirect:toFileManage.do"+msg); //保存成功跳转到列表页面
		return mv;
//	}
	}*/

    /**
     * 查看广播任务列表
     *
     * @return
     */
    @RequestMapping("/toplay")
    @ResponseBody
    public Object toplay() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        PageData pd = new PageData();
        pd = this.getPageData();
        String taskid = (null == pd.get("taskid") || "".equals(pd.get("taskid").toString())) ? "" : pd.get("taskid").toString();
        String vol = (null == pd.get("vol") || "".equals(pd.get("vol").toString())) ? "" : pd.get("vol").toString();
        String commd = (null == pd.get("commd") || "".equals(pd.get("commd").toString())) ? "" : pd.get("commd").toString();
        if (taskid != null && !"".equals(taskid)) {
            CastTaskInfo cInfo = GlobalInfoController.getCastTaskInfo(Integer.parseInt(taskid));
            if (cInfo != null) {
                if (!vol.equals("")) {
                    cInfo.setVol(Integer.parseInt(vol));//音量控制
                    logService.saveLog(Const.LOGTYPE[1], FUNCTION, "修改文件广播音量", this.getRemortIP(), "ID:" + taskid + "	vol:" + vol);
                } else if (!commd.equals("")) {
                    if (commd.equals("true")) {
                        cInfo.setIsStop(true);//暂停播放
                    } else if (commd.equals("false")) {
                        cInfo.setIsStop(false);//继续播放
                    }
                }
                map.put("result", "success");
                return AppUtil.returnObject(new PageData(), map);
            }
        }
        map.put("result", "error");
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 上传音频文件
     *
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/addfiles")
    @ResponseBody
    public Object addfiles(@RequestParam("file") MultipartFile file) throws Exception {
        PageData pd = new PageData();
        pd = this.getPageData();
        Map<String, String> map = new HashMap<String, String>();
        //		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String filename = "";
        if (file != null && file.getSize() > 0) {
            if (null != file && !file.isEmpty()) {
                try {
                    CommonsMultipartFile cf = (CommonsMultipartFile) file;
                    DiskFileItem fi = (DiskFileItem) cf.getFileItem();
                    File f = fi.getStoreLocation();
                    if (!MP3AudioUtil.isMP3File(f)) {
                        saveLog(Const.LOGTYPE[1], FUNCTION, "添加文件类型不符", this.getRemortIP(), file.getName());
                        map.put("result", "success");
                        map.put("code", "-1");
                        return AppUtil.returnObject(new PageData(), map);
                    }
                    String failcur = "";
                    String filePath = systemService.getBaseAttri("文件广播目录") + "\\";            //文件上传路径
                    String fname = file.getOriginalFilename();
                    if (FileUpload.iscreated(filePath, fname)) {
                        filename = fname.substring(0, fname.lastIndexOf("."));
                    } else {
                        filename = fname.substring(0, fname.lastIndexOf(".")) + "_" + DateUtil.getSdfTimes();
                    }
                    String saveName = FileUpload.fileUp(file, filePath, filename);    //执行上传
                    String biteRate = MP3AudioUtil.getAudioBitRate(filePath + saveName);
                    if (biteRate != null) {
                        if (Integer.parseInt(biteRate) % 16 != 0 || Integer.parseInt(biteRate) < 96) {
                            failcur = fname;
                            FileUtil.delFilePath(filePath + saveName);
                        } else {
                            saveLog(Const.LOGTYPE[1], FUNCTION, "添加", this.getRemortIP(), saveName);
                        }
                    } else {
                        failcur = fname;
                        FileUtil.delFilePath(filePath + saveName);
                    }
                    if (!failcur.equals("")) {
                        saveLog(Const.LOGTYPE[1], FUNCTION, "添加文件格式不符", this.getRemortIP(), saveName);
                        map.put("result", "success");
                        map.put("code", "1");
                        return AppUtil.returnObject(new PageData(), map);
                    }
                } catch (Exception e) {
                    logError(e);
                    saveLog(Const.LOGTYPE[1], FUNCTION, "添加文件出错", this.getRemortIP(), file.getName());
                    // TODO: handle exception
                    map.put("result", "success");
                    map.put("code", "2");
                    return AppUtil.returnObject(new PageData(), map);
                }
            }
        }
        map.put("result", "success");
        map.put("code", "0");
        return AppUtil.returnObject(new PageData(), map);
    }

    /**
     * 查看音频文件
     *
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listfiles")
    @ResponseBody
    public Object listfiles() throws Exception {
        PageData pd = new PageData();
        pd = this.getPageData();
        Map<String, Object> map = new HashMap<String, Object>();
        Page page = new Page();
        try {
            page.setShowCount(Integer.parseInt(pd.getString("limit")));
            page.setCurrentPage(Integer.parseInt(pd.getString("page")));
            List<MusicFile> files = MP3AudioUtil.getMp3Files(systemService.getBaseAttri("文件广播目录"), page);
            JSONArray array = JSONArray.fromObject(files);
            map.put("data", array);
        } catch (Exception e) {
            logError(e);
        }
        map.put("code", 0);
        map.put("msg", "");
        map.put("count", page.getTotalResult());
        return AppUtil.returnObject(new PageData(), map);
    }
}
