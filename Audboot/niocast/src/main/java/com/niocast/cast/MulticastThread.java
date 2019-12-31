package com.niocast.cast;

import com.audioweb.entity.ScheTask;
import com.audioweb.service.impl.CastTaskService;
import com.audioweb.service.impl.QuartzService;
import com.audioweb.service.impl.ScheTaskService;
import com.audioweb.util.*;
import com.niocast.entity.CastTaskInfo;
import com.niocast.entity.PointCastInfo;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.GlobalInfoController;
import com.niocast.util.TimeReloady;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.NetUtil;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 组播发送线程，可用于发送数据和组播文件
 * @author HTT
 *
 */
public class MulticastThread extends BaseLogger<MulticastThread> implements Runnable {
	private QuartzService quartzService = (QuartzService) SpringContextUtils.getBeanByClass(QuartzService.class);
	private ScheTaskService schetaskservice = (ScheTaskService) SpringContextUtils.getBeanByClass(ScheTaskService.class);
	public static final short DataLenght = 800;//每次数据包发送文件最大长度
	//private String groupIP;
	//private int groupPort;
	private Channel channel;
	EventLoopGroup group = null;
	private Boolean isopen = false;//是否打开了组播端口
	private Boolean isture = false;//是否完整开启了组播
	InetSocketAddress groupAddress;
	//广播核心信息
	private CastTaskInfo selfCastinfo;
	//定时器模式实时采播线程
	private SendCastData sendCastData;
	private volatile List<Frame> framePool;
	//文件广播、定时广播信息
	private List<String> fileinfo = new ArrayList<String>();;//文件广播时动态存储的信息表，0:获取路径;1:获取每毫秒播放的字节数;2:获取音频总时长(秒);3:获取音频播放位置(秒);4:获取音频播放时间(long);5:暂停标志位(long);6暂停存储位（毫秒）
	private int bzzzik = -1;//倒计时
	private BufferedInputStream in ;//读取文件信息
	private Timer casttimer;//发送音频信息定时线程
	private SendFileData sendFileData;//发送音频信息循环线程
	private int bitsize=0;//分包大小
	private int timesize=0;//时间分片大小，单位为ms
	private short filelistin = 0;//文件广播指针
	private String bathPath;//文件默认根目录
	private volatile int num=0;//终端采播判断终端是否发包以及发包数
	/**
	 * 组播发送线程
	 * @param groupIP
	 * @param groupPort
	 * @param isfilecast 是否是组播，为true表示文组播，并设置文件地址setFilePath()
	 * @param lastingSeconds 
	 * 			播放时长
	 * @param tasktype 
	 * 			文件广播播放类型
	 * @param type 
	 * 			广播类型
	 */
/*	public MulticastThread(String groupIP, int groupPort,List<String> types) {
		super();
		this.groupIP = groupIP;
		this.groupPort = groupPort;
		groupAddress = new InetSocketAddress(groupIP, groupPort);
		this.types = types;
	}
	public MulticastThread(String groupIP, int groupPort,String lastingSeconds,List<String> types) {
		super();
		//this.groupIP = groupIP;
		//this.groupPort = groupPort;
		groupAddress = new InetSocketAddress(groupIP, groupPort);
		if(lastingSeconds != null)
			this.bzzzik =Integer.parseInt(lastingSeconds)*1000;//ms分片值
		if(types.get(0).equals("3") && !lastingSeconds.equals("-1"))
			this.bzzzik = Integer.parseInt(lastingSeconds)*32;
		//this.types = types;
	}
*/
	/**
	 * 组播发送线程
	 * @param castTaskInfo 
	 * 			广播核心信息
	 */
	public MulticastThread(CastTaskInfo castTaskInfo) {
		// TODO Auto-generated constructor stub
		this.selfCastinfo = castTaskInfo;
		if(castTaskInfo.getMulticastaddress() != null)
			groupAddress = new InetSocketAddress(castTaskInfo.getMulticastaddress(), castTaskInfo.getMulticastport());
		if(selfCastinfo.getMultiCastType().equals(Const.CASTTYPE[1]) && selfCastinfo.getLastingSeconds() > 0) {//定时广播
			this.bzzzik =selfCastinfo.getLastingSeconds()*1000;//ms分片值
		}else if(selfCastinfo.getMultiCastType().equals(Const.CASTTYPE[3]) && selfCastinfo.getLastingSeconds() > 0) {//终端采播
			this.bzzzik =selfCastinfo.getLastingSeconds()*1000;//ms分片值
		}else {
			this.bzzzik = -1000;
		}
	}
	@Override
	public void run() {
		try {
			selfCastinfo.setIsCast(true);
			if(!Const.CASTTYPE[4].equals(selfCastinfo.getMultiCastType()) && !Const.CASTTYPE[5].equals(selfCastinfo.getMultiCastType()) && !Const.CASTTYPE[6].equals(selfCastinfo.getMultiCastType())) {//点播,寻呼话筒不启用组播,实时采播客户端也不启用
				channel = openMulticast(selfCastinfo);
			}else {
				logger.debug("点播正常启动，任务编号："+selfCastinfo.getTaskid());
				isopen = true;
			}
			switch (selfCastinfo.getMultiCastType()) {
			case "文件广播":
			case "定时广播":
				if(null != selfCastinfo.getFilelist() && selfCastinfo.getFilelist().size() > 0) {
					while(selfCastinfo.getFilelist().size() > 0 && !NewFileRead(selfCastinfo.getFilelist().get(filelistin++))) {
						saveLog(Const.LOGTYPE[2], selfCastinfo.getMultiCastType(), "读取音频文件出错", InetAddress.getLocalHost().getHostAddress(),selfCastinfo.getFilelist().get(filelistin-1));
						selfCastinfo.getFilelist().remove(--filelistin);
					}
					if(timesize > 0 ) {
						casttimer = new Timer();
						sendFileData = new SendFileData(this);
						casttimer.scheduleAtFixedRate(sendFileData,0,timesize);//动态延时调整
						refreshTime(0);
					}else {
						isopen =false;
						saveLog(Const.LOGTYPE[2], selfCastinfo.getMultiCastType(), "开启广播失败", InetAddress.getLocalHost().getHostAddress(),"读取音频文件出错");
					}
					//sendFileData();
				}else {
					isopen =false;
					saveLog(Const.LOGTYPE[2], selfCastinfo.getMultiCastType(), "开启广播失败", InetAddress.getLocalHost().getHostAddress(),"未发现音频文件");
				}
				break;
			case "实时采播":
				framePool = new LinkedList<>();
				if(selfCastinfo.getIsTimer()){
					casttimer = new Timer();
					sendCastData = new SendCastData(framePool,channel, groupAddress,this);
					casttimer.scheduleAtFixedRate(sendCastData, 5, 48);//延时?ms
				}
				/*casttimer = new Timer();
				sendCastData = new SendCastData(channel, groupAddress,this);
				casttimer.scheduleAtFixedRate(sendCastData, 5, 48);//延时48ms
*/				/*cds = new DataSendChannel(channel,groupAddress);
				cds.open();*/
				break;
			case "终端采播":
				num=1;
				if(this.bzzzik > 0 && selfCastinfo.getIsupper()) {
					casttimer = new Timer();
					casttimer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							bzzzik -= 1000;
							if(num <= 0 ||bzzzik <= 0 || !channel.isOpen()) {//使用倒计时判断源终端是否掉线，若掉线则直接结束广播
								close();
								this.cancel();
							}
							num = -1;
						}
					}, 1000, 1000);//延时1s
				}else{
					casttimer = new Timer();
					casttimer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if(num <= 0 || !channel.isOpen()) {
								close();
								this.cancel();
							}
							num = -1;
						}
					}, 1000, 3000);//延时1s
				}
				/*TerminalInfo tInfo = selfCastinfo.getMainTerm();
				byte[] enb =  InterCMDProcess.sendMainTermCast(true, selfCastinfo.getTypestr().get(2), Integer.parseInt(selfCastinfo.getTypestr().get(3)), selfCastinfo.getMultiCastType());
				GlobalInfoController.SendData(enb, tInfo.getSession(), 50);*/
			/*	UnicastThread netheartsrt  = GlobalInfoController.getNetheartsrt();
				handler=  netheartsrt.getuHandler();
				handler.SaveAll(channel, this, groupAddress);*/
				break;
			case "终端点播":
				if(null != selfCastinfo.getFilelist() && selfCastinfo.getFilelist().size() > 0) {
					NewFileRead(selfCastinfo.getFilelist().get(filelistin++));
					if(timesize > 0 ) {
						TerminalInfo PointtInfo = selfCastinfo.getMainTerm();
						PointCastInfo pointCastInfo = new PointCastInfo(selfCastinfo.getTaskid(), PointtInfo,selfCastinfo, bitsize, timesize, in, false, PointtInfo.getSession());
						SendPointData sData = GlobalInfoController.getsendPointData(timesize);
						if(sData != null) {
							sData.putPointCastInfo(pointCastInfo);
						}else{
							sData = new SendPointData(new Timer(), timesize, pointCastInfo);
						}
						refreshTime(0);
					}else {
						isopen =false;
						saveLog(Const.LOGTYPE[2], selfCastinfo.getMultiCastType(), "开启广播失败,读取文件出错", InetAddress.getLocalHost().getHostAddress(),selfCastinfo.getFilelist().get(0));
					}
				}
				break;
			case "寻呼话筒":
				TerminalInfo tInfo1 = selfCastinfo.getMainTerm();
				byte[] enbs =  InterCMDProcess.reply(selfCastinfo.getMulticastaddress(),String.valueOf(selfCastinfo.getMulticastport()));
				GlobalInfoController.SendData(enbs, tInfo1.getSession(), 50);
				/*UnicastThread vodthread1  = GlobalInfoController.getNetheartsrt();
				byte[] enbs =  InterCMDProcess.reply(groupIP,String.valueOf(groupPort));
				InetSocketAddress groupAddress = new InetSocketAddress(types.get(1), Integer.valueOf(types.get(2)));
				vodthread1.sendCMDInfo(groupAddress,enbs);*/
			case "控件广播":
				//无
				break;
			default:
				break;
			}
			//Thread.sleep(150);//让其他线程做好准备，在开发确认命令
			if(isopen) {
				isture = true;//确认广播已经开启
				selfCastinfo.setIsStop(false);//设置为播放
				/*if(channel != null && selfCastinfo.getVol() > 0) {//重新发送组播时的音量调节,确保音量正确
					sendMulticastPackt(InterCMDProcess.sendVolSet(selfCastinfo.getVol(),false));
				}*/
				//等待通道关闭
				if(channel != null)
					channel.closeFuture().sync();
			}else {
				selfCastinfo.setIsCast(false);
				//Thread.sleep(300);//让其他线程做好准备，在关闭任务
				isopen = true;
				close();
			}
			// 关闭连接
			//close();
//			channel.close().awaitUninterruptibly();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logError(e);
			selfCastinfo.setIsCast(false);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			logError(e1);
			selfCastinfo.setIsCast(false);
		}
	}
	/**
	 * 打开组播模式
	 * @param castTaskInfo
	 */
	public Channel openMulticast(CastTaskInfo castTaskInfo){
		Channel channel = null;
		try {
			group = new NioEventLoopGroup(2,GlobalInfoController.getGroupexecutorService());
			InetAddress localAddress;
			NetworkInterface ni = NetUtil.LOOPBACK_IF;
			List<String> list = Tools.getLocalIPList();
			String ip = GlobalInfoController.SERVERIP;
			if(list.contains(ip)) {
				localAddress = InetAddress.getByName(ip);
			}else {
				localAddress = getLocalHostLANAddress();
			}
			Bootstrap b = new Bootstrap();
			b.group(group)
			.localAddress(localAddress,castTaskInfo.getMulticastport())
			.channel(NioDatagramChannel.class)
			.option(ChannelOption.IP_MULTICAST_IF, ni)
			.option(ChannelOption.SO_REUSEADDR, true)
			.handler(new ChannelInitializer<NioDatagramChannel>() {
				@Override
				public void initChannel(NioDatagramChannel ch) throws Exception {
					ch.pipeline().addLast(new MulticastHandler());
				}
			});
			ChannelFuture channelFuture = b.bind().sync();
			if (channelFuture.isSuccess()) {
				logger.debug("组播正常启动，任务编号："+selfCastinfo.getTaskid());
				isopen = true;
//				logger.info("正常日志  信息： 开始启动Stocket监听，端口:" + PORT + "；" );
			}
			
			channel = channelFuture.channel();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			logError(e);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			logError(e1);
		}
		return channel;
	}
	
/**
 * 发送组播数据，在发送前请先getIsopen()查看组播通道是否打开 可以用于修改广播音量等操作
 * @param senddata
 */
	public void sendMulticastPackt(byte[] senddata){
		try {
			if(channel != null)
			channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(senddata),
					groupAddress)).sync();
			if(selfCastinfo.getMultiCastType().equals(Const.CASTTYPE[3])) {
				num++;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logError(e);
		}
	}
	/**
	 * 实时广播发送组播音频数据
	 * @param
	 */
	public void sendRealMulticastAudioPackt(Frame frame){
		if(isture && frame != null) {
			if(!selfCastinfo.getIsTimer())
				GlobalInfoController.putThreadintoPool(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(InterCMDProcess.sendAudioDataPackt(frame.data)),groupAddress)).sync();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			else{
				synchronized (framePool) {
					framePool.add(frame);
					Collections.sort(framePool);
				}
			}
		}
	}
	public void close(){
		//System.out.println("stop");
		if(isopen) {
			selfCastinfo.setIsCast(false);
			isopen = false;//关闭线程标识
			logger.debug("关闭组播,任务编号："+selfCastinfo.getTaskid());
			//广播退组
			try {
				if(channel != null && channel.isOpen()) {
					try {
						byte[] endbs = InterCMDProcess.sendCast(false,"", 0,0,selfCastinfo.getMultiCastType());
						channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(endbs),
								groupAddress)).sync();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//寻呼话筒源终端退出
				if(Const.CASTTYPE[5].equals(selfCastinfo.getMultiCastType())) {
					TerminalInfo terminalInfo = selfCastinfo.getMainTerm();
					if(terminalInfo != null && terminalInfo.getOrderCastInfo().size() > 0 && terminalInfo.getOrderCastInfo().get(0).getMultiCastType().equals(Const.CASTTYPE[5])) {
						byte[] endbs = InterCMDProcess.sendCast(false, null, 0, 0, Const.CASTTYPE[5]);
						GlobalInfoController.SendData(endbs, terminalInfo.getSession(), 0);
					}
				}
				if(casttimer != null) {
					casttimer.cancel();
				}
				//点播
				/*if(Const.CASTTYPE[4].equals(selfCastinfo.getMultiCastType())){
					TerminalInfo tInfo = selfCastinfo.getMainTerm();
					if(tInfo.getMultiCastType().equals(selfCastinfo.getMultiCastType())) {
						byte[] enbs =  InterCMDProcess.vodFileCast(7,"".getBytes());
						GlobalInfoController.SendData(enbs, tInfo.getSession(), 0);
					}else if(tInfo.getOrderCastInfo().contains(selfCastinfo)) {
						tInfo.getOrderCastInfo().remove(selfCastinfo);
					}
				}*/
			}  catch (Exception e) {
				// TODO Auto-generated catch block
				logError(e);
			}
			//若为定时任务则更新对应定时任务的信息
			try {
				if(Const.CASTTYPE[1].equals(selfCastinfo.getMultiCastType())||Const.CASTTYPE[3].equals(selfCastinfo.getMultiCastType())) {
					String[] names = selfCastinfo.getTaskName().split(":");
					if(names.length > 1) {
						PageData pd=new PageData();
						pd.put("TaskId", names[1]);
						if(Const.CASTTYPE[1].equals(selfCastinfo.getMultiCastType())) {
							ScheTask task = schetaskservice.getTaskByTaskId(pd);
							quartzService.updateTask(names[1], task.getScheId(), false);
						}else {
							quartzService.updateTask(names[1],QuartzService.JobTermGroup, false);
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logError(e);
			}
			onCloseListener();
			try {
				Thread.sleep(100);
				if(group != null)
					group.shutdownGracefully();
				if(channel != null && channel.isOpen()) {
					channel.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				logError(e);
			}
		}
	}
	/**
	 * 发送线程关闭监听方法，当线程关闭时，对内存信息进行更新以及是否有下一个任务的判断
	 */
	public void onCloseListener(){
		//System.out.println("onclose");
		/*List<TerminalInfo> terminalInfos = GlobalInfoController.getTerinfolist();
		for(TerminalInfo tInfo:terminalInfos) {
			if(selfCastinfo.getCastTeridlist().contains(tInfo)) {
					synchronized (terminalInfos) {
						tInfo.setIscast(isopen);
						tInfo.setCastType(CastTaskInfo.types[Integer.valueOf(types.get(0))]);
					}
			}
		}*/
		GlobalInfoController.getScheduledThreadPool().schedule(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				List<TerminalInfo> terminalInfos = selfCastinfo.getCastTeridlist();
				TerminalInfo maininfo = selfCastinfo.getMainTerm();
				try {
					//之前有任务正在创建则等待300ms
					synchronized (terminalInfos) {
						if(terminalInfos != null && terminalInfos.size() > 0) {
							if(CastTaskService.isCreated)Thread.sleep(300);
							CastTaskService.isCreated = true;
							try {
								GlobalInfoController.EndTasksController(terminalInfos,selfCastinfo,false);
								//Thread.sleep(10);//停止10ms等待终端确定停止
								GlobalInfoController.StartTasks(terminalInfos,null,false);//开始组播命令批量发送
								new TimeReloady(terminalInfos);//定时检测终端入组情况
								/*if(terminalInfos.size() > 20) {//分组进行更换任务
									int i = 20;
									for(;i<terminalInfos.size();i = i+20) {
										List<TerminalInfo> tList = terminalInfos.subList(i-20, i);
										sendChangeCast(tList);
									}
									if(terminalInfos.size() > 0 && i-20 != terminalInfos.size()) {//最后收尾
										List<TerminalInfo> tList = terminalInfos.subList(i-20, terminalInfos.size());
										sendChangeCast(tList);
									}
								}else {
									sendChangeCast(terminalInfos);
								}*/
							} catch (Exception e) {
								CastTaskService.isCreated = false;
								// TODO Auto-generated catch block
								logError(e);
							}
							//Thread.sleep(terminalInfos.size()*2);
							CastTaskService.isCreated = false;
						}else if(maininfo != null) {
							GlobalInfoController.DelTheTasks(maininfo, selfCastinfo);//删除终端任务信息
						}
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					logError(e1);
				}
			}
		}, 300, TimeUnit.MILLISECONDS);
	}
	/**
	 * 
	 * @return
	 * @throws Exception
	 * TODO 获取本地IP方法，此方法为备用方法，在IP配置与本地IP没有正确匹配的情况下自动使用
	 * 时间：2019年1月13日
	 */
	@SuppressWarnings("rawtypes")
	public static InetAddress getLocalHostLANAddress() throws Exception {
		try {
			InetAddress candidateAddress = null;
			// 遍历所有的网络接口
			for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// 在所有的接口下再遍历IP
				for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
						if (inetAddr.isSiteLocalAddress()) {
							// 如果是site-local地址，就是它了
							return inetAddr;
						} else if (candidateAddress == null) {
							// site-local类型的地址未被发现，先记录候选地址
							candidateAddress = inetAddr;
						}
					}
				}
			}
			if (candidateAddress != null) {
				return candidateAddress;
			}
			// 如果没有发现 non-loopback地址.只能用最次选的方案
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			return jdkSuppliedAddress;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param mypath
	 * @return timesize 返回发包时间间隔。
	 * TODO 读取音频文件，去除头部，并动态调节包的大小以及发包时间间隔，
	 * 时间：2019年1月14日
	 */
	private boolean NewFileRead(String path) {
		synchronized (in != null? in:path) {
			try {
				String mypath;
				if(bathPath != null)mypath = bathPath+"\\"+path;
				else mypath = path;
				int bts =0;
				if(MP3AudioUtil.getAudioBitRate(mypath) != null)
					bts= Integer.parseInt(MP3AudioUtil.getAudioBitRate(mypath));
				timesize = (DataLenght*8)/bts;//length = bps/8 *1000 *t  单位为秒。timesize单位为ms
				bitsize = bts*timesize/8;
				long startByte = MP3AudioUtil.getAudioStartByte(mypath);
				FileInputStream files = new FileInputStream(mypath);
				in  = new BufferedInputStream(files,files.available());
				in.skip(startByte);
				if(fileinfo.size() > 0 ) {//初始化
					fileinfo = new ArrayList<String>();
				}
				fileinfo.add(0,path);//获取路径
				fileinfo.add(1,Integer.toString(bts/8));//获取每毫秒播放的字节数
				fileinfo.add(2,Integer.toString(MP3AudioUtil.getTrackLength(mypath)));//获取音频总时长
				fileinfo.add(3,"0");//获取音频播放位置
				fileinfo.add(4,"0");//获取音频播放时间Date
				fileinfo.add(5,"0");//暂停标志位
				fileinfo.add(6,"0");//暂停存储位
				return true;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				logError(e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logError(e);
			}
		}
		return false;
	}
	/**
	 * 
	 * @param tpye为类型，true 为切换音频,false为内部音频线程调用(step 为true则为顺序播放，为false则为随机播放)
	 * @param step true为下一曲,false为上一曲
	 * @return true 切换成功，false，切换失败
	 * TODO 控制调节音频接口，进行上一曲，下一曲操作，或者为内部发送音频线程提供切换音频的功能
	 * 时间：2019年1月14日
	 */
	public boolean moveFileCast(boolean tpye,boolean step) {
		if(tpye) {
			return MoveNext_MovePrevious(step);
		}else {
			return NewCastFile();
		}
	}
	/**
	 * @param comd:控制命令的类型  0暂停  1运行  2调整播放位置
	 * @param time:调整音频的时间节点
	 * @return 调节成功或失败信息
	 * TODO	控制调节播放音频位置
	 * 时间：2019年1月16日
	 */
	public boolean castPosition(int commd, double time) {
		if(fileinfo.size() > 4 && (Const.CASTTYPE[0].equals(selfCastinfo.getMultiCastType())||Const.CASTTYPE[1].equals(selfCastinfo.getMultiCastType()))) {
			int musictime = Integer.parseInt(fileinfo.get(2));
			double musicsite = Double.parseDouble(fileinfo.get(3));
			Long systime = Long.parseLong(fileinfo.get(4));
			//暂停与播放控制
				sendFileData.command(commd, (int)time);
				if(commd == 0) {//暂停
					fileinfo.set(5, Long.toString(System.currentTimeMillis()));
				}else if(commd == 1) {
					if(!fileinfo.get(5).equals("0")) {
						long stoptime = Long.parseLong(fileinfo.get(6))+System.currentTimeMillis()-Long.parseLong(fileinfo.get(5));
						fileinfo.set(6, Long.toString(stoptime));//单位毫秒
						fileinfo.set(5, "0");//清零标志位
					}
				}else if(commd == 2 &&time >= 0 && time <= musictime) {//是否为正确的调节值
				if(time*1000 > System.currentTimeMillis()-(systime-musicsite*1000)) {//为向后调节音频
					long foot = ((int)(time*1000)-(System.currentTimeMillis()-(systime-(int)(musicsite*1000))))*Integer.parseInt(fileinfo.get(1));
					sendFileData.command(commd, foot);
					selfCastinfo.setIsStop(false);
					refreshTime(time);//更新时间信息
					return true;
				}else {//为向前调节音频，需重置线程
					try {
						sendFileData.cancel();
						casttimer.purge();
						NewFileRead(fileinfo.get(0));//重新读取文件
						sendFileData = new SendFileData(this);
						casttimer.scheduleAtFixedRate(sendFileData,0,timesize);//动态延时调整
						refreshTime(0);//更新时间信息
						if(time < 2) {
							return true;
						}
						return castPosition(commd,time);//重新调用自身
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
	/**
	 * 
	 * @param step
	 * TODO 进行广播线程内部自动切换音频或者停止
	 * 时间：2019年1月14日
	 */
	private boolean NewCastFile() {
		try {
			if(selfCastinfo.getFilelist().size() > 0) {
				if(selfCastinfo.getTasktype()< 2) {//顺序播放
					if(filelistin < selfCastinfo.getFilelist().size()){//音频发完，查看是否还有选中的音频。
						
					}else if(selfCastinfo.getTasktype() == 1){//是否循环播放
						filelistin = 0;
					}else {
						close();//文件放完关闭广播
						return false;
					}
				}else if(selfCastinfo.getTasktype() == 2){//随机播放
					Random r = new Random();
					short x = (short) r.nextInt(selfCastinfo.getFilelist().size());
					filelistin = x;
				}else if(selfCastinfo.getTasktype() == 3){//单曲循环
					if(filelistin > 0) {
						filelistin--;
					}
				}
				if(NewFileRead(selfCastinfo.getFilelist().get(filelistin++))) {
					sendFileData.cancel();
					casttimer.purge();
					sendFileData = new SendFileData(this);
					casttimer.scheduleAtFixedRate(sendFileData,0,timesize);//动态延时调整
					refreshTime(0);//更新时间信息
					return true;
				}else {//排除掉错误文件
					selfCastinfo.getFilelist().remove(--filelistin);
					NewCastFile();
					return false;
				}
			}else{
				close();
				return false;
			}
		} catch (NumberFormatException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 
	 * @param step
	 * @return 
	 * TODO 切换音频,true为下一曲,false为上一曲
	 * 时间：2019年1月14日
	 */
	private boolean MoveNext_MovePrevious(boolean step) {
		try {
			if(selfCastinfo.getFilelist().size() > 0) {
			if(selfCastinfo.getTasktype()< 2 ) {//顺序播放
					if(step) {//下一曲
						if(filelistin < selfCastinfo.getFilelist().size()) {
							
						}else{
							filelistin = 0;
						}
					}else {//上一曲
						filelistin -= 2;
						if(filelistin < 0) {
							filelistin = (short) (selfCastinfo.getFilelist().size()-1);
						}else if(filelistin < selfCastinfo.getFilelist().size()) {
							
						}else {
							return false;
						}
					}
				}else {//随机播放
					Random r = new Random();
					short x = (short) r.nextInt(selfCastinfo.getFilelist().size());
					filelistin = x;
				}
				sendFileData.cancel();
				casttimer.purge();
				if(NewFileRead(selfCastinfo.getFilelist().get(filelistin++))) {
					sendFileData = new SendFileData(this);
					casttimer.scheduleAtFixedRate(sendFileData,0,timesize);//动态延时调整
					if(selfCastinfo.getIsStop())
						selfCastinfo.setIsStop(true);
					else
						selfCastinfo.setIsStop(false);
					refreshTime(0);//更新时间信息
				}else {//排除掉错误文件
					selfCastinfo.getFilelist().remove(--filelistin);
					MoveNext_MovePrevious(step);
				}
				return true;
			}else {
				return false;
			}
		} catch (NumberFormatException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 
	 * @param time 文件播放的位置，从什么时间开始放的。
	 * TODO 更新文件播放时间以及其播放位置
	 * 时间：2019年1月16日
	 */
	public void refreshTime(double time){
		fileinfo.set(3, Double.toString(time));
		fileinfo.set(4, Long.toString(System.currentTimeMillis()));//获取系统时间，单位为毫秒，Long型
		fileinfo.set(5, "0");
		fileinfo.set(6, "0");
	}

	public Boolean getIsopen() {
		return isopen;
	}
	/**
	 * @return the isture
	 */
	public Boolean getIsture() {
		return isture;
	}
	/**
	 * @return the selfCastinfo
	 */
	public CastTaskInfo getSelfCastinfo() {
		return selfCastinfo;
	}
	/**
	 * @param selfCastinfo the selfCastinfo to set
	 */
	public void setSelfCastinfo(CastTaskInfo selfCastinfo) {
		this.selfCastinfo = selfCastinfo;
	}
	/**
	 * @return the channel
	 */
	public Channel getChannel() {
		return channel;
	}
	/**
	 * @return the lastingSeconds
	 */
	public int getBzzzik() {
		return bzzzik;
	}
	/**
	 * @return the filelistin
	 */
	public short getFilelistin() {
		return filelistin;
	}
	/**
	 * @param filelistin the filelistin to set
	 */
	public void setFilelistin(short filelistin) {
		this.filelistin = filelistin;
	}
	
	/**
	 * @return the timesize
	 */
	public int getTimesize() {
		return timesize;
	}
	/**
	 * @param timesize the timesize to set
	 */
	public void setTimesize(int timesize) {
		this.timesize = timesize;
	}
	/**
	 * @param bzzzik the bzzzik to set
	 */
	public synchronized void setBzzzik(int bzzzik) {
		this.bzzzik = bzzzik;
	}
	
	/**
	 * @return the groupAddress
	 */
	public InetSocketAddress getGroupAddress() {
		return groupAddress;
	}
	/**
	 * @return the in
	 */
	public BufferedInputStream getIn() {
		return in;
	}
	/**
	 * @return the casttimer
	 */
	public Timer getCasttimer() {
		return casttimer;
	}
	/**
	 * @return the bitsize
	 */
	public int getBitsize() {
		return bitsize;
	}
	/**
	 * @return the fileinfo
	 */
	public List<String> getFileinfo() {
		return fileinfo;
	}
	/**
	 * @return the bathPath
	 */
	public String getBathPath() {
		return bathPath;
	}
	/**
	 * @param bathPath the bathPath to set
	 */
	public void setBathPath(String bathPath) {
		this.bathPath = bathPath;
	}
	/*发送换组命令*/
	public void sendChangeCast(List<TerminalInfo> list) {
		GlobalInfoController.putThreadintoStandbyPool(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					GlobalInfoController.EndTasksController(list,selfCastinfo,false);
					Thread.sleep(10);//停止10ms等待终端确定停止
					GlobalInfoController.StartTasks(list,null,false);//开始组播命令批量发送
					new TimeReloady(list);//定时检测终端入组情况
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
}

class MulticastHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, DatagramPacket arg1) throws Exception {
		// TODO Auto-generated method stub

	}
}
