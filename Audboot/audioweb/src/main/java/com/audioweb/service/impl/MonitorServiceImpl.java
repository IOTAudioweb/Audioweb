package com.audioweb.service.impl;

import com.audioweb.service.IMonitorManager;
import com.audioweb.util.Bytes;
import com.niocast.entity.MonitorInfoBean;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Date;

/** */

/**
 * 获取系统信息的业务逻辑实现类.
 *
 * @author amg * @version 1.0 Creation date: 2008-3-11 - 上午10:06:06
 */
@Service("monitorService")
public class MonitorServiceImpl implements IMonitorManager {
    // 可以设置长些，防止读到运行此次系统检查时的cpu占用率，就不准了
    private static final int CPUTIME = 5000;

    private static final int PERCENT = 100;

    private static final int FAULTLENGTH = 10;

    private static final int KB = 1024;

    private static final int MB = 1024 * 1024;
    private static Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);
    /** */
    /**
     * 获得当前的监控对象.
     *
     * @return 返回构造好的监控对象
     * @throws Exception
     * @author amg * Creation date: 2008-4-25 - 上午10:45:08
     */
    @SuppressWarnings("restriction")
    public MonitorInfoBean getMonitorInfoBean() throws Exception {

        // 可使用内存
        long totalMemory = Runtime.getRuntime().totalMemory() / MB;
        // 剩余内存
        long freeMemory = Runtime.getRuntime().freeMemory() / MB;
        // 最大可使用内存
        long maxMemory = Runtime.getRuntime().maxMemory() / MB;

        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // 操作系统
        String osName = System.getProperty("os.name");
        // 总的物理内存
        long totalMemorySize = osmxb.getTotalPhysicalMemorySize() / MB;
        // 剩余的物理内存
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize() / MB;
        // 已使用的物理内存
        long usedMemory = (osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize()) / MB;

        // 获得线程总数
        ThreadGroup parentThread;
        for (parentThread = Thread.currentThread().getThreadGroup(); parentThread
                .getParent() != null; parentThread = parentThread.getParent())
            ;
        int totalThread = parentThread.activeCount();

        double cpuRatio = 0;
        if (osName.toLowerCase().startsWith("windows")) {
            cpuRatio = this.getCpuRatioForWindows();
        } else if (osName.toLowerCase().startsWith("linux")) {
            cpuRatio = this.getCpuRatioForLinux();
        }

        // 构造返回对象
        MonitorInfoBean infoBean = new MonitorInfoBean();
        infoBean.setFreeMemory(freeMemory);
        infoBean.setFreePhysicalMemorySize(freePhysicalMemorySize);
        infoBean.setMaxMemory(maxMemory);
        infoBean.setOsName(osName);
        infoBean.setTotalMemory(totalMemory);
        infoBean.setTotalMemorySize(totalMemorySize);
        infoBean.setTotalThread(totalThread);
        infoBean.setUsedMemory(usedMemory);
        infoBean.setCpuRatio(cpuRatio);
        infoBean.setSearchDate(new Date());
        return infoBean;
    }

    /** */
    /**
     * 获得CPU使用率.
     *
     * @return 返回cpu使用率
     * @author amg * Creation date: 2008-4-25 - 下午06:05:11
     */
    private double getCpuRatioForWindows() {
        try {
            String procCmd = System.getenv("windir") + "//system32//wbem//wmic.exe process get Caption,CommandLine,"
                    + "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
            // 取进程信息
            long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
            Thread.sleep(CPUTIME);
            long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
            if (c0 != null && c1 != null) {
                long idletime = c1[0] - c0[0];
                long busytime = c1[1] - c0[1];
                return Math.floor(Double.valueOf(PERCENT * PERCENT * (busytime) / (busytime + idletime)).doubleValue()) / PERCENT;
            } else {
                return 0.0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Purpose:采集CPU使用率
     *
     * @param args
     * @return float, CPU使用率, 小于1
     */
    public double getCpuRatioForLinux() {
        float cpuUsage = 0;
        Process pro1, pro2;
        Runtime r = Runtime.getRuntime();
        try {
            String command = "cat /proc/stat";
            //第一次采集CPU时间
            long startTime = System.currentTimeMillis();
            pro1 = r.exec(command);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(pro1.getInputStream()));
            String line = null;
            long idleCpuTime1 = 0, totalCpuTime1 = 0;    //分别为系统启动后空闲的CPU时间和总的CPU时间
            while ((line = in1.readLine()) != null) {
                if (line.startsWith("cpu")) {
                    line = line.trim();
                    logger.info(line);
                    String[] temp = line.split("\\s+");
                    idleCpuTime1 = Long.parseLong(temp[4]);
                    for (String s : temp) {
                        if (!s.equals("cpu")) {
                            totalCpuTime1 += Long.parseLong(s);
                        }
                    }
                    break;
                }
            }
            in1.close();
            pro1.destroy();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.error("CpuUsage休眠时发生InterruptedException.", e);
                logger.error(sw.toString());
            }
            //第二次采集CPU时间
            long endTime = System.currentTimeMillis();
            pro2 = r.exec(command);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
            long idleCpuTime2 = 0, totalCpuTime2 = 0;    //分别为系统启动后空闲的CPU时间和总的CPU时间
            while ((line = in2.readLine()) != null) {
                if (line.startsWith("cpu")) {
                    line = line.trim();
                    logger.info(line);
                    String[] temp = line.split("\\s+");
                    idleCpuTime2 = Long.parseLong(temp[4]);
                    for (String s : temp) {
                        if (!s.equals("cpu")) {
                            totalCpuTime2 += Long.parseLong(s);
                        }
                    }
                    logger.info("IdleCpuTime: " + idleCpuTime2 + ", " + "TotalCpuTime" + totalCpuTime2);
                    break;
                }
            }
            if (idleCpuTime1 != 0 && totalCpuTime1 != 0 && idleCpuTime2 != 0 && totalCpuTime2 != 0) {
                cpuUsage = 1 - (float) (idleCpuTime2 - idleCpuTime1) / (float) (totalCpuTime2 - totalCpuTime1);
                logger.info("本节点CPU使用率为: " + cpuUsage);
            }
            in2.close();
            pro2.destroy();
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("CpuUsage休眠时发生InterruptedException. {}", e);
            logger.error(sw.toString());
        }
        return Math.floor(cpuUsage * PERCENT * PERCENT) / PERCENT;
    }
    /** */
    /**
     * 读取CPU信息.
     *
     * @param proc
     * @return
     * @author amg * Creation date: 2008-4-25 - 下午06:10:14
     */
    private long[] readCpu(final Process proc) {
        long[] retn = new long[2];
        try {
            proc.getOutputStream().close();
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line = input.readLine();
            if (line == null || line.length() < FAULTLENGTH) {
                return null;
            }
            int capidx = line.indexOf("Caption");
            int cmdidx = line.indexOf("CommandLine");
            int rocidx = line.indexOf("ReadOperationCount");
            int umtidx = line.indexOf("UserModeTime");
            int kmtidx = line.indexOf("KernelModeTime");
            int wocidx = line.indexOf("WriteOperationCount");
            long idletime = 0;
            long kneltime = 0;
            long usertime = 0;
            while ((line = input.readLine()) != null) {
                if (line.length() < wocidx) {
                    continue;
                }
                // 字段出现顺序：Caption,CommandLine,KernelModeTime,ReadOperationCount,
                // ThreadCount,UserModeTime,WriteOperation
                String caption = Bytes.substring(line, capidx, cmdidx - 1).trim();
                String cmd = Bytes.substring(line, cmdidx, kmtidx - 1).trim();
                if (cmd.indexOf("wmic.exe") >= 0) {
                    continue;
                }
                // log.info("line="+line);
                if (caption.equals("System Idle Process") || caption.equals("System")) {
                    idletime += Long.valueOf(Bytes.substring(line, kmtidx, rocidx - 1).trim()).longValue();
                    idletime += Long.valueOf(Bytes.substring(line, umtidx, wocidx - 1).trim()).longValue();
                    continue;
                }

                kneltime += Long.valueOf(Bytes.substring(line, kmtidx, rocidx - 1).trim()).longValue();
                usertime += Long.valueOf(Bytes.substring(line, umtidx, wocidx - 1).trim()).longValue();
            }
            retn[0] = idletime;
            retn[1] = kneltime + usertime;
            return retn;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                proc.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /** */
    /**
     * 测试方法.
     *
     * @param args
     * @throws Exception
     * @author amg * Creation date: 2008-4-30 - 下午04:47:29
     */
	/*public static void main(String[] args) throws Exception {
		IMonitorManager service = new MonitorServiceImpl();
		MonitorInfoBean monitorInfo = service.getMonitorInfoBean();
		System.out.println("cpu占有率=" + monitorInfo.getCpuRatio());

		System.out.println("可使用内存=" + monitorInfo.getTotalMemory());
		System.out.println("剩余内存=" + monitorInfo.getFreeMemory());
		System.out.println("最大可使用内存=" + monitorInfo.getMaxMemory());

		System.out.println("操作系统=" + monitorInfo.getOsName());
		System.out.println("总的物理内存=" + monitorInfo.getTotalMemorySize() + "kb");
		System.out.println("剩余的物理内存=" + monitorInfo.getFreeMemory() + "kb");
		System.out.println("已使用的物理内存=" + monitorInfo.getUsedMemory() + "kb");
		System.out.println("线程总数=" + monitorInfo.getTotalThread() + "kb");
	}*/
}