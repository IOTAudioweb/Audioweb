package com.audioweb.util;


import org.slf4j.Logger;

public class LoggerUtil {
    /**
     * @param logger
     * @param interfaceName
     */
    public static void logBefore(Logger logger, String interfaceName) {
        logger.info("start:" + interfaceName);
    }

    /**
     * @param logger TODO
     *               2018年12月28日
     */
    public static void logAfter(Logger logger) {
        logger.info("end");
    }

    /**
     * @param logger TODO
     *               2018年12月28日
     */
    public static void logError(Logger logger, Exception e) {
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
        logBefore(logger, "执行报错，日志如下:");
        logger.error("cause - 文件:" + fileName + "-类:" + className + "-方法:" + methodName + "-行:" + lineNumber, e);
        logAfter(logger);
    }

    /**
     * @param logger TODO
     *               2018年12月28日
     */
    public static void logError(Logger logger, String s, Exception e) {
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
        logBefore(logger, "执行报错，日志如下:");
        logger.error(s, "cause - 文件:" + fileName + "-类:" + className + "-方法:" + methodName + "-行:" + lineNumber, e);
        logAfter(logger);
    }
}
