package com.audioweb.quartzjob;

import com.audioweb.service.impl.TerminalsService;
import com.audioweb.util.PageData;
import com.audioweb.util.SpringContextUtils;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.Map;

public class RefreshJob extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Map<String, TerminalInfo> terminalInfos = GlobalInfoController.getTerinfoMap();
        TerminalsService terminalsService = (TerminalsService) SpringContextUtils
                .getBeanByClass(TerminalsService.class);
        try {
            for (Map.Entry<String, TerminalInfo> tInfo : terminalInfos.entrySet()) {
                TerminalInfo info = tInfo.getValue();
                if (!info.getIscast() && (info.getIsOnline() || info.getIstrueOnline())) {
                    if (info.getOrderCastInfo().size() > 0) {
                        GlobalInfoController.StartCast(info, info.getOrderCastInfo().get(0));
                    } else {
                        synchronized (info) {
                            info.setIstrueOnline(info.getIsOnline());
                            info.setIsOnline(false);
                            if (!info.getIsOnline() && !info.getIstrueOnline()) {
                                PageData pd = new PageData();
                                pd.put("TIDString", tInfo.getKey());
                                pd.put("FinalOfflineDate", new Date());
                                terminalsService.editDate(pd);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
