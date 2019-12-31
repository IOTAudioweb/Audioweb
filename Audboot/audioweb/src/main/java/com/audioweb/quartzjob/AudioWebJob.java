package com.audioweb.quartzjob;

import com.audioweb.entity.ScheTask;
import com.audioweb.entity.TermTask;
import com.audioweb.service.LogManager;
import com.audioweb.service.impl.*;
import com.audioweb.util.Const;
import com.audioweb.util.PageData;
import com.audioweb.util.SpringContextUtils;
import com.audioweb.util.Tools;
import com.niocast.cast.MulticastThread;
import com.niocast.entity.CastTaskInfo;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.List;

//@PersistJobDataAfterExecution
public class AudioWebJob extends QuartzJobBean {
    public static final String ISSKIP = "isskip";
    private QuartzService quartzService = (QuartzService) SpringContextUtils.getBeanByClass(QuartzService.class);
    private CastTaskService castTaskService = (CastTaskService) SpringContextUtils.getBeanByClass(CastTaskService.class);
    private ScheTaskService scheTaskService = (ScheTaskService) SpringContextUtils.getBeanByClass(ScheTaskService.class);
    private TermTaskService termTaskService = (TermTaskService) SpringContextUtils.getBeanByClass(TermTaskService.class);
    private TerminalsService terminalsService = (TerminalsService) SpringContextUtils.getBeanByClass(TerminalsService.class);
    private static LogManager logService = (LogService) SpringContextUtils
            .getBeanByClass(LogService.class);

    private List<String> domainidlist;//分组信息
    private List<TerminalInfo> terminalInfos;//终端信息
    private TerminalInfo info = null;//源终端
    private CastTaskInfo castTaskInfo;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        try {

            PageData pd = new PageData();
            TriggerKey trigger = context.getTrigger().getKey();
            JobDataMap data = context.getJobDetail().getJobDataMap();
            String isskip = data.getString(ISSKIP);
            //System.out.println("isskip:"+isskip);
			/*else {
				data.put(ISSKIP, "true");
			}*/
            if (isskip != null && isskip.equals("true")) {
                data.put(ISSKIP, "false");
                logService.saveLog(Const.LOGTYPE[2], jobDetail.getKey().getGroup(), "跳过任务", MulticastThread.getLocalHostLANAddress().getHostAddress(), jobDetail.getKey().getName());
                return;
            }
            String name = jobDetail.getKey().getGroup() + ":" + jobDetail.getKey().getName();
            String[] strings = jobDetail.getKey().getName().split(":");
            String schetaskid = strings[0];
            pd.put("TaskId", schetaskid);
            List<String> types = new ArrayList<String>();
            int taskid = GlobalInfoController.getTaskId();
            //获取文件信息、优先级、音量
            List<String> filelist;
            //获取组播端口以及IP
            String multicastaddress = GlobalInfoController.getMulticastAddress();
            int multicastport = castTaskService.PortAnalyze();

            if (jobDetail.getKey().getGroup().equals(QuartzService.JobTimeGroup)) {//定时任务
                ScheTask task = scheTaskService.getTaskByTaskId(pd);//获取定时任务信息
                if (task != null) {
                    types.add(Const.CASTTYPE[1]);
                    types.add(name);//定时任务名称
                    filelist = castTaskService.FileAnalyze(Const.CASTTYPE[1], task.getFilesInfo());
                    if (!Format(task)) {
                        logService.saveLog(Const.LOGTYPE[2], jobDetail.getKey().getGroup(), "获取分区以及终端信息出错", MulticastThread.getLocalHostLANAddress().getHostAddress(), jobDetail.getKey().getName());
                        return;
                    }
                    castTaskInfo = new CastTaskInfo(taskid, types, Integer.parseInt(task.getCastlevel()), domainidlist, terminalInfos, Integer.parseInt(task.getVols()), multicastaddress, multicastport, task.getTasktype(), Integer.parseInt(task.getLastingSeconds()), filelist);
                } else {
                    logService.saveLog(Const.LOGTYPE[2], jobDetail.getKey().getGroup(), "获取任务信息出错", MulticastThread.getLocalHostLANAddress().getHostAddress(), jobDetail.getKey().getName());
                    quartzService.deleteTrigger(trigger.getName(), trigger.getGroup());
                    quartzService.deleteJob(jobDetail.getKey().getName(), jobDetail.getKey().getGroup());
                }
            } else {//采播任务
                TermTask task = termTaskService.getTaskByTaskId(pd);
                if (task != null) {
                    types.add(Const.CASTTYPE[3]);
                    types.add(name);//定时任务名称
                    types.add(task.getType());//模式
                    types.add(Tools.GetValueByKey(Const.CONFIG, "netheartPort"));//源终端命令端口
                    if (!Format(task)) {
                        logService.saveLog(Const.LOGTYPE[2], jobDetail.getKey().getGroup(), "获取分区、终端信息源终端权限出错", MulticastThread.getLocalHostLANAddress().getHostAddress(), jobDetail.getKey().getName());
                        return;
                    }
                    castTaskInfo = new CastTaskInfo(taskid, types, task.getCastLevel(), domainidlist, terminalInfos, task.getVols(), multicastaddress, multicastport, Integer.parseInt(task.getLastingSeconds()), info, true);
                } else {
                    logService.saveLog(Const.LOGTYPE[2], jobDetail.getKey().getGroup(), "获取任务信息出错", MulticastThread.getLocalHostLANAddress().getHostAddress(), jobDetail.getKey().getName());
                    quartzService.deleteTrigger(trigger.getName(), trigger.getGroup());
                    quartzService.deleteJob(jobDetail.getKey().getName(), jobDetail.getKey().getGroup());
                }
            }
            GlobalInfoController.putThreadintoStandbyPool(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        if (castTaskInfo != null) {
                            if (Const.CASTTYPE[1].equals(castTaskInfo.getMultiCastType())) {
                                castTaskService.startCommCastTask(castTaskInfo);
                                logService.saveLog(Const.LOGTYPE[2], jobDetail.getKey().getGroup(), "开始广播", MulticastThread.getLocalHostLANAddress().getHostAddress(), types.get(1));
                            } else if (info != null && (info.getIsOnline() || info.getIstrueOnline())) {
                                castTaskService.startPagCastTask(castTaskInfo);
                                logService.saveLog(Const.LOGTYPE[2], jobDetail.getKey().getGroup(), "开始广播", MulticastThread.getLocalHostLANAddress().getHostAddress(), types.get(1));
                            } else
                                logService.saveLog(Const.LOGTYPE[2], jobDetail.getKey().getGroup(), "开始" + types.get(1) + "广播失败", MulticastThread.getLocalHostLANAddress().getHostAddress(), "源终端不在线");
                            String[] names = jobDetail.getKey().getName().split(":");
                            if (names.length > 1) {
                                if (Const.CASTTYPE[1].equals(castTaskInfo.getMultiCastType())) {//定时任务
                                    quartzService.updateTask(names[0], QuartzService.JobTimeGroup, true);
                                } else {//定时采播
                                    quartzService.updateTask(names[0], QuartzService.JobTermGroup, true);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            //Thread.sleep(1000);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            try {
                logService.saveLog(Const.LOGTYPE[2], jobDetail.getKey().getGroup(), "定时任务执行出错", MulticastThread.getLocalHostLANAddress().getHostAddress(), jobDetail.getKey().getName());
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private boolean Format(ScheTask task) {
        PageData pd = new PageData();
        try {
            String[] domidlist;
            String terids = "";
            List<String> domids = new ArrayList<String>();
            String domainsId = task.getDomainsId();
            String[] grouplist = domainsId == null ? null : domainsId.split("//");
            domidlist = grouplist[0] == null ? null : grouplist[0].split(",");
            for (int i = 0; i < domidlist.length; i++) {
                if (!domidlist[i].contains("*")) {
                    domids.add(domidlist[i]);
                }
            }
            String domIds = grouplist[0].replaceAll("[//*]", "");
            pd.put("domidlists", domids);
            if (grouplist.length > 1) {
                terids = grouplist[1];
            }
            List<PageData> terlist = terminalsService.listAllTerByDomainsId(pd);
            for (PageData s : terlist) {
                terids += "," + s.getString("TIDString");
            }
            domainidlist = castTaskService.InfoAnalyze(domIds);
            List<String> terlists = castTaskService.InfoAnalyze(terids);
            terminalInfos = castTaskService.TermAnalyze(terlists, domainidlist, null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean Format(TermTask task) {
        PageData pd = new PageData();
        try {
            String[] domidlist;
            String terids = "";
            List<String> domids = new ArrayList<String>();
            String domainsId = task.getDomainsId();
            String[] grouplist = domainsId == null ? null : domainsId.split("//");
            domidlist = grouplist[0] == null ? null : grouplist[0].split(",");
            for (int i = 0; i < domidlist.length; i++) {
                if (!domidlist[i].contains("*")) {
                    domids.add(domidlist[i]);
                }
            }
            String domIds = grouplist[0].replaceAll("[//*]", "");
            pd.put("domidlists", domids);
            if (grouplist.length > 1) {
                terids = grouplist[1];
            }
            List<PageData> terlist = terminalsService.listAllTerByDomainsId(pd);
            for (PageData s : terlist) {
                terids += "," + s.getString("TIDString");
            }
            domainidlist = castTaskService.InfoAnalyze(domIds);
            List<String> terlists = castTaskService.InfoAnalyze(terids);
            info = GlobalInfoController.getTerminalInfo(task.getTIDString());
            if (info != null && info.getIsAutoCast()) {
                terminalInfos = castTaskService.TermAnalyze(terlists, domainidlist, info);
            } else {
                return false;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
