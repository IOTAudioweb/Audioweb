package com.hunau.temperature.controller.environmentController;

import com.hunau.temperature.util.Tools;
import gnu.io.SerialPort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.hunau.temperature.util.KMP;
/**
 * @Author: 焦文垚
 * @单位：湖南农业大学物联网工程专业
 */
@Controller
public class tempController extends Tools{

    @RequestMapping("/temperature")
    @ResponseBody
    public static String template() throws Exception {
        // 打开串口
        SerialPort serialPort = portParameterOpen("COM5", 115200);
        // 要发送的数据
        String dataSend = "";

        int i=1;
        for(int j=0;j<1;j++) {
            // 发送数据到单片机
            byte []datByte = dataSend.getBytes();
            uartSendDatatoSerialPort(serialPort, datByte);
            System.out.println("-------------------------------------------------------");
            //System.out.println((i++) + ". 发送到串口的数据：" + dataSend);

            // 休眠300ms，等待单片机反应
            Thread.sleep(500);

            // 从单片机接收到的数据
            byte[] dat = uartReceiveDatafromSingleChipMachine(serialPort);
            if(dat != null && dat.length > 0) {
                String dataReceive = new String(dat, "GB2312");
                //String dataReceive = new String(dat, "UTF-8");
                System.out.println("当前温湿度为" + dataReceive);
                KMP kmp = new KMP();
                serialPort.close();
                //int temperature_local = kmp.kmpMatch(dataReceive,"temperature");
                //int humidity_local = kmp.kmpMatch(dataReceive,"humidity");
                return "当前温度为:"+dataReceive.substring(12,14)+"℃" + "</br>"
                      +"当前湿度为:"+dataReceive.substring(25,27)+"%";
            } else {
                serialPort.close();
                System.out.println("接收到的数据为空！");
            }
        }
        return ("未检测到温度");
    }
}
