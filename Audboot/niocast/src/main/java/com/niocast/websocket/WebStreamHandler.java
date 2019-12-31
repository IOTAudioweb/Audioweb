package com.niocast.websocket;

import com.niocast.cast.Frame;
import com.niocast.cast.MulticastThread;
import com.niocast.entity.CastTaskInfo;
import com.niocast.minathread.MinaCastHandler;
import com.niocast.util.GlobalInfoController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebStreamHandler extends BinaryWebSocketHandler {
	private static Map<WebSocketSession, MulticastThread> streams = new ConcurrentHashMap<>();// 连接映射列表
	// private static List<WebSocketSession> streams = new
	// ArrayList<WebSocketSession>();//连接列表
	// private static List<Integer> taskids = new
	// ArrayList<Integer>();//与连接列表对应的广播任务列表
	// private static List<MulticastThread> sendThreadlist = new
	// ArrayList<MulticastThread>();//任务列表对应的发送线程列表
	private static Logger logger = LoggerFactory.getLogger(MinaCastHandler.class);

	/***
	 * WebSocket关闭连接后调用的方法
	 * 
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus arg1) throws Exception {
		MulticastThread multicastThread = streams.get(session);
		if (multicastThread != null && multicastThread.getIsopen()) {
			multicastThread.close();
		}
		streams.remove(session);
		/*
		 * int index = streams.indexOf(session); if(index>-1){//负责从信息列表中删除
		 * streams.remove(session);//当列表中还有这个session才进行处理，
		 * 经stopSocketByTaskid处理后没有session了将不会进入 if(sendThreadlist.size()>index)
		 * sendThreadlist.remove(index); if(taskids.size()>index){ int taskid =
		 * taskids.get(index); CastTaskInfo cInfo =
		 * GlobalInfoController.getCastTaskInfo(taskid); if(cInfo != null &&
		 * cInfo.getMct() != null && cInfo.getMct().getIsopen()) {
		 * cInfo.getMct().close(); } taskids.remove(index); } }
		 */
		if (session.isOpen())
			session.close();
		logger.info("正常日志：" + session.getRemoteAddress() + " 断开连接!");
	}

	/**
	 * WebSocket打开连接后调用的方法
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// streams.put(session, null);
		logger.info("正常日志：" + session.getRemoteAddress() + ": 打开连接！");
	}

	/**
	 * 接收；连接信息后调用的方法
	 */
	@Override
	protected void handleTextMessage(WebSocketSession conn, TextMessage message) {
		// TODO Auto-generated method stub
		// super.handleTextMessage(conn, message);
		System.out.println("String message: " + message.getPayload());
		// 开始：start:广播任务编号
		String[] rs = message.getPayload().toString().split(":");
		String commd = "";
		int taskid = 0;
		if (rs.length > 1) {
			commd = rs[0];
			taskid = Integer.parseInt(rs[1]);
			if (commd.equals("start")) {
				try {
					CastTaskInfo cInfo = GlobalInfoController.getCastTaskInfo(taskid);
					if (cInfo.getMct() != null)
						streams.put(conn, cInfo.getMct());
					else
						GlobalInfoController.stopCastTaskInList(taskid);
				} catch (Exception e) {
					// TODO: handle exception
				}
				/*
				 * for(int i=0;i<streams.size();i++){ if(conn==streams.get(i)){ try {
				 * taskids.add(i, taskid);
				 * sendThreadlist.add(GlobalInfoController.getCastTaskInfo(taskid).getMct()); }
				 * catch (Exception e) { // TODO: handle exception e.printStackTrace();
				 * GlobalInfoController.stopCastTaskInList(taskid); }
				 * 
				 * } }
				 */
			}
		}
	}

	/**
	 * 接收到数据时的调用方法
	 */
	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
		MulticastThread mct = streams.get(session);
		ByteBuffer databuf = message.getPayload();
		Frame frame = new Frame(databuf);
		databuf.clear();
		if (mct != null && mct.getIsopen()) {
			mct.sendRealMulticastAudioPackt(frame);
		}
	}

	/**
	 * 
	 * @param conn
	 * @param message
	 *            TODO 时间：2019年1月13日
	 */
	/*
	 * private void sendAudioDataToThread(WebSocketSession conn,BinaryMessage
	 * message) { MulticastThread mct = streams.get(conn); ByteBuffer databuf =
	 * message.getPayload(); byte[] data = new byte[databuf.remaining()];
	 * databuf.get(data); databuf.clear(); if(mct!=null&&mct.getIsopen()){
	 * //System.out.println(System.currentTimeMillis());
	 * mct.sendRealMulticastAudioPackt(data); } }
	 */
	/**
	 * 错误抛出
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable arg1) throws Exception {
		if (session.isOpen()) {
			session.close();
		}
		logger.error("出错日志: IP:" + session.getRemoteAddress() + "  " + arg1.getMessage());
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	/**
	 * 根据广播任务编号，断开websocket连接
	 * 
	 * @param taskid
	 * @throws IOException
	 */
	public static void stopSocketByTaskid(MulticastThread mct) throws IOException {
		if (streams.containsValue(mct)) {
			WebSocketSession session = getKey(streams, mct);
			streams.remove(session);
			if (session.isOpen())
				session.close();
			// logger.info( "正常日志："+session.getRemoteAddress() + " 断开连接!" );
		}

	}

	/**
	 * 停止全部广播连接
	 * 
	 * @param taskid
	 * @throws IOException
	 */
	public static void stopAllSocket() throws IOException {
		synchronized (streams) {
			for (Map.Entry<WebSocketSession, MulticastThread> session : streams.entrySet()) {
				if (session.getKey().isOpen())
					session.getKey().close();
				// logger.info( "正常日志："+session.getKey().getRemoteAddress() + " 断开连接!" );
			}
			streams.clear();
		}
	}

	/**
	 * 通过value获取map的key
	 * 
	 * @param taskid
	 * @throws IOException
	 */
	public static WebSocketSession getKey(Map<WebSocketSession, MulticastThread> map, MulticastThread value) {
		if (value != null)
			for (WebSocketSession key : map.keySet()) {
				if (map.get(key).equals(value)) {
					return key;
				}
			}
		return null;
	}
}