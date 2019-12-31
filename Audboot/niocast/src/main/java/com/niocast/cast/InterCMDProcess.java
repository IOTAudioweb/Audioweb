package com.niocast.cast;

import com.audioweb.entity.Terminals;
import com.audioweb.util.Const;
import com.audioweb.util.Tools;
import com.niocast.entity.TerminalInfo;
import com.niocast.util.Convert;
import com.niocast.util.GlobalInfoController;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 终端服务器交互指令解析与打包
 * @author HTT
 */
public class InterCMDProcess {
	private static final String UTF8 = "UTF8";
	private static final String GB2312 = "GB2312";
	private static final int netHeartRecPort= Integer.parseInt(Tools.GetValueByKey(Const.CONFIG, "netheartPort"));//心跳检测接收端口默认6970
	public static final String CMDTYPE_TERCONTROL = "3";//终端控制包
	public static final String CMDTYPE_SERVERRETURN = "7";//服务器响应包
	public static final String CMDTYPE_AUDIODATA = "b";//服务器发送的音频数据包 0x0b  单播、组播、广播
	public static final String CMDTYPE_XHHTDJ = "AA";//寻呼话筒对讲
	public static final String CMD_PAD = "aa";//传输填充命令
	public static final String CMD_NONE = "0";//无效填充命令
	public static final String CMD_LOGIN = "1";//登录
	public static final String CMD_NETHEART = "3";//终端心跳包
	public static final String CMD_VOLSET = "5";//网络调音
	public static final String IP_REQUEST = "4";//回复线程端口
	public static final String CMD_PLAY_FILES = "6";  //开始播放(填充位)
	public static final String CMD_STOPVOD = "7";//停止点播
	public static final String CMD_TERMINAL = "8";//终端馈送（终端采播）
	public static final String CMD_VODFILELIST = "12";//点播音频列表
	public static final String CMD_VODFILECAST = "13";//点播控制命令
	public static final String CMD_VODFILEPAUSE = "26";//点播暂停
	public static final String CMD_FILECAST = "27";//文件组播
	public static final String CMD_TIMINGCAST = "40";//定时组播
	public static final String CMD_PIC_SEND = "41";//声卡采播
	public static final String REQUEST_RESTART = "89";//终端重启
	public static final String CMD_CMICCAST = "90";//寻呼话筒点播列表
	public static final String CMD_CMICEnable = "95";//寻呼话筒点播使能
	public static final String CMD_CMICELIST = "96";//寻呼话筒进入广播下级列表
	public static final String CMD_CMICVOL = "93";//寻呼话筒点播回复
	/*******************解析接收到的数据*************************/
	/**
	 * 从收到的数据包解析登录终端编号
	 * @param content
	 * @return terid
	 */
	public static String getTeridFromLogin(byte[] content){
		byte[] ids = new byte[4];
		System.arraycopy(content, 6, ids, 0, ids.length);
		String terid = "";
		try {
			terid = new String(ids,UTF8);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return terid;
	}
	
	/*******************编码需要发送或者返回到终端的数据***************************/
	/**
	 * 终端登录响应返回数据
	 * @return 需要发送的byte[]
	 */
	public static byte[] returnLoginBytes(){
		ByteBuffer encoded = ByteBuffer.allocate(50);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_SERVERRETURN));
		encoded.put((byte)Integer.parseInt(CMD_LOGIN));
		//for(int i=2;i<6;i++){
			encoded.put((byte)18);
			encoded.put((byte)0);
			encoded.put((byte)1);
			encoded.put((byte)0);
		//}
		//以下从第7位开始
		/*Calendar c = Calendar.getInstance();
		c.setTime(new Date());
	    c.add(Calendar.YEAR, -1);
	    Date y = c.getTime();*/
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd  HHmmss");
		String dateString = netHeartRecPort+formatter.format(new Date());
		return sendStringToBytes(encoded,dateString,null);
	}
	/**
	 * 终端心跳包响应返回数据
	 * @return 需要发送的byte[]
	 */
	public static byte[] returnNetHeart(){
		ByteBuffer encoded = ByteBuffer.allocate(50);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_SERVERRETURN));
		encoded.put((byte)Integer.parseInt(CMD_NETHEART));
		for(int i=2;i<6;i++){
			encoded.put((byte)0);
		}
//		encoded.put((byte)1);//第7位
		return sendStringToBytes(encoded,"1",null);
	}
	/**
	 * 终端点播初始化列表
	 * @return 需要发送的byte[]
	 */
	public static byte[] vodFileList(String path){
		byte[] imot = TerminalUnicast.getFilesName(path);
		ByteBuffer encoded = ByteBuffer.allocate(imot.length+7);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_SERVERRETURN));
		if(path == null) {
			encoded.put((byte)Integer.parseInt(CMD_VODFILELIST));
		}
		else{
			encoded.put((byte)Integer.parseInt(CMD_VODFILECAST));
		}
		byte[] length = Convert.hexStringToBytes(Integer.toHexString(imot.length));//音频包长度
		if(length.length >1) {
			encoded.put(length[1]);
			encoded.put(length[0]);
		}else {
			encoded.put(length[0]);
			encoded.put((byte)0);
		}
		encoded.put((byte)1);
		encoded.put((byte)0);
//		encoded.put((byte)1);//第7位
		return sendStringToBytes(encoded,null,imot);
	}
	/**
	 * 寻呼话筒终端点播初始化列表
	 * @return 需要发送的byte[]
	 */
	public static byte[] vodDomainList(TerminalInfo tInfo,String domain){
		byte[] imot = TerminalUnicast.Unicastdomain(tInfo,domain);
		ByteBuffer encoded;
		if(imot != null)
			encoded = ByteBuffer.allocate(imot.length+7);
		else
			encoded = ByteBuffer.allocate(7);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_SERVERRETURN));
		if(domain == null)
			encoded.put((byte)Integer.parseInt(CMD_CMICCAST));
		else
			encoded.put((byte)Integer.parseInt(CMD_CMICELIST));
		byte[] length;
		if(imot != null)
			length = Convert.hexStringToBytes(Integer.toHexString(imot.length));//音频包长度
		else {
			length = Convert.hexStringToBytes("0");
		}
		if(length.length >1) {
			encoded.put(length[1]);
			encoded.put(length[0]);
		}else {
			encoded.put(length[0]);
			encoded.put((byte)0);
		}
		encoded.put((byte)0);
		encoded.put((byte)20);
//		encoded.put((byte)1);//第7位
		return sendStringToBytes(encoded,null,imot);
	}
	/**
	 * 寻呼话筒终端点播使能
	 * @return 需要发送的byte[]
	 */
	public static byte[] getEnable(int num){
		ByteBuffer encoded = ByteBuffer.allocate(7);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_TERCONTROL));
		encoded.put((byte)Integer.parseInt(CMD_CMICVOL));
		//byte[] length = Convert.hexStringToBytes(Integer.toHexString(7));
		encoded.put((byte)7);//音频包长度
		encoded.put((byte)0);
		encoded.put((byte)1);
		encoded.put((byte)0);
		encoded.put((byte)num);
//		encoded.put((byte)1);//第7位
		return sendStringToBytes(encoded,null,null);
	}
	/**
	 * 寻呼话筒任务创建回复
	 * @return 需要发送的byte[]
	 */
	public static byte[] reply(String groupip,String groupPort){
		ByteBuffer encoded = ByteBuffer.allocate(50);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_SERVERRETURN));
		encoded.put((byte)Integer.parseInt(CMD_CMICEnable));
		for(int i=0;i<4;i++){
			encoded.put((byte)0);
		}
		String[] ips = groupip.split("\\.");
		byte[] ip = new byte[4];
		if(ips.length>3){
			ip[0] = (byte)Integer.parseInt(ips[0]);
			ip[1] = (byte)Integer.parseInt(ips[1]);
			ip[2] = (byte)Integer.parseInt(ips[2]);
			ip[3] = (byte)Integer.parseInt(ips[3]);
		}
		encoded.put(ip);
//		encoded.put((byte)1);//第7位
		return sendStringToBytes(encoded,groupPort,null);
	}
	/**
	 * 终端点播控制管理
	 * @return 需要发送的byte[]
	 */
	public static byte[] vodFileCast(int type,byte[] imot){
		ByteBuffer encoded = ByteBuffer.allocate(50);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_SERVERRETURN));
		if(type == 13) {//点播开始
			encoded.put((byte)Integer.parseInt(CMD_VODFILECAST));
		}else if(type == 26){//点播暂停
			encoded.put((byte)Integer.parseInt(CMD_VODFILEPAUSE));
		}/*else if(type == 27){//点播停止
			encoded.put((byte)Integer.parseInt(CMD_VODFILEPAUSE));
			imot = "0".getBytes();
		}*/else if(type == 7){//点播停止
			if(new String(imot).equals("0")) {
				encoded.put((byte)Integer.parseInt(CMD_STOPVOD));
			}else {
				encoded.put(0, (byte)Integer.parseInt(CMDTYPE_TERCONTROL));
				encoded.put((byte)Integer.parseInt(CMD_STOPVOD));
			}
		}
		byte[] length = Convert.hexStringToBytes(Integer.toHexString(imot.length));//音频包长度
		encoded.put(length[0]);
		encoded.put((byte)0);
		encoded.put((byte)1);
		encoded.put((byte)0);
//		encoded.put((byte)1);//第7位
		return sendStringToBytes(encoded,null,imot);
	}

	/**
	 * 发送广播命令汇总处理->非源终端
	 * @param isstart 是开始还是结束，true开始，false结束
	 * @param multiCastIp 组播IP地址，isstart为true时必须
	 * @param targetPort 终端接收组播端口号，isstart为true时必须，必须为5位数
	 * @param vol 音量0-40
	 * @param type 命令类型以及对应指令
	 * @return 需要发送的byte[]
	 */
	public static byte[] sendCast(Boolean isstart,String multiCastIp, int targetPort,int vol,String type){
		ByteBuffer encoded = ByteBuffer.allocate(50);
		encoded.put((byte)Integer.parseInt(CMDTYPE_TERCONTROL));
		switch(type) {
		case "文件广播":
			encoded.put((byte)Integer.parseInt(CMD_FILECAST));
			break;
		case "定时广播":
			encoded.put((byte)Integer.parseInt(CMD_TIMINGCAST));
			break;
		case "实时采播":
			encoded.put((byte)Integer.parseInt(CMD_FILECAST/*CMD_PIC_SEND*/));
			break;
		case "控件广播":
			encoded.put((byte)Integer.parseInt(CMD_FILECAST));
			break;
		case "终端采播":
			encoded.put((byte)Integer.parseInt(CMD_TERMINAL));
			break;
		/*case "终端点播":
			encoded.put((byte)Integer.parseInt(CMD_FILECAST));
			break;*/
		case "寻呼话筒":
			encoded.put((byte)Integer.parseInt(CMD_TERMINAL));
			break;
		default: 
			encoded.put((byte)Integer.parseInt(CMD_FILECAST));
			break;
		}
		
		for(int i=2;i<6;i++){
			encoded.put((byte)0);
		}
		if(isstart){//开始
			String str = "1";
			/*if(types.get(0).equals("3")) {
				if(types.size()>4) {
					str = "1"+types.get(2)+types.get(3);
					types.remove(4);//移除主机标记
					return sendStringToBytes(encoded,str,null);
				}else{
					str = str+"0";
				}
			}*/
			//终端采播
			if(type.equals(Const.CASTTYPE[3])) {
				str = str+"0";//第8位为0
			}
			//寻呼话筒
			if(type.equals(Const.CASTTYPE[5])) {
				str = str+"0"+netHeartRecPort+""+targetPort;//第7位为1
			}else {
				str = str+netHeartRecPort+""+targetPort;//第7位为1
			}
			String[] ips = multiCastIp.split("\\.");
			if(ips.length>3){
				byte[] ip = new byte[6];
				ip[0] = (byte)Integer.parseInt(ips[0]);
				ip[1] = (byte)Integer.parseInt(ips[1]);
				ip[2] = (byte)Integer.parseInt(ips[2]);
				ip[3] = (byte)Integer.parseInt(ips[3]);
				if(type.equals(Const.CASTTYPE[0])||type.equals(Const.CASTTYPE[1])) {
					ip[4] = (byte)0;
					ip[5] = (byte)vol;//音量
				}
				return sendStringToBytes(encoded,str,ip);
			}else{
				return null;
			}
		}else{//结束
			String str = "0";//第7位不为1
			return sendStringToBytes(encoded,str,null);
		}
		
	}
	
	/**
	 * 发送源终端广播命令
	 * @param isstart 是开始还是结束，true开始，false结束
	 * @param multiCastIp 组播IP地址或者为终端采播的类型，isstart为true时必须
	 * @param targetPort 终端接收组播端口号，isstart为true时必须，必须为5位数
	 * @param vol 音量0-40
	 * @param types 命令类型以及对应指令
	 * @return 需要发送的byte[]
	 */
	public static byte[] sendMainTermCast(Boolean isstart,String multiCastIp, int targetPort,String type){
		ByteBuffer encoded = ByteBuffer.allocate(50);
		encoded.put((byte)Integer.parseInt(CMDTYPE_TERCONTROL));
		switch(type) {
		case "终端采播":
			encoded.put((byte)Integer.parseInt(CMD_TERMINAL));
			break;
		case "寻呼话筒":
			encoded.put((byte)Integer.parseInt(CMD_TERMINAL));
			break;
		default: 
			encoded.put((byte)Integer.parseInt(CMD_TERMINAL));
			break;
		}
		
		for(int i=2;i<6;i++){
			encoded.put((byte)0);
		}
		if(isstart){//开始
			String str = "1";
			if(type.equals(Const.CASTTYPE[3])) {//终端采播
				str += multiCastIp+targetPort;
				return sendStringToBytes(encoded,str,null);
			}
			//寻呼话筒
			if(type.equals(Const.CASTTYPE[5])) {//寻呼话筒
				str += "0"+netHeartRecPort+""+targetPort;//第7位为1
			}else {
				str += netHeartRecPort+""+targetPort;//第7位为1
			}
			String[] ips = multiCastIp.split("\\.");
			if(ips.length>3){
				byte[] ip = new byte[6];
				ip[0] = (byte)Integer.parseInt(ips[0]);
				ip[1] = (byte)Integer.parseInt(ips[1]);
				ip[2] = (byte)Integer.parseInt(ips[2]);
				ip[3] = (byte)Integer.parseInt(ips[3]);
				return sendStringToBytes(encoded,str,ip);
			}else{
				return null;
			}
		}else{//结束
			String str = "0";//第7位不为1
			return sendStringToBytes(encoded,str,null);
		}
		
	}
	/**
	 * 发送定时组播命令
	 * @param isstart 是开始还是结束，true开始，false结束
	 * @param multiCastIp 组播IP地址，isstart为true时必须
	 * @param targetPort 终端接收组播端口号，isstart为true时必须，必须为5位数
	 * @return 需要发送的byte[]
	 */
	/*public static byte[] sendTimedCast(Boolean isstart,String multiCastIp, int targetPort){
		ByteBuffer encoded = ByteBuffer.allocate(50);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_TERCONTROL));
		encoded.put((byte)Integer.parseInt(CMD_TIMINGCAST));
		for(int i=2;i<6;i++){
			encoded.put((byte)0);
		}
		if(isstart){//开始
			//以下从第8位开始
//			for(int i=7;i<11;i++){
//				encoded.put((byte)1);
//			}
			String str = "1"+netHeartRecPort+""+targetPort;//第7位为1
			String[] ips = multiCastIp.split("\\.");
			if(ips.length>3){
				byte[] ip = new byte[4];
				ip[0] = (byte)Integer.parseInt(ips[0]);
				ip[1] = (byte)Integer.parseInt(ips[1]);
				ip[2] = (byte)Integer.parseInt(ips[2]);
				ip[3] = (byte)Integer.parseInt(ips[3]);
				return sendStringToBytes(encoded,str,ip);
			}else{
				return null;
			}
		}else{//结束
			String str = "0";//第7位不为1
			return sendStringToBytes(encoded,str,null);
		}
		
	}*/
	/**
	 * 网络调终端命令 --test 
	 * @param terminals 终端信息
	 * @return
	 */
	public static byte[] sendTerReset(Terminals terminals){
		ByteBuffer encoded = ByteBuffer.allocate(40);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_TERCONTROL));
		encoded.put((byte)Integer.parseInt(IP_REQUEST));
		encoded.put((byte)26);
		for(int i=3;i<6;i++){
			encoded.put((byte)0);
		}
		try {
			String[] ips = terminals.getTIP().split("\\.");
			for(String ip:ips) {//发送设置的IP
				encoded.put((byte)Integer.parseInt(ip));
			}
			String[] subNet = GlobalInfoController.NETMASK.split("\\.");//获取子网掩码
			for(String ip:subNet) {//发送设置的IP
				encoded.put((byte)Integer.parseInt(ip));
			}
			String[] gateway = GlobalInfoController.GATEWAY.split("\\.");//获取网关
			for(String ip:gateway) {//发送设置的IP
				encoded.put((byte)Integer.parseInt(ip));
			}
			String[] serverIP = GlobalInfoController.SERVERIP.split("\\.");//获取配置服务器IP
			for(String ip:serverIP) {//发送设置的IP
				encoded.put((byte)Integer.parseInt(ip));
			}
			String Tid = terminals.getTIDString();//获取终端更改后的IP地址
			for(char id:Tid.toCharArray()) {//发送设置的IP
				encoded.put((byte)id);
			}
			String definedPort = Tools.GetValueByKey(Const.CONFIG, "definedPort");//获取终端设置端口
			for(char id:definedPort.toCharArray()) {//发送设置的IP
				encoded.put((byte)id);
			}
			encoded.flip();
			if(encoded.remaining() != 30) {
				return null;
			}
			byte[] exec  = new byte[40];
			encoded.get(exec,0,30);
			int sum = 0;
			byte y = (byte)0;
			for(int i = 6;i <26;i++) {//计算校验和 分别是累加校验与异或校验
				sum += (0xff & exec[i]);
				y ^= exec[i];
			}
			byte[] checksum = {(byte)sum,y};
			System.arraycopy(checksum, 0, exec, 30, 2);
			return exec;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 终端调音命令
	 * @param vol 音量0-40
	 * @param issave 是否保存音量
	 * @return
	 */
	public static byte[] sendVolSet(int vol,Boolean issave){
		ByteBuffer encoded = ByteBuffer.allocate(50);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_TERCONTROL));
		encoded.put((byte)Integer.parseInt(CMD_VOLSET));
		for(int i=2;i<6;i++){
			encoded.put((byte)0);
		}
		encoded.put(Convert.hexStringToBytes(Integer.toHexString(vol)));
		String str = "";
		if(issave){//保存音量
			str = "1";//第8位为1
		}else{//不保存
			str = "0";//第8位 为0
		}
		return sendStringToBytes(encoded,str,null);
		
	}
	/**
	 * 终端重启命令
	 * @return
	 */
	public static byte[] sendTerReboot(){
		ByteBuffer encoded = ByteBuffer.allocate(50);
		encoded.put(Convert.hexStringToBytes(CMDTYPE_TERCONTROL));
		encoded.put((byte)Integer.parseInt(REQUEST_RESTART));
		for(int i=2;i<6;i++){
			encoded.put((byte)0);
		}
		String str = "1";
		return sendStringToBytes(encoded,str,null);
		
	}
	/**
	 * 将要发送的音频数据包加上头标志.点播
	 * @param audiodata
	 * @return
	 */
	public static byte[] sendAudioDataPackt(byte[] audiodata){
//		byte[] lastdata = new byte[audiodata.length+1];
//		lastdata[0] = Convert.hexStringToBytes(CMDTYPE_AUDIODATA)[0];
//		System.arraycopy(audiodata, 0, lastdata, 1, audiodata.length);
		
		byte[] lastdata = new byte[audiodata.length+16];
		//Arrays.fill(lastdata,(byte)170);
		lastdata[0] = Convert.hexStringToBytes(CMDTYPE_AUDIODATA)[0];
		byte[] l = Convert.hexStringToBytes(Integer.toHexString(audiodata.length));//音频包长度
		if(l.length==1){
			lastdata[6]=(byte)0;//低位在前，高位在后
			lastdata[7]=l[0];
		}else if(l.length==2){
			lastdata[6]=(byte)l[1];//低位在前，高位在后
			lastdata[7]=l[0];
		}
		System.arraycopy(audiodata, 0, lastdata, 16, audiodata.length);
		return lastdata;
//		int len = 16+audiodata.length;
//		ByteBuffer encoded = ByteBuffer.allocate(len);
//		encoded.put(Convert.hexStringToBytes(CMDTYPE_AUDIODATA)[0]);
//		for(int i=2;i<7;i++){
//			encoded.put((byte)0);
//		}
//		byte[] l = Convert.hexStringToBytes(Integer.toHexString(audiodata.length));//音频包长度
//		if(l.length==1){
//			encoded.put((byte)0);//低位在前，高位在后
//			encoded.put(l[0]);
//		}else if(l.length==2){
//			encoded.put(l[1]);//低位在前，高位在后
//			encoded.put(l[0]);
//		}
////		encoded.put(Convert.hexStringToBytes("4403"));
//		for(int i=9;i<17;i++){
//			encoded.put((byte)0);
//		}
////		encoded.put(Convert.hexStringToBytes("fffb"));
//		encoded.put(audiodata);
//		return sendStringToBytes(encoded,"",null);
	}
	/**
	 * 将content编码放入ByteBuffer中并转化成byte[]返回
	 * @param bb
	 * @param content 没有则设置为""
	 * @param lastbytes 在ByteBuffer放入content后在放入lastbytes，没有则设置为null
	 * @return byte[]
	 */
	private static byte[] sendStringToBytes(ByteBuffer bb,String content,byte[] lastbytes){
		byte[] bs = null;
		try {
			if(content!=null && !content.equals(""))
				bb.put(content.getBytes(GB2312));
			if(lastbytes!=null)
				bb.put(lastbytes);
			bb.flip();  
			bs = new byte[bb.remaining()];
			
			bb.get(bs);
			bb.clear();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return bs;
	}
}
