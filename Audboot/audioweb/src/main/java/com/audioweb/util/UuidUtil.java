package com.audioweb.util;

import java.util.ArrayList;
import java.util.UUID;

public class UuidUtil {
    public static String get32UUID() {
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }

    public static void main(String[] args) {
//		System.out.println(get32UUID());
        //String path = "E:\\test";
        String path = "E:\test".replace("\\", ", \\\\");
        System.out.println(path);
		/*ArrayList<String> filelist = FileUtil.getFiles(path,0);
		for(String string:filelist) {
			System.out.println(string);
		}*/
        //getFilesName(path);

    }

    public static byte[] getFilesName(String path) {
        String omit = "…";
        byte[] name = new byte[34];
        ArrayList<String> filelist = FileUtil.getFiles(path, null, 1);
        byte[] bytes = new byte[filelist.size() * 34];
        try {
            byte[] bomit = omit.getBytes("GB2312");
            for (int i = 0; i < filelist.size(); i++) {
                name = filelist.get(i).getBytes("GB2312");
                if (name.length > 34) {
                    byte[] names = new byte[34];
                    System.arraycopy(name, 0, names, 0, 32);
                    System.arraycopy(bomit, 0, names, 32, 2);
                    name = names;
                }
                System.out.println(new String(name, "GB2312"));
                System.arraycopy(name, 0, bytes, 0 + i * 34, name.length);
				/*
				System.arraycopy(name,0,data3,0,data1.length);
				System.arraycopy(data2,0,data3,data1.length,data2.length);*/
            }
            System.out.println(new String(bytes, "GB2312"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }
}

