package com.audioweb.quartzjob;

import com.audioweb.service.LogManager;
import com.audioweb.service.impl.LogService;
import com.audioweb.util.Const;
import com.audioweb.util.SpringContextUtils;
import com.niocast.cast.MulticastThread;
import com.niocast.util.GlobalInfoController;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class TerminalsRebootJob extends QuartzJobBean {
    private static LogManager logService = (LogService) SpringContextUtils
            .getBeanByClass(LogService.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        try {
            GlobalInfoController.AllTerReboot();
            logService.saveLog(Const.LOGTYPE[2], jobDetail.getKey().getGroup(), "全部终端重启", MulticastThread.getLocalHostLANAddress().getHostAddress(), jobDetail.getKey().getName());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
