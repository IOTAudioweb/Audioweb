package com.audioweb.service;

import java.util.List;

/**
 * 说明：创建，管理组播的服务接口
 */

public interface MulticastManager {
    /**
     * 开始文件广播任务，发送广播命令，向广播任务列表中添加广播任务
     *
     * @param terips   需要广播终端的ip组 ，格式如 "1001,1002,1003"
     * @param domids   需要广播的分组的id，格式同上
     * @param filename 需要广播的文件地址
     * @param vol      广播音量
     * @return
     * @throws Exception
     */
    public int startFileCastTask(String terids, String domids, String filename, int vol, int castLevel, String lastingSeconds, Boolean isLooping, List<String> type)
            throws Exception;
}
