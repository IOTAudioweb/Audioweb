package com.audioweb.servletlistener;

import com.audioweb.util.BaseStaticLogger;
import com.audioweb.util.Const;
import com.audioweb.util.Tools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 名称 :DelLogs 描述 :定时删除服务器日志
 */
public class DelLogs extends BaseStaticLogger {


    public static void delAdvert() {
        logBefore("开始执行删除日志操作");
        String path = System.getProperty("catalina.home");
        path = path + "\\logs";
        deleteBaseFile(path);
        path = path + "\\AudioWeb";
        deleteSelfFile(path);
    }

    public static void deleteBaseFile(String Path) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        int days = 15;
        try {
            days = Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "saveDate"));
        } catch (Exception e) {
            // TODO: handle exception
            logError("获取日志保存天数失败", e);
        }
        //获取所有文件名
        File file1 = new File(Path);
        File[] files = file1.listFiles();
        //遍历删除文件
        for (int i = 0; i < files.length; i++) {
            File file2 = files[i];
            String logsName = file2.getName();//文件名
            //获取当天以外所有日志
            if (logsName.length() > 15) {
                try {
                    //获取文件创建日期
                    int beginIndex = logsName.indexOf(".");
                    int endIndex = logsName.lastIndexOf(".");
                    if (beginIndex < endIndex) {
                        String logsDate = logsName.substring(beginIndex + 1, endIndex);

                        Date lastLogs = format.parse(logsDate);//日期类型的文件名
                        //获取n天以前日志并删除
                        Calendar lastDate = Calendar.getInstance();
                        lastDate.add(Calendar.DATE, -days);//日期回滚n天
                        String beforeLog1 = format.format(lastDate.getTime());
                        Date beforeDaysLog = format.parse(beforeLog1);
                        //如果文件是在n天前的文件那么删除
                        if (lastLogs.before(beforeDaysLog)) {
                            String delPath = Path + "\\" + logsName;
                            File deleteFile = new File(delPath);
                            deleteFile.delete();
                            logger.debug("已经删除" + days + "天前日志:" + delPath);
                        }
                    }
                } catch (Exception e) {
                    logError("删除日志出错", e);
                }
            }
        }
    }

    public static void deleteSelfFile(String Path) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        int days = 15;
        try {
            days = Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "saveDate"));
        } catch (Exception e) {
            // TODO: handle exception
            logError("获取日志保存天数失败", e);
        }
        //获取所有文件名
        File file1 = new File(Path);
        File[] files = file1.listFiles();
        //遍历删除文件
        for (int i = 0; i < files.length; i++) {
            File file2 = files[i];
            String logsName = file2.getName();//文件名
            //获取当天以外所有日志
            if (logsName.length() > 15) {
                try {
                    //获取文件创建日期
                    int beginIndex = logsName.indexOf("_");
                    int endIndex = logsName.lastIndexOf(".");
                    if (beginIndex < endIndex) {
                        boolean is = true;
                        String name = logsName.substring(0, beginIndex);
                        String logsDate = logsName.substring(beginIndex + 1, endIndex);
                        Date lastLogs = format.parse(logsDate);//日期类型的文件名
                        Calendar lastDate = Calendar.getInstance();
                        if (name.contains("Error")) {//报错日志保存时间延长3倍
                            //获取n天以前日志并删除
                            lastDate.add(Calendar.DATE, -3 * days);//日期回滚3n天
                            is = false;
                        } else {
                            //获取n天以前日志并删除
                            lastDate.add(Calendar.DATE, -days);//日期回滚n天
                        }
                        String beforeLog1 = format.format(lastDate.getTime());
                        Date beforeDaysLog = format.parse(beforeLog1);
                        //如果文件是在n天前的文件那么删除
                        if (lastLogs.before(beforeDaysLog)) {
                            String delPath = Path + "\\" + logsName;
                            File deleteFile = new File(delPath);
                            deleteFile.delete();
                            logger.debug("已经删除" + (is ? days : 3 * days) + "天前日志:" + delPath);
                        }

                    }
                } catch (Exception e) {
                    logError("删除日志出错", e);
                }
            }
        }
    }
}
