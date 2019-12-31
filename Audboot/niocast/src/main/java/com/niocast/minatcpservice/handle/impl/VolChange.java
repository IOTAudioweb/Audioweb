package com.niocast.minatcpservice.handle.impl;

import com.niocast.entity.CastTaskInfo;
import com.niocast.minatcpservice.handle.DefaultCommand;
import com.niocast.util.GlobalInfoController;
import org.apache.mina.core.session.IoSession;

public class VolChange extends DefaultCommand {

	public VolChange(IoSession session, byte[] content) {
		super(session, content);
		// TODO Auto-generated constructor stub
	}

	@Override
	public byte[] execute() {
		// TODO Auto-generated method stub
		try {
			if(commd.length > 1) {
				String vol = commd[1];
				CastTaskInfo castTaskInfo = GlobalInfoController.getCastTaskInfo(info.getTaskid());
				if(castTaskInfo != null)
				{
					castTaskInfo.setVol(Integer.parseInt(vol));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("获取音量改变失败:",e);
		}
		return null;
	}

}
