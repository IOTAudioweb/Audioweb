package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.Log;
import com.audioweb.entity.Page;
import com.audioweb.service.LogManager;
import com.audioweb.util.Const;
import com.audioweb.util.Jurisdiction;
import com.audioweb.util.PageData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * 系统用户
 */
@Service("logService")
public class LogService implements LogManager {

    @Resource(name = "daoSupport")
    private DaoSupport dao;

    @SuppressWarnings("unchecked")
    @Override
    public List<Log> getLogs(Page page) throws Exception {
        return (List<Log>) dao.findForList("LogMapper.getAlllistPage", page);
    }

    /**
     * 保存日志信息
     */
    @Override
    public void saveLog(String logtype, String function, String logcontent,
                        String ip, String remark) throws Exception {
        PageData logpd = new PageData();
        if (!logtype.equals(Const.LOGTYPE[2])) {
            if (logtype.contains("-")) {
                String[] logtypes = logtype.split("-");
                logpd.put("userid", logtypes[1]);
                logtype = logtypes[0];
            } else
                logpd.put("userid", Jurisdiction.getUserid());
        } else
            logpd.put("userid", "0");
        logpd.put("logtype", logtype);
        logpd.put("function", function);
        logpd.put("logcontent", logcontent);
        logpd.put("ip", ip);
        if (remark != null && remark.length() > 240) {//做长度限制截取
            remark = remark.substring(0, 240);
        }
        logpd.put("remark", remark);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        logpd.put("logtime", df.format(new Date()));
        dao.save("LogMapper.saveLog", logpd);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PageData> getLogsByIds(PageData pd) throws Exception {
        return (List<PageData>) dao.findForList("LogMapper.getLogListByIds", pd);
    }

}
