package com.niocast.cast;

import com.audioweb.util.Const;
import com.niocast.util.GlobalInfoController;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * @author Shuofang
 *
 */
public class SendFileData extends TimerTask{
	// private static final short DataLenght = 880;//每次数据包发送文件长度，长度与延时的计算公式（对128Kbps
	// MP3 CBR音频）16000*t = lenght;长度不能超过900。
	private Channel channel;
	private Timer timer;
	private MulticastThread multicastThread;
	InetSocketAddress groupAddress;
	private String type;// 广播类型
	private String retype;// 历史控制命令以及广播类型
	private int bitsize;// 分包大小
	private BufferedInputStream in;// 文件读取信息流
	private Logger logger = LoggerFactory.getLogger(SendFileData.class);

	/**
	 * 
	 * @param filelist
	 * @param channel
	 * @param groupAddress
	 * @param multicastThread
	 * @param timer
	 * @param lastingSeconds
	 * @param isLooping
	 * @param type
	 * @throws FileNotFoundException
	 */
	public SendFileData(MulticastThread multicastThread) throws FileNotFoundException {
		// 初始化获取文件，线程与地址
		this.in = multicastThread.getIn();
		this.bitsize = multicastThread.getBitsize();
		this.channel = multicastThread.getChannel();
		this.groupAddress = multicastThread.getGroupAddress();
		this.multicastThread = multicastThread;
		this.timer = multicastThread.getCasttimer();
		this.type = multicastThread.getSelfCastinfo().getMultiCastType();
		/*
		 * start = System.nanoTime();//纳秒
		 */ }

	/**
	 * 
	 * @param filelist
	 * @param vodthread
	 * @param groupAddress
	 * @param multicastThread
	 * @param timer
	 * @param type
	 * @throws FileNotFoundException
	 */
	/*
	 * public SendFileData(BufferedInputStream in,int bitsize,UnicastThread
	 * vodthread,InetSocketAddress groupAddress,MulticastThread multicastThread,
	 * Timer timer,int type) throws FileNotFoundException { //初始化获取文件，线程与地址 this.in
	 * = in; this.bitsize = bitsize; this.vodthread = vodthread; this.groupAddress =
	 * groupAddress; this.multicastThread = multicastThread; this.timer = timer;
	 * this.type = type; }
	 */
	@Override
	public void run() {
		byte[] data = new byte[bitsize];// length = bps/8 *1000 *t
		try {
			 if (Const.CASTTYPE[0].equals(type) || (Const.CASTTYPE[1].equals(type) && multicastThread.getBzzzik() == -1000)) {//文件广播或者无倒计时的定时广播
					if (multicastThread.getIsopen() && channel.isWritable()) {// 是否关闭
						if ((in.read(data)) != -1) {// 继续发送音频
							send(data);
						} else {
							send(data);
							GlobalInfoController.putThreadintoGroupPool(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									if(!multicastThread.moveFileCast(false, true)) {
										fileclose();
									}
								}
							});
						}
					} else {
						fileclose();
					}
			} else if (Const.CASTTYPE[1].equals(type)) {
				if (multicastThread.getBzzzik() > 0 && multicastThread.getIsopen() && channel.isWritable()) {// 时间是否用完或者关闭
					if ((in.read(data)) != -1) {// 继续发送音频
						multicastThread.setBzzzik(multicastThread.getBzzzik() - multicastThread.getTimesize());
						send(data);
					} else {
						send(data);
						GlobalInfoController.putThreadintoGroupPool(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								if(!multicastThread.moveFileCast(false, true)) {
									fileclose();
								}
							}
						});
					}
				} else {
					fileclose();// 时间用完立即关闭
				}
			}else if ("".equals(type)) {
				if (multicastThread.getIsopen() && channel.isWritable()) {
					send(data);// 发送空文件，保持终端播放状态
				}
			} else {
				fileclose();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void send(byte[] data) {
		try {
			byte[] data1 = InterCMDProcess.sendAudioDataPackt(data);
			channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data1), groupAddress)).sync();
			// start = System.nanoTime();//纳秒
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭线程
	 * 
	 */
	public void fileclose() {
		byte[] endbs = InterCMDProcess.sendCast(false, "", 0, 0, type.equals("")?retype:type);
		logger.debug("循环发送线程结束，所属任务编号：" + multicastThread.getSelfCastinfo().getTaskid());
		try {
			if (channel != null)
				channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(endbs), groupAddress)).sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (multicastThread != null && multicastThread.getIsopen())
					multicastThread.close();
				if (in != null)
					in.close();
				timer.cancel();
				if (channel != null &&channel.isOpen())
					channel.closeFuture().sync();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 指令输入，控制循环线程的暂停，音频切换选择，以及调音频播放位置
	 * 
	 * @param comd:控制命令的类型
	 *            0暂停 1运行 2调整播放位置
	 * @param temp:调整音频的比特位置
	 * @return null
	 */
	public void command(int comd, long temp) {
		switch (comd) {
		case 0:
			retype = type;
			type = "";
			break;
		case 1:
			if ("".equals(type)) {
				type = retype;
			}
			break;
		case 2:
			synchronized (in) {// 跳过中间的字节
				//long num;
				try {
					if (in.available() > temp) {// 防止因时间或数据误差导致越界
						/*num = */in.skip(temp);
						// System.out.println("需跳过"+temp+" 实际跳过"+num);
					} else {
						/*num = */in.skip(in.available() - 1);
						// System.out.println("需跳过"+temp+" 实际跳过"+num+"超过上限");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		default:
			break;
		}
	}
}