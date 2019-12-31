/**  
 * @Title:  MinaCastTest.java   
 * @Package com.niocast.cast   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @author: Shuofang     
 * @date:   2019年1月10日 上午10:15:20   
 * @version V1.0 
 * @Copyright: 2019 
 */
package com.niocast.minathread;

import com.audioweb.util.Const;
import com.niocast.cast.InterCMDProcess;
import com.niocast.cast.TerminalUnicast;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;


/**
 * @author Shuofang
 *	TODO
 */
public class MinaCastHandler extends IoHandlerAdapter
{
    private static Logger logger = LoggerFactory.getLogger(MinaCastHandler.class);
    public static final CharsetDecoder decoder = (Charset.forName("GB2312")).newDecoder();
    public static final String  APPOINT = "APPOINT";
    public static final String  CMICNAME = "CMICNAME";
    public static final String  TERID = "TERID";
        //private int Number = 1;
    /**
     * MINA的异常回调方法。
     * <p>
     * 本类中将在异常发生时，立即close当前会话。
     * 
     * @param session 发生异常的会话
     * @param cause 异常内容
     * @see IoSession#close(boolean)
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception 
    {
        logger.error("[IMCORE]exceptionCaught捕获到错了，原因是："+cause.getMessage(), cause);
        session.closeNow();
    }
         
    /**
     * MINA框架中收到客户端消息的回调方法。
     * <p>
     * 本类将在此方法中实现完整的即时通讯数据交互和处理策略。
     * <p>
     * 为了提升并发性能，本方法将运行在独立于MINA的IoProcessor之外的线程池中，
     * 详见 {@link ServerLauncher#initAcceptor()}中的MINA设置代码 。
     * 
     * @param session 收到消息对应的会话引用
     * @param message 收到的MINA的原始消息封装对象，本类中是 {@link IoBuffer}对象
     * @throws Exception 当有错误发生时将抛出异常
     */
    @Override
    public void messageReceived(IoSession session, Object message)throws Exception 
    {
            /*GlobalInfoController.putThreadintoPool(new Runnable() {
				@Override
				public void run() {*/
		    		boolean unbindprotflag = false;
					// TODO Auto-generated method stub
					//*********************************************** 接收数据
		            // 读取收到的数据
		            IoBuffer buffer = (IoBuffer) message;
		            byte[] content = new byte[buffer.limit()];
		            buffer.get(content);
		            String print="";
					String terid = "";
					InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
					String IP = socketAddress.getAddress().getHostAddress();
					byte[] returnData = null;
					if(content[0] == 10) {
						TerminalInfo tInfo = GlobalInfoController.getTerminalInfo(session);
						try {
							if(tInfo !=null && tInfo.getOrderCastInfo().get(0).getMct().getChannel().isWritable()) {
								GlobalInfoController.putThreadintoPool(new Runnable() {//发送数据线程另开
									@Override
									public void run() {
										// TODO Auto-generated method stub
										int length = content[7] << 8 | content[6];
										byte[] data = subBytes(content,16,length);
										byte[] data1 = InterCMDProcess.sendAudioDataPackt(data);
										tInfo.getOrderCastInfo().get(0).getMct().sendMulticastPackt(data1);
									}
								});
							}else {
								returnData = InterCMDProcess.sendMainTermCast(false,IP,0,Const.CASTTYPE[3]);
								if(tInfo !=null && tInfo.getOrderCastInfo().size()>0 && tInfo.getOrderCastInfo().get(0).getMultiCastType().equals(Const.CASTTYPE[3])) {
									GlobalInfoController.stopCastTaskInList(tInfo.getCastid());
								}
							}
						} catch (Exception e) {
							e.getStackTrace();
							returnData = InterCMDProcess.sendMainTermCast(false,IP,0,Const.CASTTYPE[3]);
							if(tInfo.getOrderCastInfo().size()>0 && tInfo.getOrderCastInfo().get(0).getMultiCastType().equals(Const.CASTTYPE[3])) {
								GlobalInfoController.stopCastTaskInList(tInfo.getCastid());
							}
							// TODO: handle exception
						}
					}else {
						switch(content[1]){
						case 1://登录
							String is = null;
							terid = InterCMDProcess.getTeridFromLogin(content);
							//String ipAddress = ((InetSocketAddress)session.getRemoteAddress()).getAddress().getHostAddress();
							print = "登录，编号："+terid+"，地址："+IP;
							try {
								is = GlobalInfoController.setTerminalLogin(terid, socketAddress,session);
								if(is != null&&is.equals("true")) {
									print += "登录成功";
									session.setAttribute(TERID,terid);//会话存储终端ID号
									returnData = InterCMDProcess.returnLoginBytes();
									unbindprotflag =true;//解绑此端口
								}else if(is != null&&is.equals("fast")) {
									print += "时间间隔太短，登录失败";
								}else if(is != null&&is.equals("cast")) {
									print += "登陆成功，查询到有广播任务，尝试入组中";
									returnData = InterCMDProcess.returnLoginBytes();
									unbindprotflag =true;//解绑此端口
								}else if(is != null&&is.equals("error")) {
									print += "终端登录出现配置IP错误或其他未知错误";
								}else if(is != null&&is.equals("nothing")) {
									print += "无此终端信息";
								}
							} catch (Exception e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
							break;
						case 3://心跳包
							String heart= null;
							terid = InterCMDProcess.getTeridFromLogin(content);
							//String ip = ((InetSocketAddress)session.getRemoteAddress()).getAddress().getHostAddress();
							try {
								heart = GlobalInfoController.setTerminalOnline(terid, socketAddress, true, session);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							};
							if(heart != null&&heart.equals("true")) {
								print = "心跳包，编号："+terid+"，地址："+IP;
								session.setAttribute(TERID,terid);//会话存储终端ID号
								returnData = InterCMDProcess.returnNetHeart();
							}else if(heart != null&&heart.equals("fast")) {
								print += "时间间隔太短，弃用";
							}else if(heart != null&&heart.equals("error")) {
								print += "终端登录出现配置IP错误或其他未知错误";
							}else if(heart != null&&heart.equals("nothing")) {
								print += "无此终端信息";
							}else if(heart != null&&heart.equals("cast")) {
								session.setAttribute(TERID,terid);//会话存储终端ID号
								print += "查询到有广播任务，尝试入组中";
							}
							break;
						case 4://终端配置完成后返回信息
							if(content[6] == 49 && content[0] == 6){
								TerminalInfo tInfo = GlobalInfoController.getTerminalInfo(session);
								//session.setAttribute(TERID,"success");//更改终端存储ID为success，做前端参考
								print="终端更新配置,ID:"+session.getAttribute(TERID);
								synchronized (tInfo) {
									tInfo.getSession().closeNow();
									tInfo.setSession(null);
								}
							}
							break;
						case 27://文件广播终端返回消息
						case 40://定时广播终端返回消息
						case 41://声卡采播终端返回消息
						case 8://实时采播、终端点播返回
							if(content[6] == 49 && content[0] == 6){
								TerminalInfo tInfo = GlobalInfoController.getTerminalInfo(session);
								if(tInfo != null) {
									synchronized (tInfo) {
										tInfo.setIsCastCMDReturn(true);
										tInfo.setIscast(true);
										tInfo.setRetry(0);
									}
									if(tInfo.getOrderCastInfo().size()>0 && tInfo.getOrderCastInfo().get(0).getVol()>=0) {
										returnData = InterCMDProcess.sendVolSet(tInfo.getOrderCastInfo().get(0).getVol(),false);
										Thread.sleep(1);
									}
								}
								print="广播终端返回消息:"+IP;
							}
							break;
						case 12://点播列表
							if(content[3] == 4 && content[9] == 49) {
								String vodterid = (String) session.getAttribute(TERID);
								try {
									String iString = GlobalInfoController.setTerminalOnline(vodterid, socketAddress, false, session);
									if(iString != null&&(iString.equals("true")||iString.equals("fast"))) {
										returnData = InterCMDProcess.vodFileList(null);
										print="终端点播音频列表,"+"地址："+IP;
									}else if(iString != null&&iString.equals("error")) {
										print += "终端登录出现配置IP错误或其他未知错误";
									}else if(iString != null&&iString.equals("nothing")) {
										print += "无此终端信息";
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								};
							}
							break;
						case 13://点播播放
							 int length = content[2]<<8 | content[3];
								byte[] imotstr =new byte[length];
								System.arraycopy(content,content.length-length,imotstr,0,length);
								if(imotstr[0] == 48) {//进文件夹
									byte[]  new_imotstr= Arrays.copyOfRange(imotstr, 1, imotstr.length);
									try {
										String path = new String(new_imotstr, "GB2312");
										returnData = InterCMDProcess.vodFileList(path);
										print="终端点播音频列表进入文件夹,"+"地址："+IP; 
									} catch (UnsupportedEncodingException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}else if(imotstr[0] == 49) {//点播
									byte[]  new_imotstr= Arrays.copyOfRange(imotstr, 1, imotstr.length);
									try {
										TerminalInfo tInfo = GlobalInfoController.getTerminalInfo(session);
										String fileName = new String(new_imotstr, "GB2312");
										if(tInfo != null) {
											TerminalUnicast.Unicast(tInfo, fileName);
											print="终端点播音频,"+"地址："+IP+"	路径:"+fileName;
										}else {
											print="终端点播音频,"+"地址："+IP+"	路径:"+fileName+"，点播失败，没有此终端信息";
										}
									} catch (UnsupportedEncodingException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								break;
						case 26://点播暂停
							if(content[6] == 49) {
								byte[] order =  {49};
								TerminalInfo tInfo = GlobalInfoController.getTerminalInfo(session);
								if(TerminalUnicast.castPause(tInfo,true)) {
									print="终端点播暂停,"+"地址："+IP;
									returnData = InterCMDProcess.vodFileCast(26,order);
								}else {
									print="终端控制点播,"+"地址："+IP+"	失败";
								}
							}else {
								byte[] order =  {48};
								TerminalInfo tInfo = GlobalInfoController.getTerminalInfo(session);
								if(TerminalUnicast.castPause(tInfo,false)) {
									print="终端点播继续,"+"地址："+IP;
									returnData = InterCMDProcess.vodFileCast(26,order);
								}else {
									print="终端控制点播,"+"地址："+IP+"	失败";
								}
							}
							break;
						case 7://停止点播
							if(content[6] == 49) {
								TerminalInfo terminalInfo = GlobalInfoController.getTerminalInfo(session);
								if(terminalInfo != null) {
									TerminalUnicast.Unicaststop(terminalInfo,Const.CASTTYPE[4]);
									print="终端停止点播,"+"地址："+IP;
								}
							}
							break;
						case 84://终端采播，主动开启广播
							//if(content[6] == 49) {
								int type = content[6]-48;
								TerminalInfo terminalInfo = GlobalInfoController.getTerminalInfo(session);
								if(terminalInfo != null && type >= 0) {
									boolean iscast = TerminalUnicast.Termcast(terminalInfo, String.valueOf(type));
									if(iscast) {
										print="终端开始采播,"+"地址："+IP;
									}else {
										print="终端开始采播无权限或出错,"+"地址："+IP;
									}
								}
							//}
							break;
						case 85://终端采播，主动停止广播 ----
							if(content[6] == 49) {
								TerminalInfo terminalInfo3 = GlobalInfoController.getTerminalInfo(session);
								if(terminalInfo3 != null) {
									TerminalUnicast.Unicaststop(terminalInfo3,Const.CASTTYPE[3]);
									returnData = InterCMDProcess.sendMainTermCast(false,IP,0,Const.CASTTYPE[3]);
									print="终端停止采播,"+"地址："+IP;
								}
							}
							break;
						case 90://寻呼话筒，初始化分区
							TerminalInfo tInfo1 = GlobalInfoController.getTerminalInfo(session);
							returnData = InterCMDProcess.vodDomainList(tInfo1,null);
							print="寻呼话筒点播广播列表,"+"地址："+IP;
							session.setAttribute(APPOINT, "");
							session.setAttribute(CMICNAME, "");
							break;
						case 91://寻呼话筒,选中分区广播
							int count = content[6];
							byte[] cmic =new byte[count*14];
							//TerminalInfo tInfo3 = GlobalInfoController.getTerminalInfo(session);
							System.arraycopy(content,7,cmic,0,count*14);
							try {
								String cmicName = new String(cmic, "GB2312");
								//int taskid = TerminalUnicast.CMICCast(tInfo3, cmicName,(String)session.getAttribute(APPOINT));
								session.setAttribute(CMICNAME,cmicName);
								print="寻呼话筒点播广播列表,"+"地址："+IP +"分区:"+cmicName;
							} catch (UnsupportedEncodingException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							break;
						case 92://寻呼话筒,记录广播编号
							int ct = content[6];
							byte[] cc =new byte[14];
							String point = session.getAttribute(APPOINT) != null?(String) session.getAttribute(APPOINT):"";
							try {
								for( ; ct > 0 ;ct--) {
									System.arraycopy(content,7+14*ct,cc,0,14);
									String cmicName = new String(cc, "GB2312");
									if(!point.contains(cmicName)) {
										if(point.equals(""))
											point = cmicName;
										else
											point +=","+cmicName;
									}
								}
								session.setAttribute(APPOINT, point);
							} catch (UnsupportedEncodingException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							print="寻呼话筒点播记录广播终端或列表,"+"地址："+IP;
							break;
						case 95://寻呼话筒,开始广播&结束广播
							if(content[6] == 49) {//开始广播
								//returnData = InterCMDProcess.getEnable(TerminalUnicast.getNum());
								TerminalInfo tInfo4 = GlobalInfoController.getTerminalInfo(session);
								String tids = session.getAttribute(APPOINT) != null?(String) session.getAttribute(APPOINT):"";
								String path = session.getAttribute(CMICNAME) != null?(String) session.getAttribute(CMICNAME):"";
								TerminalUnicast.CMICCast(tInfo4, path, tids);
								print="寻呼话筒点播开始,"+"地址："+IP;
							}else if(content[6] == 48) {//结束广播
								TerminalInfo tInfo2 = GlobalInfoController.getTerminalInfo(session);
								if(tInfo2 != null)
									TerminalUnicast.Unicaststop(tInfo2,Const.CASTTYPE[5]);
								print="寻呼话筒点播结束,"+"地址："+IP;
							}
							break;
						case 96://寻呼话筒,进入分区
							byte[] dom =new byte[14];
							System.arraycopy(content,6,dom,0,14);
							try {
								TerminalInfo tInfo2 = GlobalInfoController.getTerminalInfo(session);
								String cmicName = new String(dom, "GB2312");
								returnData = InterCMDProcess.vodDomainList(tInfo2,cmicName);
							} catch (UnsupportedEncodingException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							print="寻呼话筒进入广播列表,"+"地址："+IP;
							break;
						default:
							break;
						}
						try {
							logger.debug("接收信息："+print+",源码："+new String(content,"GB2312"));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					//*********************************************** 发送数据
					if(returnData != null) {
						 IoBuffer buf = IoBuffer.wrap(returnData);
						 
						 WriteFuture future = session.write(buf);  
			            // 在100毫秒超时间内等待写完成
			            future.awaitUninterruptibly(100);
			            // The message has been written successfully
			            if(future.isWritten())
			            {
			                        // send sucess!
			            	if(unbindprotflag)
			            		session.closeNow();//关闭此次端口连接信息，因为登录端口和心跳端口不同，需要记住心跳端口，而不是登录端口.
			            }
			            // The messsage couldn't be written out completely for some reason.
			            // (e.g. Connection is closed)
			            else
			            {
			                logger.warn("回复给客户端的数据发送失败！地址："+IP);
			            }
					}
		           /*
			});*/
    }
    @Override
	 public void messageSent(IoSession session, Object message) throws Exception {
    	//logger.info("------------服务端发消息到客户端---");
	 }
	 @Override
	 public void sessionClosed(IoSession session) throws Exception {
	  // TODO Auto-generated method stub
		 logger.info("远程session关闭了一个..." + session.getRemoteAddress().toString());
	 }
	 @Override
	 public void sessionCreated(IoSession session) throws Exception {
	     //String clientIP = ((InetSocketAddress)session.getRemoteAddress()).getAddress().getHostAddress();
	     //session.setAttribute("KEY_SESSION_CLIENT_IP", clientIP);
	     //logger.info("sessionCreated, client IP: " + clientIP);
	 }

	 @Override
	 public void sessionIdle(IoSession session, IdleStatus status)
	   throws Exception {
		 //logger.info(session.getServiceAddress() +"IDS");
	 }
	 @Override
	 public void sessionOpened(IoSession session) throws Exception {
		 logger.info("连接打开："+session.getLocalAddress());
	 }
	 private static byte[] subBytes(byte[] src, int begin, int count) {
	        byte[] bs = new byte[count];
	        System.arraycopy(src, begin, bs, 0, count);
	        return bs;
	 }
}
