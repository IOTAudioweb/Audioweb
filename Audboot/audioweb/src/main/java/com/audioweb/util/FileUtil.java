package com.audioweb.util;

import com.audioweb.entity.Page;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;


/**
 * 文件处理
 */
public class FileUtil {
    public static void main(String[] args) {
        String path = "";
        if (!findDir(path)) {
            createDir(path);
        }
    }

    /**
     * 获取文件大小 返回 KB 保留3位小数  没有文件时返回0
     *
     * @param filepath 文件完整路径，包括文件名
     * @return
     */
    public static Double getFilesize(String filepath) {
        File backupath = new File(filepath);
        return Double.valueOf(backupath.length()) / 1000.000;
    }

    /**
     * 创建目录
     *
     * @param destDirName目标目录名
     * @return
     */
    public static Boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (!dir.exists()) {                //判断文件整个路径是否存在
            System.out.println("创建路径");
            return dir.mkdirs();        //不存在就全部创建
        }
        return false;
    }

    /**
     * 判断目录是否存在
     *
     * @param destDirName目标目录名
     * @return
     */
    public static Boolean findDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists() && dir.isDirectory()) {                //判断是否为目录
            System.out.println("存在路径");
            return true;        //存在
        }
        return false;
    }

    /**
     * 创建文件
     *
     * @param path
     */
    public static File createFile(String path) {

        File file = new File(path);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 读取路径下所有文件
     *
     * @param path    根路径
     * @param pathrep 补充路径
     * @param type    读取方式 0 无视目录，1设为根目录，2设为子目录
     * @return
     */
    public static ArrayList<String> getFiles(String path, String pathrep, int type) {
        ArrayList<String> files = new ArrayList<String>();
        File file = null;
        String pathreps;
        if (pathrep != null) {
            pathreps = pathrep.replace("/", "\\");
            file = new File(path + pathreps);
        } else
            file = new File(path);
        if (file.isDirectory()) {
            File[] tempList = file.listFiles();
            for (int i = 0; i < tempList.length; i++) {
                if (tempList[i].isFile()) {
                    files.add(tempList[i].getName());
                }
                if (tempList[i].isDirectory()) {//是否为目录
                    if (type == 1) {
                        files.add(0, "/" + tempList[i].getName());
                    } else if (type == 2) {
                        files.add(0, pathrep + "/" + tempList[i].getName());
                    }
                }
            }
            return files;
        } else {
            return null;
        }
    }

    /**
     * 读取路径下文件(分页)
     *
     * @param path
     * @return
     */
    public static ArrayList<String> getFileslist(String path, Page page) {
        ArrayList<String> files = new ArrayList<String>();
        File file = new File(path);
        if (file.isDirectory()) {
            File[] tempList = getCurFilesList(path);
            String showCount = (null == page.getPd().get("page.showCount") || "".equals(page.getPd().get("page.showCount").toString())) ? "" : page.getPd().get("page.showCount").toString();
            String currentPage = (null == page.getPd().get("page.currentPage") || "".equals(page.getPd().get("page.currentPage").toString())) ? "" : page.getPd().get("page.currentPage").toString();
            page.setTotalResult(tempList.length);
            if ("".equals(showCount) || "".equals(currentPage)) {
                page.getPd().put("currentPage", page.getCurrentPage());
                page.getPd().put("showCount", page.getShowCount());
            } else {
                page.getPd().put("currentPage", Integer.parseInt(currentPage));
                page.getPd().put("showCount", Integer.parseInt(showCount));
                page.setCurrentPage(Integer.parseInt(currentPage));
                page.setShowCount(Integer.parseInt(showCount));
            }
            for (int i = (page.getCurrentPage() - 1) * page.getShowCount(); i < page.getCurrentPage() * page.getShowCount() && i < tempList.length; i++) {
                if (tempList[i].isFile()) {
                    files.add(tempList[i].getName());
                }
            }
            return files;
        } else {
            return null;
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static Boolean copyFile(String oldPath, String newPath) {
        try {
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                File newfile = createFile(newPath);
                FileUtils.copyInputStreamToFile(inStream, newfile);
            }
            return true;
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 删除文件
     *
     * @param filePathAndName String 文件路径及名称 如c:/fqf.txt
     * @param fileContent     String
     * @return boolean
     */
    public static boolean delFile(String filePathAndName) {
        try {
            String filePath = filePathAndName;
            filePath = filePath.toString();
            File myDelFile = new File(filePath);
            if (myDelFile.exists()) {
                myDelFile.delete();
                return true;
            }
        } catch (Exception e) {
            System.out.println("删除文件操作出错");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除文件或文件夹
     *
     * @param filePath
     */
    public static boolean delFilePath(String filePath) {
        File dir = new File(filePath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = delFilePath(filePath + "/" + children[i]);
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * 读取到字节数组0
     *
     * @param filePath //路径
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static byte[] getContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        fi.close();
        return buffer;
    }

    /**
     * 读取到字节数组1
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(String filePath) throws IOException {

        File f = new File(filePath);
        if (!f.exists()) {
            throw new FileNotFoundException(filePath);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bos.close();
        }
    }

    /**
     * 读取到字节数组2
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray2(String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.exists()) {
            throw new FileNotFoundException(filePath);
        }
        FileChannel channel = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(f);
            channel = fs.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            while ((channel.read(byteBuffer)) > 0) {
                // do nothing
                // System.out.println("reading");
            }
            return byteBuffer.array();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mapped File way MappedByteBuffer 可以在处理大文件时，提升性能
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray3(String filePath) throws IOException {

        FileChannel fc = null;
        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile(filePath, "r");
            fc = rf.getChannel();
            MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, 0,
                    fc.size()).load();
            //System.out.println(byteBuffer.isLoaded());
            byte[] result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                // System.out.println("remain");
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                rf.close();
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取路径下所有文件，不包括文件夹
     *
     * @param filePath
     * @return listFiles
     * @throws
     */
    public static File[] getCurFilesList(String filePath) {
        File path = new File(filePath);
        File[] listFiles = path.listFiles(new java.io.FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile())
                    return true;
                else
                    return false;
            }
        });

        return listFiles;
    }
}