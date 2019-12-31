/**  
 * @Title:  MinaCastThread.java   
 * @Package com.niocast.cast   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @author: Shuofang     
 * @date:   2019年1月10日 下午3:22:16   
 * @version V1.0 
 * @Copyright: 2019 
 */
package com.niocast.minathread;

import com.niocast.util.GlobalInfoController;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.ExpiringSessionRecycler;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

;

/**
 * @author Shuofang
 *	TODO
 */
public class MinaCastThread extends Thread {
	private MinaCastHandler mHandler;
	private NioDatagramAcceptor Acceptor;
	//private NioDatagramAcceptor netheartAcceptor;
	private Integer loginport; 
	private Integer netheartPort; 
	private String ip = null;
    private static Logger logger = LoggerFactory.getLogger(MinaCastThread.class);
    
    /**
	 * 
	 * TODO 无地址初始化
	 * 时间：2019年1月10日
	 */
	public MinaCastThread(int loginport,int netheartPort) {
		// TODO Auto-generated constructor stub
		this.loginport = loginport;
		this.netheartPort = netheartPort;
	}
	/**
	 * 
	 * TODO 有地址初始化
	 * 时间：2019年1月10日
	 */
	public MinaCastThread(int loginport,int netheartPort,String ip) {
		// TODO Auto-generated constructor stub
		this.loginport = loginport;
		this.netheartPort = netheartPort;
		this.ip = ip;
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		mHandler = new MinaCastHandler();
		// TODO Auto-generated method stub
		// ** loginAcceptor设置
        Acceptor = new NioDatagramAcceptor();
        // 此行代码能让你的程序整体性能提升10倍
        DefaultIoFilterChainBuilder chain = Acceptor.getFilterChain();   
        LoggingFilter loggingFilter = new LoggingFilter();   
        loggingFilter.setMessageSentLogLevel(LogLevel.NONE);
        loggingFilter.setMessageReceivedLogLevel(LogLevel.NONE);
        chain.addLast("logger", loggingFilter);
        chain.addLast("IOThreadPool", new ExecutorFilter(GlobalInfoController.getExecutorService()));
        // 设置MINA2的IoHandler实现类
        Acceptor.setHandler(mHandler);
        // 设置会话超时时间（单位：毫秒），不设置则默认是10秒，请按需设置
        Acceptor.setSessionRecycler(new ExpiringSessionRecycler(8 * 1000));
        // ** UDP通信配置
        DatagramSessionConfig logindcfg = Acceptor.getSessionConfig();
        logindcfg.setReuseAddress(true);//运行端口重用
        // 设置输入缓冲区的大小，压力测试表明：调整到2048后性能反而降低
        //logindcfg.setReceiveBufferSize(1024);
        // 设置输出缓冲区的大小，压力测试表明：调整到2048后性能反而降低
        //logindcfg.setSendBufferSize(1024);
        //acceptor.bind(arg0, arg1);
        // ** UDP服务端开始侦听
        //acceptor.bind(new InetSocketAddress(6970));
      /*  ////** netheartAcceptor设置
        netheartAcceptor = new NioDatagramAcceptor();
        // 此行代码能让你的程序整体性能提升10倍
        netheartAcceptor.getFilterChain()
                .addLast("netheartIOThreadPool", new ExecutorFilter(Executors.newCachedThreadPool())); 
        // 设置MINA2的IoHandler实现类
        netheartAcceptor.setHandler(mHandler);
        // 设置会话超时时间（单位：毫秒），不设置则默认是10秒，请按需设置
        netheartAcceptor.setSessionRecycler(new ExpiringSessionRecycler(8 * 1000));
         
        // ** UDP通信配置
        DatagramSessionConfig netheartdcfg = netheartAcceptor.getSessionConfig();
        netheartdcfg.setReuseAddress(true);*/
        // 设置输入缓冲区的大小，压力测试表明：调整到2048后性能反而降低
        //dcfg.setReceiveBufferSize(1024);
        // 设置输出缓冲区的大小，压力测试表明：调整到2048后性能反而降低
        //dcfg.setSendBufferSize(1024);
        //acceptor.bind(arg0, arg1);
        // ** UDP服务端开始侦听
        //acceptor.bind(new InetSocketAddress(6970));
        try {
        	if(ip != null) {
        		Acceptor.bind(new InetSocketAddress(ip,loginport));
        		Acceptor.bind(new InetSocketAddress(ip,netheartPort));
        	}else {
        		Acceptor.bind(new InetSocketAddress(loginport));
        		Acceptor.bind(new InetSocketAddress(netheartPort));
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
        logger.info("UDP服务器正在端口 "+loginport+","+netheartPort+" 上监听中...");
        if(ip != null) {
        	logger.info("UDP服务器监听IP为 "+ip);
        }
	}
	/**
	 * @return the mHandler
	 */
	public MinaCastHandler getmHandler() {
		return mHandler;
	}
	/**
	 * @return the acceptor
	 */
	public NioDatagramAcceptor getAcceptor() {
		return Acceptor;
	}
}
