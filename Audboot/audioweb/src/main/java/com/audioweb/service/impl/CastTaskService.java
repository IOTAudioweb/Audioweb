package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.service.CastTaskManager;
import com.audioweb.service.LogManager;
import com.audioweb.service.SystemManager;
import com.audioweb.util.Const;
import com.audioweb.util.FileUtil;
import com.audioweb.util.LoggerUtil;
import com.audioweb.util.Tools;
import com.niocast.cast.InterCMDProcess;
import com.niocast.cast.MulticastThread;
import com.niocast.entity.CastTaskInfo;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 组播管理服务
 */
@Service("castTaskService")
public class CastTaskService implements CastTaskManager {
    public static boolean isCreated = false;
    @Resource(name = "logService")
    private LogManager logService;
    @Resource(name = "systemService")
    private SystemManager systemService;
    @Resource(name = "daoSupport")
    private DaoSupport dao;
    private static Logger logger = LoggerFactory.getLogger(CastTaskService.class);


    /**
     * 开始文件或定时广播任务
     * 开始实时采播,控件广播,发送广播命令，向广播任务列表中添加广播任务
     *
     * @param terips
     * @return
     * @throws Exception
     */
    @Override
    public boolean startCommCastTask(CastTaskInfo castTaskinfo) {
        try {
            if (isCreated) Thread.sleep(300);//之前有任务正在创建则等待500ms
            isCreated = true;
            if (castTaskinfo.getCastTeridlist().size() > 0) {
                //开启组播
                GroupCast(castTaskinfo);
                Thread.sleep(10);
                //各分组终端判断入组
                GlobalInfoController.putThreadintoGroupPool(new RunService(castTaskinfo));
            }
            /***********************广播创建成功后的信息补充以及设置*************************/
            synchronized (castTaskinfo) {
                if (castTaskinfo != null) {
                    GlobalInfoController.addCastTaskTolist(castTaskinfo);
                    return true;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            LoggerUtil.logError(logger, e);
            isCreated = false;
        }
        return false;
    }

    /**
     * 开始终端采播、寻呼话筒
     *
     * @param castTaskInfo 需要开启广播的信息
     * @return
     */
    @Override
    public boolean startPagCastTask(CastTaskInfo castTaskInfo) {
        // TODO Auto-generated method stub
        try {
            if (isCreated) Thread.sleep(300);//之前有任务正在创建则等待500ms
            isCreated = true;
            if (castTaskInfo.getCastTeridlist().size() > 0) {
                //开启组播
                GroupCast(castTaskInfo);
                Thread.sleep(10);
                //各分组终端判断入组
                GlobalInfoController.putThreadintoGroupPool(new RunService(castTaskInfo));
            } else {
                isCreated = false;
            }
            if (Const.CASTTYPE[5].equals(castTaskInfo.getMultiCastType())) {
                byte[] enbs = InterCMDProcess.getEnable(Integer.parseInt(castTaskInfo.getTypestr().get(2)));
                GlobalInfoController.SendData(enbs, castTaskInfo.getMainTerm().getSession(), 0);
                castTaskInfo.getMainTerm().getOrderCastInfo().add(castTaskInfo);
                GlobalInfoController.RefreshTerData(castTaskInfo.getMainTerm());
            }
            /***********************广播创建成功后的信息补充以及设置*************************/
            synchronized (castTaskInfo) {
                if (castTaskInfo != null) {
                    GlobalInfoController.addCastTaskTolist(castTaskInfo);
                    return true;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            LoggerUtil.logError(logger, e);
            isCreated = false;
        }
        return false;
    }

    /**
     * 开始终端点播
     *
     * @param castTaskInfo 需要开启广播的信息
     * @return
     */
    @Override
    public boolean startPointCastTask(CastTaskInfo castTaskInfo) {
        // TODO Auto-generated method stub
        try {
            if (isCreated) Thread.sleep(300);//之前有任务正在创建则等待500ms
            isCreated = true;
            GroupCast(castTaskInfo);
            byte[] enbs = InterCMDProcess.vodFileCast(13, "0".getBytes());
            GlobalInfoController.SendData(enbs, castTaskInfo.getMainTerm().getSession(), 0);
            castTaskInfo.getMainTerm().getOrderCastInfo().add(castTaskInfo);
            GlobalInfoController.RefreshTerData(castTaskInfo.getMainTerm());
            isCreated = false;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LoggerUtil.logError(logger, e);
            isCreated = false;
        }
        synchronized (castTaskInfo) {
            if (castTaskInfo != null) {
                GlobalInfoController.addCastTaskTolist(castTaskInfo);
                return true;
            }
        }
        return false;
    }

    /**
     * 发送组播
     *
     * @param castTaskInfo
     * @return
     * @throws Exception
     */
    private void GroupCast(CastTaskInfo castTaskInfo) throws Exception {
        // 组播
        MulticastThread mfst = new MulticastThread(castTaskInfo) {
            @Override
            public void onCloseListener() {
                super.onCloseListener();
                GlobalInfoController.stopCastTaskInList(castTaskInfo.getTaskid());// 任务结束时关闭对应广播线程
            }
        };
        try {
            if (castTaskInfo.getMultiCastType().equals(Const.CASTTYPE[0]) || castTaskInfo.getMultiCastType().equals(Const.CASTTYPE[1]))
                mfst.setBathPath(systemService.getBaseAttri("文件广播目录"));
        } catch (Exception e) {
            // TODO: handle exception
            LoggerUtil.logError(logger, e);
        }
        synchronized (castTaskInfo) {
            castTaskInfo.setMct(mfst);
        }
        GlobalInfoController.putThreadintoGroupPool(mfst);
        logger.info("创建广播线程,所属任务编号：" + castTaskInfo.getTaskid());
    }

    /**
     * 广播文件信息处理
     *
     * @param type
     * @param filename
     * @return
     * @throws Exception
     */
    @Override
    public List<String> FileAnalyze(String type, String filename) throws Exception {
        List<String> filenamelist = new ArrayList<String>();
        if (type.equals(Const.CASTTYPE[0]) || type.equals(Const.CASTTYPE[1])) {//为文件广播或者定时广播的文件处理
            String[] filenames = filename == null ? null : filename.split("//");
            //内部指令
            if (filename.contains("all") && filenames.length == 2) {//全部音频
                filenamelist.addAll(FileUtil.getFiles(systemService.getBaseAttri("文件广播目录"), filenames[1], 0));
            } else if (filename.contains("all") && filenames.length == 1) {
                filenamelist.addAll(FileUtil.getFiles(systemService.getBaseAttri("文件广播目录"), null, 0));
            } else {
                String path = systemService.getBaseAttri("文件广播目录") + "\\";
                for (String file : filenames) {
                    String filepath = path + file;
                    try {
                        File statu = new File(filepath);
                        if (statu.exists()) {//判断路径是否存在
                            filenamelist.add(file);
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }

                }
            }
        } else if (type.equals(Const.CASTTYPE[4])) {//为点播时的文件处理
            try {
                String fnString = systemService.getBaseAttri("终端点播目录") + "\\" + filename;
                File statu = new File(fnString);
                if (statu.exists()) {//判断路径是否存在
                    filenamelist.add(fnString);
                }
            } catch (Exception e) {
                // TODO: handle exception
                LoggerUtil.logError(logger, e);
            }
        }
        return filenamelist;
    }

    /**
     * 信息分离处理
     *
     * @param type
     * @param filename
     * @return
     * @throws Exception
     */
    @Override
    public List<String> InfoAnalyze(String info) throws Exception {
        String[] infos = info == null ? null : info.split(",");
        List<String> infolist = new ArrayList<String>();
        for (String string : infos) {
            if (string.length() > 0) {
                infolist.add(string);
            }
        }
        return infolist;
    }

    /**
     * 终端信息处理
     *
     * @param type
     * @param filename
     * @return
     * @throws Exception
     */
    @Override
    public List<TerminalInfo> TermAnalyze(List<String> terids, List<String> domainlist, TerminalInfo mainInfo) throws Exception {
        List<TerminalInfo> teridlist = new ArrayList<>();
        for (String domid : domainlist) {// 所有组的终端共用一个线程
            if (domid.length() > 0) {
                List<TerminalInfo> terlist = GlobalInfoController.listTerByDomainId(domid);
                for (TerminalInfo terpd : terlist) {
                    if (terids.contains(terpd.getTerid())) {
                        teridlist.add(terpd);
                        terids.remove(terpd.getTerid());
                    }
                }
            }
        }
        //添加源终端在列表中的信息
        if (mainInfo != null) {
            teridlist.remove(mainInfo);
            teridlist.add(mainInfo);
        }
        return teridlist;
    }

    /**
     * 端口 绑定处理
     *
     * @param type
     * @param filename
     * @return
     * @throws Exception
     */
    public int PortAnalyze() throws Exception {
        int multicastport = GlobalInfoController.getMulticastPort();
        InetAddress localAddress;
        List<String> list = Tools.getLocalIPList();
        String ip = GlobalInfoController.SERVERIP;
        if (ip != null && list.contains(ip)) {
            localAddress = InetAddress.getByName(ip);
        } else {
            localAddress = MulticastThread.getLocalHostLANAddress();
        }
        while (!bindPort(localAddress, multicastport)) {
            multicastport = GlobalInfoController.getMulticastPort();
        }
        return multicastport;
    }

    /**
     * 查看是否能绑定ip端口
     *
     * @param localAddress
     * @param port
     * @return
     * @throws Exception
     */
    @SuppressWarnings("finally")
    private static Boolean bindPort(InetAddress localAddress, int port) throws Exception {
        boolean flag = false;
        try {
            Socket s = new Socket();
            s.bind(new InetSocketAddress(localAddress, port));
            s.close();
            flag = true;
        } catch (Exception e) {
            flag = false;
        } finally {
            return flag;
        }
    }

    /**
     * @author 10155
     * 分发广播命令线程
     */
    class RunService implements Runnable {
        CastTaskInfo castTaskinfo;

        public RunService(CastTaskInfo castTaskinfo) {
            // TODO Auto-generated constructor stub
            this.castTaskinfo = castTaskinfo;
        }

        @Override
        public void run() {
            List<TerminalInfo> terminalInfos = castTaskinfo.getCastTeridlist();
            // TODO Auto-generated method stub
            try {
                if (terminalInfos != null && terminalInfos.size() > 0) {
                    if (CastTaskService.isCreated) Thread.sleep(300);
                    CastTaskService.isCreated = true;
                    try {
                        GlobalInfoController.AddAltTasks(terminalInfos, castTaskinfo);
						/*if(terminalInfos.size() > 20) {//分组进行更换任务
							int i = 20;
							for(;i<terminalInfos.size();i = i+20) {
								List<TerminalInfo> tList = terminalInfos.subList(i-20, i);
								sendChangeCast(tList,castTaskinfo);
							}
							if(terminalInfos.size() > 0 && i-20 != terminalInfos.size()) {//最后收尾
								List<TerminalInfo> tList = terminalInfos.subList(i-20, terminalInfos.size());
								sendChangeCast(tList,castTaskinfo);
							}
						}else {
							sendChangeCast(terminalInfos,castTaskinfo);
						}*/
                    } catch (Exception e) {
                        CastTaskService.isCreated = false;
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //Thread.sleep(terminalInfos.size()*2);
                    CastTaskService.isCreated = false;
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LoggerUtil.logError(logger, e);
                isCreated = false;
            }
        }

    }
    /*发送组播命令*/
	/*public void sendChangeCast(List<TerminalInfo> list,CastTaskInfo castTaskinfo) {
		GlobalInfoController.putThreadintoStandbyPool(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				GlobalInfoController.AddAltTasks(list,castTaskinfo);
			}
		});
	}*/
}
