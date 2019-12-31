package com.niocast.cast;

import com.niocast.entity.PointCastInfo;
import com.niocast.util.GlobalInfoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SendPointData extends TimerTask{
		private Timer timer;
		private int timesize;
		private int point;//位置标识
		private List<PointCastInfo> pointList = new ArrayList<>();
		private Logger logger = LoggerFactory.getLogger(SendPointData.class);
		/**
		 * 	本定时任务的初始化，给定Timer和时间间隔ms
		 * @param timer
		 * @param timesize
		 */
		public SendPointData(Timer timer,int timesize,PointCastInfo pInfo) {
			this.timer = timer;
			this.timesize =timesize;
			pointList.add(pInfo);
			if(GlobalInfoController.putPointTaskMap(timesize, this))
				timer.scheduleAtFixedRate(this, 0, timesize);
		}
		@Override
		public void run() {
			if(pointList.size() > 0) {
				synchronized (pointList) {
					for(point = pointList.size()-1;point >= 0;point--) {
						GlobalInfoController.putThreadintoPool(new Senddata(pointList.get(point)));
					}
				}
			}else {
				this.cancel();
				timer.cancel();
				GlobalInfoController.removesendPointData(timesize);
				logger.debug("timesize:"+timesize+"		timer destroyed");
			}
		}
		public List<PointCastInfo> getpointList() {
			return pointList;
		}
		public void setpointList(List<PointCastInfo> pointList) {
			this.pointList = pointList;
		}
		public int getTimesize() {
			return timesize;
		}
		public void setTimesize(int timesize) {
			this.timesize = timesize;
		}
		/**
		 * 	将此点播信息加入本定时任务
		 * @param pointCastInfo
		 */
		public void putPointCastInfo(PointCastInfo pointCastInfo) {
			synchronized (pointList) {
				pointList.add(pointCastInfo);
			}
		}
		/**
		 * 查询本定时任务中是否存在此点播信息
		 * @param taskid
		 * @return
		 */
		public boolean hasPointCastInfo(int  taskid) {
			for(PointCastInfo pInfo:pointList) {
				if(pInfo.getTaskid() == taskid)return true;
			}
			return false;
		}
		/**
		 * 	删除点播信息
		 * @param taskid 任务ID
		 * @return
		 */
		public boolean removePointCastInfo(int taskid) {
			synchronized (pointList) {
				for(PointCastInfo pInfo:pointList) {
					if(pInfo.getTaskid() == taskid) {
						pointList.remove(pInfo);
						return true;
					}
				}
			}
			return false;
		}
}

class Senddata implements Runnable{
	private PointCastInfo castInfo;
	private Logger logger = LoggerFactory.getLogger(Senddata.class);
	
	public Senddata(PointCastInfo castInfo) {
		// TODO Auto-generated constructor stub
		this.castInfo = castInfo;
	}
	@Override
	public void run() {
		if(castInfo != null) {
			byte[] data = new byte[castInfo.getBitsize()];
			try {
				if(castInfo.getIsStop() && castInfo.getCastTaskInfo().getMct().getIsopen() && castInfo.getSession().isConnected()) {
					//GlobalInfoController.SendData(InterCMDProcess.sendAudioDataPackt(data), castInfo.getSession(), 0);
				}else if((castInfo.getIn().read(data)) != -1 && castInfo.getCastTaskInfo().getMct().getIsopen() && castInfo.getSession().isConnected()) {
					GlobalInfoController.SendData(InterCMDProcess.sendAudioDataPackt(data), castInfo.getSession(), 0);
				}else if((castInfo.getIn().read(data)) == -1 ){
					close(castInfo,true);
				}else {
					close(castInfo,false);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void close(PointCastInfo castInfo,boolean isture) {//关闭点播，并根据是否完成推送做判断
		logger.debug("点播循环发送线程结束，所属任务编号："+castInfo.getTaskid());
		try {
			//发送停止命令
			if(isture) {
				GlobalInfoController.SendData(InterCMDProcess.vodFileCast(7,"1".getBytes()), castInfo.getSession(), 0);
			}else {
				GlobalInfoController.SendData(InterCMDProcess.vodFileCast(7,"0".getBytes()), castInfo.getSession(), 0);
			}
		}finally {
			try {
				GlobalInfoController.removePointTask(castInfo.getTaskid(),castInfo.getTimesize());
				if(castInfo.getCastTaskInfo().getMct() != null && castInfo.getCastTaskInfo().getMct().getIsopen())
					castInfo.getCastTaskInfo().getMct().close();
				if(castInfo.getIn()!=null) 
					castInfo.getIn().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
