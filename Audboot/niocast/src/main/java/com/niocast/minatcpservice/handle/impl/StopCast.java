package com.niocast.minatcpservice.handle.impl;

import com.audioweb.util.Const;
import com.niocast.minatcpservice.handle.DefaultCommand;
import com.niocast.util.GlobalInfoController;
import org.apache.mina.core.session.IoSession;

public class StopCast extends DefaultCommand{

	public StopCast(IoSession session, byte[] content) {
		super(session, content);
		// TODO Auto-generated constructor stub
	}

	@Override
	public byte[] execute() {
		try {
			String returndata = "";
			if(commd.length > 1) {
				String com = commd[1];
				if(info != null) {
					if(com.equals("0")) {
						if(commd.length > 2) {
							String taskid = commd[2];
							if(taskid != null && !taskid.equals(""))
							if(Integer.parseInt(taskid) != info.getTaskid()) {
								GlobalInfoController.stopCastTaskInList(Integer.parseInt(taskid));
							}
						}
						GlobalInfoController.stopCastTaskInList(info.getTaskid());
						logservice.saveLog(Const.LOGTYPE[1]+"-"+info.getUserid(), FUNCTION, "控件终端停止采播", IP, info.getTaskid()+"");
						info.setTaskid(0);
						returndata = ClientStopCast;
					}else {
						logservice.saveLog(Const.LOGTYPE[1]+"-"+info.getUserid(), FUNCTION, "控件终端停止采播", IP, info.getTaskid()+"");
						info.setTaskid(0);
						returndata = StopCast;
					}
				}else {
					returndata = CommError;
				}
			}
			return StrToByte(returndata);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Qt停止控件任务出错:",e);
		}
		return null;
	}
}
