/**  
 * @Title:  minaCastmain.java   
 * @Package com.niocast.minathread   
 * @Description:    TODO 测试类
 * @author: Shuofang     
 * @date:   2019年1月10日 上午11:39:51   
 * @version V1.0 
 * @Copyright: 2019 
 */
package com.niocast.minathread;

import com.audioweb.util.Const;
import com.audioweb.util.Tools;
import com.niocast.util.GlobalInfoController;
import org.apache.mina.core.session.ExpiringSessionRecycler;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;


/**
 * @author Shuofang
 *	TODO
 */
public class minaCastmain
{
        private static Logger logger = LoggerFactory.getLogger(minaCastmain.class);
         
        public static void main(String[] args) throws Exception
        {
        	List<String> list = Tools.getLocalIPList();
        	String ip = GlobalInfoController.SERVERIP;
        	int loginport = Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "loginPort"));
        	int netheartPort = Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "netheartPort"));
        	if(ip != null && list.contains(ip)) {
        		CastListerning(loginport,netheartPort, ip);
        	}else {
        		CastListerning(loginport,netheartPort, null);
        	}
        	/*InetAddress  address = null;
        	address = InetAddress.getLocalHost();
	    	 String localname=address.getHostName();
	         String localip=address.getHostAddress();
	         System.out.println("本机名称是："+ localname);
	         System.out.println("本机的ip是 ："+localip);*/
        	//CastListerning(1000);
        	//CastListerning(6970);
        }
        
        static void CastListerning(int loginport,int netheartPort,String ip) {
        	// ** Acceptor设置
            NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
            // 此行代码能让你的程序整体性能提升10倍
            acceptor.getFilterChain()
                    .addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool())); 
            // 设置MINA2的IoHandler实现类
            acceptor.setHandler(new MinaCastHandler());
            // 设置会话超时时间（单位：毫秒），不设置则默认是10秒，请按需设置
            acceptor.setSessionRecycler(new ExpiringSessionRecycler(8 * 1000));
             
            // ** UDP通信配置
            DatagramSessionConfig dcfg = acceptor.getSessionConfig();
            dcfg.setReuseAddress(true);
            // 设置输入缓冲区的大小，压力测试表明：调整到2048后性能反而降低
            dcfg.setReceiveBufferSize(1024);
            // 设置输出缓冲区的大小，压力测试表明：调整到2048后性能反而降低
            dcfg.setSendBufferSize(1024);
         
            // ** UDP服务端开始侦听
            //acceptor.bind(new InetSocketAddress(6970));
            try {
            	if(ip != null) {
            		acceptor.bind(new InetSocketAddress(ip,loginport));
            		acceptor.bind(new InetSocketAddress(ip,netheartPort));
            	}else {
            		acceptor.bind(new InetSocketAddress(loginport));
            		acceptor.bind(new InetSocketAddress(netheartPort));
            	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         
            logger.info("UDP服务器正在端口 "+loginport+","+netheartPort+" 上监听中...");
        }
       
}