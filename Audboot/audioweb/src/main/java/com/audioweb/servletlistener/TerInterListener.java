package com.audioweb.servletlistener;

import com.audioweb.service.impl.MonitorServiceImpl;
import com.audioweb.util.Const;
import com.audioweb.util.Jurisdiction;
import com.audioweb.util.Tools;
import com.niocast.entity.CastTaskInfo;
import com.niocast.entity.MonitorInfoBean;
import com.niocast.minatcpservice.TCPMinaCastThread;
import com.niocast.minathread.MinaCastThread;
import com.niocast.util.GlobalInfoController;
import org.apache.shiro.session.Session;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TerInterListener implements ServletContextListener {

    //private Thread heartThread;
    //private Thread netheartThread;
    /**
     * 获取活跃的 cpu数量
     */
    private final static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private final static BlockingQueue<Runnable> mWorkQueue = new LinkedBlockingQueue<Runnable>();
    ;
    private final static long KEEP_ALIVE_TIME = 3L;
    private final static TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private ThreadFactory mThreadFactory = new MyThreadFactory(2, "IOThread-");
    private ThreadFactory groupThreadFactory = new MyThreadFactory(1, "GroupThread-");
    private ThreadFactory standbyThreadFactory = new MyThreadFactory(1, "StandbyThread-");

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        //关闭所有任务
        List<CastTaskInfo> castTaskInfos = GlobalInfoController.getCastTasklistInfos();
        for (int i = castTaskInfos.size(); i > 0; i--) {
            GlobalInfoController.stopCastTaskInList(castTaskInfos.get(i - 1).getTaskid());
        }
        //关闭线程池
        GlobalInfoController.getExecutorService().shutdown();
        GlobalInfoController.getScheduledThreadPool().shutdown();
        GlobalInfoController.getGroupexecutorService().shutdown();
        GlobalInfoController.getStandbyexecutorService().shutdown();

        //关闭IO处理器
        if (GlobalInfoController.getCastThread().getAcceptor() != null) {
            GlobalInfoController.getCastThread().getAcceptor().unbind();
            GlobalInfoController.getCastThread().getAcceptor().setCloseOnDeactivation(true);
            GlobalInfoController.getCastThread().getAcceptor().dispose();
        }

    }

    @Override
    public void contextInitialized(ServletContextEvent e) {
        System.out.println("Server contextInitialized over");
        //初始化线程池
        GlobalInfoController.setExecutorService(new ThreadPoolExecutor((int) (NUMBER_OF_CORES),
                NUMBER_OF_CORES * 4, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT,
                mWorkQueue, mThreadFactory));//创建一个处理IO的线程池
        GlobalInfoController.setScheduledThreadPool(Executors.newScheduledThreadPool(NUMBER_OF_CORES));//定时器线程池
        GlobalInfoController.setGroupexecutorService(new ThreadPoolExecutor(NUMBER_OF_CORES > 4 ? 4 : NUMBER_OF_CORES,
                NUMBER_OF_CORES * 3, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT,
                mWorkQueue, groupThreadFactory));//创建一个处理组播的线程池
        GlobalInfoController.setStandbyexecutorService(new ThreadPoolExecutor(NUMBER_OF_CORES > 4 ? 4 : NUMBER_OF_CORES,
                NUMBER_OF_CORES * 2, KEEP_ALIVE_TIME - 1, KEEP_ALIVE_TIME_UNIT,
                mWorkQueue, standbyThreadFactory));//创建一个分发数据或者给任务待机的线程池
        //初始化组播全局信息定时重置线程
        GlobalInfoController.getScheduledThreadPool().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {//每3天执行一次
                GlobalInfoController.resetMultiCastGlobalInfo();
            }
        }, 3, 3, TimeUnit.DAYS);
        GlobalInfoController.getScheduledThreadPool().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {//每天执行一次删除日志操作
                DelLogs.delAdvert();
            }
        }, 0, 1, TimeUnit.DAYS);
        GlobalInfoController.getScheduledThreadPool().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {//每30秒执行一次
                try {
                    MonitorServiceImpl serviceImpl = new MonitorServiceImpl();
                    MonitorInfoBean bean = serviceImpl.getMonitorInfoBean();
                    if (bean != null) {
                        GlobalInfoController.addInfoBaen(bean);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
//				CastTaskInfo.resetTaskid();
            }
        }, 30, 30, TimeUnit.SECONDS);
        GlobalInfoController.getScheduledThreadPool().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {//每20分钟执行一次
                try {
                    long now = new Date().getTime();
                    List<Session> sessions = Jurisdiction.getSessions();
                    for (int i = sessions.size() - 1; i >= 0; i--) {
                        if (now - sessions.get(i).getLastAccessTime().getTime() > 1200000) {
                            sessions.get(i).stop();
                            sessions.remove(i);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//				CastTaskInfo.resetTaskid();
            }
        }, 20, 20, TimeUnit.MINUTES);
        MinaCastThread castThread;
        TCPMinaCastThread tcpMinaCastThread;
        List<String> list = Tools.getLocalIPList();
        String ip = Tools.GetValueByKey(Const.CONFIG, "serverIp");
        int ClientPort = Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "qtClientPort"));
        int loginport = Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "loginPort"));
        int netheartPort = Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "netheartPort"));
        if (ip != null && list.contains(ip)) {
            castThread = new MinaCastThread(loginport, netheartPort, ip);
        } else {
            castThread = new MinaCastThread(loginport, netheartPort);
        }
        tcpMinaCastThread = new TCPMinaCastThread(ClientPort);
        castThread.start();
        tcpMinaCastThread.start();
        GlobalInfoController.setCastThread(castThread);
        GlobalInfoController.setTcpThread(tcpMinaCastThread);

        GlobalInfoController.falshSqlTerminals();
        GlobalInfoController.setDate(System.currentTimeMillis());
        GlobalInfoController.initServerConfig(1);
    }


    private class MyThreadFactory implements ThreadFactory {
        private AtomicInteger threadNumberAtomicInteger = new AtomicInteger(1);
        private String name = "ThreadFactory";

        public MyThreadFactory(int size, String name) {
            // TODO Auto-generated constructor stub
            this.name = name;
            threadNumberAtomicInteger = new AtomicInteger(size);
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, String.format(Locale.CHINA, "%s%d", name, threadNumberAtomicInteger.getAndIncrement()));
            /* thread.setDaemon(true);//是否是守护线程
            thread.setPriority(Thread.NORM_PRIORITY);//设置优先级 1~10 有3个常量 默认 Thread.MIN_PRIORITY*/
            return thread;
        }
    }

}
