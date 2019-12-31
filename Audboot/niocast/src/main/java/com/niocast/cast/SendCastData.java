package com.niocast.cast;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.TimerTask;

public class SendCastData  extends TimerTask{
	private static final short DataLenghts = 768;//2帧标准数据长度，对应48K采样率为48ms
	private Channel channel;
	private int offset = 0;
	private MulticastThread mThread;
	//private volatile byte[] Alldata = new byte[0];//数据拼接包
	//private CircleByteBuffer senddata = new CircleByteBuffer(16720);//未发送数据，一个数据包不够DataLenghts字节，则放入该循环数组与下一个数据包组合
	private List<Frame> framePool;
	private ByteBuffer unsenddata;//未发送数据，一个数据包不够DataLenghtsw字节，则放入该数组与下一个数据包组合
	//private RingBuffer<Byte> ringBuffer = new RingBuffer<>(8192);
	private InetSocketAddress target;
	//发送终端采播数据
	public SendCastData(List<Frame> framePool,Channel channel,InetSocketAddress target,MulticastThread mThread) {
		this.channel = channel;
		this.target = target;
		this.mThread = mThread;
		this.framePool = framePool;
	}
	@Override
	public void run() {
		byte[] data = new byte[DataLenghts];//每次发送的字节数
		if(channel!=null){
			try {
				if(unsenddata != null && unsenddata.hasRemaining()){//上一个数据包余留
					offset = unsenddata.remaining() > DataLenghts ? DataLenghts:unsenddata.remaining();
					unsenddata.get(data,0,offset);
				}else {
					offset = 0;
				}
				/*if(System.currentTimeMillis()%11 == 1) {
					System.out.println(framePool.size());
				}*/
				if(offset != DataLenghts) {
					if(framePool.size() > 0) {
						byte[] array;
						synchronized (framePool) {
							Frame frame = framePool.remove(0);
							array = frame.data;
						}
						ByteBuffer bb = ByteBuffer.allocate(array.length);
						bb = ByteBuffer.wrap(array);
						if(array.length >= data.length-offset)
							bb.get(data,offset,data.length-offset);
						else {
							bb.get(data,offset,array.length);
						}
						unsenddata = bb;
					}else {
						Thread.sleep(20);
						if(framePool.size() > 0) {
							byte[] array;
							synchronized (framePool) {
								Frame frame = framePool.remove(0);
								array = frame.data;
							}
							ByteBuffer bb = ByteBuffer.allocate(array.length);
							bb = ByteBuffer.wrap(array);
							if(array.length >= data.length-offset)
								bb.get(data,offset,data.length-offset);
							else {
								bb.get(data,offset,array.length);
							}
							unsenddata = bb;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
			try {
				if(channel.isWritable()){
					channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(InterCMDProcess.sendAudioDataPackt(data)),target)).sync();
				}else {
					if(mThread != null)
						mThread.close();
					this.cancel();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else {
			if(mThread != null)
				mThread.close();
			this.cancel();
		}
		/*if(test == 10) {
			System.out.println("time:"+System.currentTimeMillis());
			//System.out.println(8192 - senddata.getWriteableBytes());
			test = 0;
		}
		test++;*/
		// TODO Auto-generated method stub
		/*if(channel!=null){
			try {
				if(!ringBuffer.empty()) {
					byte[] senddata = new byte[DataLenght];
					for(int i =0;i< senddata.length;i++) {
						senddata[i] = (byte) ringBuffer.get();
					}
					if(channel.isWritable()){
						channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(InterCMDProcess.sendAudioDataPackt(senddata)),target)).sync();
					}else {
						this.cancel();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else {
			this.cancel();
		}*/
	}
	/**
	 * @return the ringBuffer
	 *//*
	public RingBuffer<Byte> getRingBuffer() {
		return ringBuffer;
	}*/
	/**
	 * @return the framePool
	 */
	/*public List<Frame> getFramePool() {
		synchronized (framePool) {
			return framePool;
		}
	}*/
	
}
