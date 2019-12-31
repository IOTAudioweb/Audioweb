package com.audioweb.service;

import com.niocast.entity.CastTaskInfo;
import com.niocast.entity.TerminalInfo;

import java.util.List;

/**
 * 说明：创建，管理广播的服务接口
 */

public interface CastTaskManager {
    /**
     * 开始文件广播任务、定时广播任务，发送广播命令，向广播任务列表中添加广播任务
     *
     * @param castTaskInfo 需要开启广播的信息
     * @return
     */
    public boolean startCommCastTask(CastTaskInfo castTaskinfo);

    /**
     * 开始终端采播、寻呼话筒
     *
     * @param castTaskInfo 需要开启广播的信息
     * @return
     */
    public boolean startPagCastTask(CastTaskInfo castTaskinfo);

    /**
     * 开始终端点播
     *
     * @param castTaskInfo 需要开启广播的信息
     * @return
     */
    public boolean startPointCastTask(CastTaskInfo castTaskinfo);

    /**
     * 广播文件信息处理
     *
     * @param type
     * @param filename
     * @return
     * @throws Exception
     */
    public List<String> FileAnalyze(String type, String filename) throws Exception;

    /**
     * 端口绑定处理
     *
     * @return port
     * @throws Exception
     */
    public int PortAnalyze() throws Exception;

    /**
     * 终端信息处理
     *
     * @param type
     * @param filename
     * @return
     * @throws Exception
     */
    public List<TerminalInfo> TermAnalyze(List<String> terids, List<String> domainlist, TerminalInfo mainInfo) throws Exception;

    /**
     * 信息分离处理
     *
     * @param type
     * @param filename
     * @return
     * @throws Exception
     */
    public List<String> InfoAnalyze(String info) throws Exception;
}
