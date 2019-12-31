package com.niocast.minatcpservice.handle.impl;

import com.audioweb.entity.Users;
import com.audioweb.service.UsersManager;
import com.audioweb.service.impl.UsersService;
import com.audioweb.util.*;
import com.niocast.entity.QtClientInfo;
import com.niocast.minatcpservice.handle.DefaultCommand;
import com.niocast.util.GlobalInfoController;
import org.apache.mina.core.session.IoSession;
import org.apache.shiro.crypto.hash.SimpleHash;

import java.security.Key;

public class ClientLogin extends DefaultCommand {
	private UsersManager usersService = (UsersService) SpringContextUtils.getBeanByClass(UsersService.class);
	
	public ClientLogin(IoSession session, byte[] content) {
		super(session, content);
		// TODO Auto-generated constructor stub
	}

	@Override
	public byte[] execute() {
		String returndata = null;
		try {
			if(commd.length > 2) {
				PageData pd = new PageData();
				pd.put("password", new SimpleHash("SHA-1", commd[1], commd[2]).toString());
				pd.put("loginid", commd[1]);
				Users user = usersService.getUserByLoginAndPwd(pd);
				if(user != null) {
					QtClientInfo info = GlobalInfoController.getClientInfo(user.getUserid());
					if(info != null) {//已登录
						returndata = HasLogined;
					}else {
						if(RightsHelper.testRights(user.getMenuRights(), Tools.GetValueByKey(Const.CONFIG, "menuMid"))) {//有访问实时采播权限
							session.setAttribute(USERID,user.getUserid());
							QtClientInfo clientInfo = new QtClientInfo();
							clientInfo.setUserid(user.getUserid());
							clientInfo.setLoginid(user.getLoginid());
							clientInfo.setUsername(user.getUsername());
							clientInfo.setKey((Key)session.getAttribute(KEY));
							clientInfo.setPassword(commd[2]);
							clientInfo.setRoleId(user.getRoleId());
							clientInfo.setSession(session);
							GlobalInfoController.putClientInfo(user.getUserid(), clientInfo);
							logservice.saveLog(Const.LOGTYPE[0]+"-"+clientInfo.getUserid(), FUNCTION, "终端控件登录", IP, user.getUsername());
							returndata = LoginSuccess+":"+user.getUsername()+":"+GlobalInfoController.SERVERIP;
						}else {
							returndata = LoginNoAccess;//无登录权限
						}
					}
				}
			}
			if(returndata == null) {
				returndata = LoginFail;
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("QT终端登录报错：",e);
		}
		return StrToByte(returndata);
	}
}
