package com.niocast.minatcpservice.handle;

import com.niocast.minatcpservice.handle.impl.*;
import com.niocast.util.AesUtil;
import org.apache.mina.core.session.IoSession;

import java.security.Key;


public class SimpleCommandFactory {
	//private static Logger logger = LoggerFactory.getLogger(MinaCastThread.class);

	public DefaultCommand createCommand(IoSession session, byte[] content) {
		DefaultCommand command = null;
		if(session.getAttribute(DefaultCommand.KEY) == null) {
			command = new CreatClient(session, content);//创建AES连接密码
		}else {
			//解码
			byte[] sp = {(byte)0};
			Key key = (Key) session.getAttribute(DefaultCommand.KEY);
			byte[] res = AesUtil.decrypt(content, key);
			String recive = new String(res);
			String[] infos = recive.split(new String(sp));
			String[] commd = infos[0].split(":");
			switch (commd[0]) {
			case DefaultCommand.CreatLogin://登录
				command = new ClientLogin(session, res);
				break;
			case DefaultCommand.GetTer://终端请求获取终端列表
				command = new SendTermInfo(session, res);
				break;
			case DefaultCommand.StartCast://终端请求开启采播
				command = new StartCast(session, res);
				break;
			case DefaultCommand.Heart://心跳
				command = new ClientHreat(session, res);
				break;
			case DefaultCommand.Stop://关闭广播
				command = new StopCast(session, res);
				break;
			case DefaultCommand.VOL://修改音量通知
				command = new VolChange(session, res);
				break;
			default://默认错误处理
				command = new DefaultInfo(session, res);
				break;
			}
			//分类
		}
		return command;
	}
}
