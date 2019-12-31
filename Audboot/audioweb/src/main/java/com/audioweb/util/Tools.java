package com.audioweb.util;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 说明：常用工具
 */
public class Tools {

    /**
     * 随机生成六位数验证码
     *
     * @return
     */
    public static int getRandomNum() {
        Random r = new Random();
        return r.nextInt(900000) + 100000;//(Math.random()*(999999-100000)+100000)
    }

    /**
     * 检测字符串是否不为空(null,"","null")
     *
     * @param s
     * @return 不为空则返回true，否则返回false
     */
    public static boolean notEmpty(String s) {
        return s != null && !"".equals(s) && !"null".equals(s);
    }

    /**
     * 检测字符串是否为空(null,"","null")
     *
     * @param s
     * @return 为空则返回true，不否则返回false
     */
    public static boolean isEmpty(String s) {
        return s == null || "".equals(s) || "null".equals(s);
    }

    /**
     * 字符串转换为字符串数组
     *
     * @param str        字符串
     * @param splitRegex 分隔符
     * @return
     */
    public static String[] str2StrArray(String str, String splitRegex) {
        if (isEmpty(str)) {
            return null;
        }
        return str.split(splitRegex);
    }

    /**
     * 用默认的分隔符(,)将字符串转换为字符串数组
     *
     * @param str 字符串
     * @return
     */
    public static String[] str2StrArray(String str) {
        return str2StrArray(str, ",\\s*");
    }

    /**
     * 按照yyyy-MM-dd HH:mm:ss的格式，日期转字符串
     *
     * @param date
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String date2Str(Date date) {
        return date2Str(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 按照yyyy-MM-dd HH:mm:ss的格式，字符串转日期
     *
     * @param date
     * @return
     */
    public static Date str2Date(String date) {
        if (notEmpty(date)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return new Date();
        } else {
            return null;
        }
    }

    /**
     * 按照参数format的格式，日期转字符串
     *
     * @param date
     * @param format
     * @return
     */
    public static String date2Str(Date date, String format) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        } else {
            return "";
        }
    }

    /**
     * 把时间根据时、分、秒转换为时间段
     *
     * @param StrDate
     */
    public static String getTimes(String StrDate) {
        String resultTimes = "";

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now;

        try {
            now = new Date();
            Date date = df.parse(StrDate);
            long times = now.getTime() - date.getTime();
            long day = times / (24 * 60 * 60 * 1000);
            long hour = (times / (60 * 60 * 1000) - day * 24);
            long min = ((times / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long sec = (times / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

            StringBuffer sb = new StringBuffer();
            //sb.append("发表于：");
            if (hour > 0) {
                sb.append(hour + "小时前");
            } else if (min > 0) {
                sb.append(min + "分钟前");
            } else {
                sb.append(sec + "秒前");
            }

            resultTimes = sb.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return resultTimes;
    }

    /**
     * 写txt里的单行内容
     *
     * @param filePath 文件路径
     * @param content  写入的内容
     */
    public static void writeFile(String fileP, String content) {
        String filePath = String.valueOf(Thread.currentThread().getContextClassLoader().getResource("")) + "../../";    //项目路径
        filePath = (filePath.trim() + fileP.trim()).substring(6).trim();
        if (filePath.indexOf(":") != 1) {
            filePath = File.separator + filePath;
        }
        try {
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(filePath), "utf-8");
            BufferedWriter writer = new BufferedWriter(write);
            writer.write(content);
            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 验证邮箱
     *
     * @param email
     * @return
     */
    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机号码
     *
     * @param mobiles
     * @return
     */
    public static boolean checkMobileNumber(String mobileNumber) {
        boolean flag = false;
        try {
            Pattern regex = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
            Matcher matcher = regex.matcher(mobileNumber);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 检测KEY是否正确
     *
     * @param paraname 传入参数
     * @param FKEY     接收的 KEY
     * @return 为空则返回true，不否则返回false
     */
    public static boolean checkKey(String paraname, String FKEY) {
        paraname = (null == paraname) ? "" : paraname;
        return MD5.md5(paraname + DateUtil.getDays() + ",fh,").equals(FKEY);
    }

    /**
     * 读取txt里的单行内容
     *
     * @param filePath 文件路径
     */
    public static String readTxtFile(String fileP) {
        try {

            String filePath = String.valueOf(Thread.currentThread().getContextClassLoader().getResource("")) + "../../";    //项目路径
            filePath = filePath.replaceAll("file:/", "");
            filePath = filePath.replaceAll("%20", " ");
            filePath = filePath.trim() + fileP.trim();
            if (filePath.indexOf(":") != 1) {
                filePath = File.separator + filePath;
            }
            String encoding = "utf-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {        // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);    // 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    return lineTxt;
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件,查看此路径是否正确:" + filePath);
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
        }
        return "";
    }

    /**
     * 根据Key读取Properties中Value
     *
     * @param @param fileP
     * @param @param key
     * @return String
     * @Title: GetValueByKey
     */
    public static String GetValueByKey(String fileP, String key) {
        String filePath = String.valueOf(Thread.currentThread().getContextClassLoader().getResource("")) + "../../";    //项目路径
        filePath = filePath.replaceAll("file:/", "");
        filePath = filePath.replaceAll("%20", " ");
        filePath = filePath.trim() + fileP.trim();
        if (filePath.indexOf(":") != 1) {
            filePath = File.separator + filePath;
        }
        Properties pps = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));
            pps.load(in);
            String value = pps.getProperty(key);
//	             System.out.println(key + " = " + value);
            return value;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 写入properties信息
     *
     * @param fileP          配置文件路径
     * @param parameterName  配置文件属性名
     * @param parameterValue 需要写入的配置文件的信息
     * @Title: writeProperties
     */
    public static boolean writeProperties(String fileP, String parameterName, String parameterValue) throws IOException {
        String filePath = String.valueOf(Thread.currentThread().getContextClassLoader().getResource("")) + "../../";    //项目路径
        filePath = filePath.replaceAll("file:/", "");
        filePath = filePath.replaceAll("%20", " ");
        filePath = filePath.trim() + fileP.trim();
        if (filePath.indexOf(":") != 1) {
            filePath = File.separator + filePath;
        }
        Properties prop = new Properties();
        try {
            InputStream fis = new FileInputStream(filePath);
            //从输入流中读取属性列表（键和元素对）
            prop.load(fis);
            //调用 Hashtable 的方法 put。使用 getProperty 方法提供并行性。
            //强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
            OutputStream fos = new FileOutputStream(filePath);
            prop.put(parameterName, parameterValue);
            //以适合使用 load 方法加载到 Properties 表中的格式，
            //将此 Properties 表中的属性列表（键和元素对）写入输出流
            prop.store(fos, " Update '" + parameterName + "' value");
            //setLastUpdateBalanceStat(parameterValue);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读取Properties的全部信息
     *
     * @param @param  fileP
     * @param @throws IOException
     * @return void
     * @Title: GetAllProperties
     */
    public static Map<String, String> GetAllProperties(String fileP) throws IOException {
        String filePath = String.valueOf(Thread.currentThread().getContextClassLoader().getResource("")) + "../../";    //项目路径
        filePath = filePath.replaceAll("file:/", "");
        filePath = filePath.replaceAll("%20", " ");
        filePath = filePath.trim() + fileP.trim();
        if (filePath.indexOf(":") != 1) {
            filePath = File.separator + filePath;
        }
        Properties pps = new Properties();
        InputStream in = new BufferedInputStream(new FileInputStream(filePath));
        pps.load(in);
        Enumeration en = pps.propertyNames(); //得到配置文件的名字
        Map<String, String> map = new HashMap<String, String>();
        while (en.hasMoreElements()) {
            String strKey = (String) en.nextElement();
            String strValue = pps.getProperty(strKey);
            map.put(strKey, strValue.trim());
//	             System.out.println(strKey + "=" + strValue);
        }
        return map;
    }

    /**
     * list去重复元素
     *
     * @param @param list
     * @return void
     * @Title: removeDuplicate
     */
    public static void removeDuplicate(List<Object> list) {
        LinkedHashSet<Object> set = new LinkedHashSet<Object>(list.size());
        set.addAll(list);
        list.clear();
        list.addAll(set);
    }

    /**
     * list添加自动排序
     *
     * @param @param e
     * @Title: SortedArrayList
     * @return boolen
     */
    public static class SortedArrayList<E extends Comparable<E>> extends ArrayList<E> {
        @Override
        public boolean add(E e) {
            if (size() == 0) {
                add(0, e);
                return true;
            } else {
                E value = e;
                int x = 0;
                for (x = size(); x > 0; x--) {
                    if (value.compareTo(get(x - 1)) > 0) {
                        break;
                    }
                }
                add(x, value);
                return true;
            }
        }
    }

    /**
     * @return TODO 获取全部IP地址
     * 时间：2019年1月11日
     */
    public static List<String> getLocalIPList() {
        List<String> ipList = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                        ipList.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipList;
    }
}
