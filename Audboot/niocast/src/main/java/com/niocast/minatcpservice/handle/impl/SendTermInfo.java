package com.niocast.minatcpservice.handle.impl;

import com.audioweb.entity.Domains;
import com.audioweb.entity.Page;
import com.audioweb.entity.Terminals;
import com.audioweb.service.DomainsManager;
import com.audioweb.service.TerminalsManager;
import com.audioweb.service.impl.DomainsService;
import com.audioweb.service.impl.TerminalsService;
import com.audioweb.util.SpringContextUtils;
import com.niocast.minatcpservice.handle.DefaultCommand;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.List;

public class SendTermInfo extends DefaultCommand {
	private DomainsManager domainsService = (DomainsService) SpringContextUtils.getBeanByClass(DomainsService.class);
	private TerminalsManager terminalsService = (TerminalsService) SpringContextUtils.getBeanByClass(TerminalsService.class);
	private static final int DataLength = 768;
	public SendTermInfo(IoSession session, byte[] content) {
		super(session, content);
		// TODO Auto-generated constructor stub
	}

	@Override
	public byte[] execute() {
		try {
			if(commd.length >= 2) {
				if(info != null) {
					if(commd[1].equals("0")) {//第一次请求获取终端列表
						//String mul = 
						StringBuilder lastjson =  new StringBuilder();
						List<Domains> domarr = domainsService.listAllDomains("");//所有分组
						for(Domains domains:domarr){//设置jsonobject类型：0表示分组，1表示终端
							lastjson.append(domains.getDomainId());
							lastjson.append("|");
							lastjson.append(domains.getDomainName());
							lastjson.append("|");
							lastjson.append(domains.getParentDomainId());
							lastjson.append("|1,");
						}
						List<Terminals> termList = terminalsService.listAllTerm(new Page());//所有终端
						for(Terminals ter:termList){//设置jsonobject类型：0表示分组，1表示终端
							lastjson.append(ter.getTIDString());
							lastjson.append("|");
							lastjson.append(ter.getTName());
							lastjson.append("|");
							lastjson.append(ter.getDomainId());
							lastjson.append("|0,");
						}
						List<String> terinfo = chineseSplitFunction(lastjson.toString());
						info.setTerinfos(terinfo);
						return StrToByte(sendinfo(terinfo));
					}else if(commd[1].equals("1")) {//请求获取后续数据
						return StrToByte(sendinfo(info.getTerinfos()));
					}
				}else {
					return disCon();
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.error("终端获取分组出错：",e);
		}
		return null;
	}
	private String sendinfo(List<String> terinfo) {
		String data = "";
		if(terinfo.size() <= 1) {
			data += TersComplete+":";
			if(terinfo.size() == 1) {
				data +=terinfo.remove(0);
			}
		}else {
			data +=TersMotion+":";
			data +=terinfo.remove(0);
		}
		return data;
	}
	//字符串按照指定字节长度划分开，做分包处理
	private static List<String> chineseSplitFunction(String src){  
        try {  
            if(src == null){  
                return null;  
            }
            List<String> splitList = new ArrayList<String>();  
            int startIndex = 0;    //字符串截取起始位置  
            int endIndex = DataLength > src.length() ? src.length() : DataLength;  //字符串截取结束位置   
            while(startIndex < src.length()){  
                String subString = src.substring(startIndex,endIndex);  
                //截取的字符串的字节长度大于需要截取的长度时，说明包含中文字符  
                //在GBK编码中，一个中文字符占2个字节，UTF-8编码格式，一个中文字符占3个字节。  
                while (subString.getBytes(UTF_8).length > DataLength) {  
                    --endIndex;  
                    subString = src.substring(startIndex,endIndex);  
                }  
                splitList.add(src.substring(startIndex,endIndex));  
                startIndex = endIndex;  
                //判断结束位置时要与字符串长度比较(src.length())，之前与字符串的bytes长度比较了，导致越界异常。  
                endIndex = (startIndex + DataLength) > src.length() ?   
                        src.length()  : startIndex+DataLength ;  
                  
            }  
            return splitList;  
              
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null;  
    }  
}
