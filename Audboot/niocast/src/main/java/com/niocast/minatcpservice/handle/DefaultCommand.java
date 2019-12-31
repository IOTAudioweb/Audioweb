package com.niocast.minatcpservice.handle;

import com.audioweb.service.LogManager;
import com.audioweb.service.impl.LogService;
import com.audioweb.util.SpringContextUtils;
import com.niocast.entity.QtClientInfo;
import com.niocast.util.GlobalInfoController;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;


public abstract class DefaultCommand implements Command{
	protected static final Logger logger = LoggerFactory.getLogger(DefaultCommand.class);
	public final static String UTF_8 = "UTF-8"; 
    public static final String KEY = "KEY";
    public static final String  USERID = "USERID";
    public static final String FUNCTION ="控件操作";
    protected LogManager logservice = (LogService) SpringContextUtils.getBeanByClass(LogService.class);
	protected IoSession session;
	protected byte[] content;
	protected String[] commd;
	protected QtClientInfo info;
	protected String IP;
	public DefaultCommand(IoSession session,byte[] content) {
		this.content = content;
		this.session = session;
		String recive = new String(content);
		commd = recive.split(":");
		if(session.getAttribute(USERID) != null)
			info = GlobalInfoController.getClientInfo(session.getAttribute(USERID).toString());
		InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
		IP = socketAddress.getAddress().getHostAddress();
	}
	
	protected byte[] StrToByte(String info) {
		try {
			if(info != null)
			return info.getBytes(UTF_8);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "12".getBytes();//编码报错
	}
	protected byte[] disCon() {//终端已被离线
		return "96".getBytes();//终端离线
	}
	
	

	public static final String  CreatLogin = "0";
	public static final String  LoginSuccess = "1";
	public static final String  LoginFail = "2";
	public static final String  HasLogined = "3";
	public static final String  LoginNoAccess = "4";
	public static final String  GetTer = "6";
    protected static final String  TersComplete = "6:0";
    protected static final String  TersMotion = "6:1";
    public static final String  StartCast = "8";
    protected static final String  CastCreatError = "9";
    protected static final String  EncodeError = "12";
    public static final String  VOL= "15";
    public static final String  Stop= "18";
    public static final String  StopCast = "18:1";
    protected static final String  ClientStopCast = "18:0";
    protected static final String  CastError = "19";
    public static final String  SendTop = "20";
    protected static final String  DisCon = "96";
    public static final String  Heart = "98";
    protected static final String  CommError = "99:error";
    
}
