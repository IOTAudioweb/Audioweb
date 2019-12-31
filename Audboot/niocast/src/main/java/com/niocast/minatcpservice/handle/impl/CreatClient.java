package com.niocast.minatcpservice.handle.impl;

import com.niocast.minatcpservice.handle.DefaultCommand;
import com.niocast.util.AesUtil;
import org.apache.mina.core.session.IoSession;

import java.security.Key;

public class CreatClient extends DefaultCommand {

	public CreatClient(IoSession session, byte[] content) {
		super(session, content);
		// TODO Auto-generated constructor stub
	}

	// TODO 创建连接后，第一次通信给终端发送KEY
	@Override
	public byte[] execute() {
		long time = System.currentTimeMillis();
    	byte[] returndata = (CreatLogin+":"+time).getBytes();
    	String keystr = "WEB"+time;//添加标志头
    	Key key = AesUtil.createKey(keystr);
    	session.setAttribute(KEY, key);//保存key
		return returndata;
	}
}
