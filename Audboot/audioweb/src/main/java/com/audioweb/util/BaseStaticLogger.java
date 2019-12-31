package com.audioweb.util;

import com.audioweb.service.LogManager;
import com.audioweb.service.impl.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseStaticLogger {
    protected static Logger logger = LoggerFactory.getLogger(BaseStaticLogger.class);
    protected static LogManager logservice = (LogService) SpringContextUtils
            .getBeanByClass(LogService.class);

    /**
     * @param logger
     * @param interfaceName
     */
    public static void logBefore(String interfaceName) {
        logger.info("start:" + interfaceName);
    }

    /**
     * @param logger TODO
     *               2018年12月28日
     */
    public static void logAfter() {
        logger.info("end");
    }

    /**
     * 保存日志到数据库
     *
     * @param logtype    日志类型：用户或者后台
     * @param function   日志任务类型
     * @param logcontent 日志信息
     * @param ip         操作者IP
     * @param remark     备注
     */
    public static void saveLog(String logtype, String function, String logcontent, String ip, String remark) {
        logBefore("存储日志");
        try {
            logservice.saveLog(logtype, function, logcontent, ip, remark);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logError(e);
        }
        logAfter();
    }

    /**
     * @param logger TODO
     *               2018年12月28日
     */
    public static void logError(Exception e) {
        String methodName = "";
        String className = "";
        String fileName = "";
        int lineNumber = 1;
        StackTraceElement[] stacks = e.getStackTrace();
        for (int i = 1; i < stacks.length; i++) {
            lineNumber = stacks[i].getLineNumber();
            if (lineNumber > 1) {
                fileName = stacks[i].getFileName();
                className = stacks[i].getClassName();
                methodName = stacks[i].getMethodName();
                break;
            }
        }
        logBefore("执行报错，日志如下:");
        logger.error("cause - 文件:" + fileName + "-类:" + className + "-方法:" + methodName + "-行:" + lineNumber, e);
        logAfter();
    }

    /**
     * @param logger TODO
     *               2018年12月28日
     */
    public static void logError(String s, Exception e) {
        String methodName = "";
        String className = "";
        String fileName = "";
        int lineNumber = 1;
        StackTraceElement[] stacks = e.getStackTrace();
        for (int i = 1; i < stacks.length; i++) {
            lineNumber = stacks[i].getLineNumber();
            if (lineNumber > 1) {
                fileName = stacks[i].getFileName();
                className = stacks[i].getClassName();
                methodName = stacks[i].getMethodName();
                break;
            }
        }
        logBefore("执行报错，日志如下:");
        logger.error(s, "cause - 文件:" + fileName + "-类:" + className + "-方法:" + methodName + "-行:" + lineNumber, e);
        logAfter();
    }
}
