package com.audioweb.util;

import com.audioweb.entity.MusicFile;
import com.audioweb.entity.Page;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;

import java.io.*;
import java.util.ArrayList;

public class MP3AudioUtil extends BaseStaticLogger {

    public static void getAudioHeader(String mp3Path) {
        try {
            MP3File mp3File = new MP3File(mp3Path);//封装好的类
            FileInputStream file = new FileInputStream(mp3Path);
            System.out.println("总长度：" + file.available());
            MP3AudioHeader header = mp3File.getMP3AudioHeader();
            file.skip(header.getMp3StartByte());
            System.out.println("音频长度：" + file.available());
            System.out.println("时长: " + header.getTrackLength()); //获得时长
            System.out.println("比特率: " + header.getBitRate()); //获得比特率
            System.out.println("音轨长度: " + header.getTrackLength()); //音轨长度
            System.out.println("帧数量: " + header.getNumberOfFrames()); //帧数量
            System.out.println("格式: " + header.getFormat()); //格式，例 MPEG-1
            System.out.println("声道: " + header.getChannels()); //声道
            System.out.println("采样率: " + header.getSampleRate()); //采样率
            System.out.println("MPEG: " + header.getMpegLayer()); //MPEG
            System.out.println("MP3起始字节: " + header.getMp3StartByte()); //MP3起始字节
            System.out.println("精确的音轨长度: " + header.getPreciseTrackLength()); //精确的音轨长度
            System.out.println("估计每秒的字节数：" + file.available() / header.getTrackLength());
            System.out.println("精确每秒的字节数：" + file.available() / header.getPreciseTrackLength());
            System.out.println("理论每秒的字节数：" + Integer.parseInt(header.getBitRate()) / 8 * 1000);
            file.close();
        } catch (Exception e) {
            System.out.println("没有获取到任何信息");
        }
    }

    /**
     * 获取音频比特率
     *
     * @return bitRate
     */
    public static String getAudioBitRate(String mp3Path) {
        MP3File mp3File = null;
        MP3AudioHeader header = null;
        try {
            mp3File = new MP3File(mp3Path);
            header = mp3File.getMP3AudioHeader();
        } catch (IOException | TagException | ReadOnlyFileException | CannotReadException
                | InvalidAudioFrameException e) {
            logError(e);
            return null;
        }//封装好的类
        if (header != null)
            return header.getBitRate();
        else
            return null;
    }

    /**
     * 判断是否为音频
     *
     * @return bitRate
     */
    public static boolean isMP3File(String mp3Path) {
        try {
            new MP3File(mp3Path);
            return true;
        } catch (IOException | TagException | ReadOnlyFileException | CannotReadException
                | InvalidAudioFrameException e) {
            return false;
        }//封装好的类
    }

    /**
     * 判断是否为音频
     *
     * @return bitRate
     */
    public static boolean isMP3File(File file) {
        try {
            new MP3File(file);
            return true;
        } catch (IOException | TagException | ReadOnlyFileException | CannotReadException
                | InvalidAudioFrameException e) {
            return false;
        }//封装好的类
    }

    /**
     * 获取音频时长
     *
     * @return bitRate
     */
    public static int getTrackLength(String mp3Path) {
        MP3File mp3File = null;
        MP3AudioHeader header = null;
        try {
            mp3File = new MP3File(mp3Path);
            header = mp3File.getMP3AudioHeader();
        } catch (IOException | TagException | ReadOnlyFileException | CannotReadException
                | InvalidAudioFrameException e) {
            logError(e);
            return 0;
        }//封装好的类
        return header.getTrackLength();
    }

    /**
     * 获取音频起始字节
     *
     * @return startByte
     */
    public static long getAudioStartByte(String mp3Path) {
        MP3File mp3File = null;
        MP3AudioHeader header = null;
        try {
            mp3File = new MP3File(mp3Path);
            header = mp3File.getMP3AudioHeader();
        } catch (IOException | TagException | ReadOnlyFileException | CannotReadException
                | InvalidAudioFrameException e) {
            logError(e);
            return 0;
        }//封装好的类
        return header.getMp3StartByte();
    }

    public static void main(String[] args) {
        String path = "E:\\\\test\\\\10 加州旅馆.mp3";
        getAudioHeader(path);
        try {
            BufferedInputStream fStream = new BufferedInputStream(new FileInputStream(path));
            System.out.println(fStream.available());
            long len = fStream.skip(100000);
            System.out.println(len);
            System.out.println(fStream.available());
            fStream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 读取当前路径下所有文件(不包括文件夹以内的)
     *
     * @param path 根路径 page 页数 pageCount 每页数量
     * @return Mp3File
     */
    public static ArrayList<MusicFile> getMp3Files(String path, Page page) {
        ArrayList<MusicFile> files = new ArrayList<MusicFile>();
        File file = new File(path);
        if (file.isDirectory()) {
            File[] tempList = FileUtil.getCurFilesList(path);
            page.setTotalResult(tempList.length);
            try {
                int pagesize = (int) Math.ceil(page.getTotalResult() / (double) page.getShowCount());
                page.setTotalPage(pagesize);
                if (page.getCurrentPage() > page.getTotalPage()) {
                    page.setCurrentPage(page.getTotalPage());
                }
                for (int i = (page.getCurrentPage() - 1) * page.getShowCount(); i < page.getTotalResult() && i < page.getCurrentPage() * page.getShowCount(); i++) {
                    if (tempList[i].isFile()) {
                        //files.add(tempList[i].getName());
                        if (isMP3File(tempList[i])) {
                            MP3File mp3File;
                            mp3File = new MP3File(tempList[i]);
                            MP3AudioHeader header = mp3File.getMP3AudioHeader();
                            MusicFile musicFile = new MusicFile();
                            musicFile.setFileName(tempList[i].getName());
                            musicFile.setFileSize(String.valueOf(tempList[i].length() >> 20) + "." + (int) ((((tempList[i].length() << 20) >> 30) / 1024.0) * 100) + "MB");
                            musicFile.setBitRate(header.getBitRate() + "Kbps");
                            musicFile.setChannels(header.getChannels());
                            musicFile.setSampleRate(header.getSampleRate() + "Hz");
                            musicFile.setTimeSize(header.getTrackLength() / 60 + ":" + (header.getTrackLength() % 60 >= 10 ? (header.getTrackLength() % 60) : ("0" + header.getTrackLength() % 60)));
                            files.add(musicFile);
                        }
                    }
                }
            } catch (IOException | TagException | ReadOnlyFileException | CannotReadException
                    | InvalidAudioFrameException e) {
                // TODO Auto-generated catch block
                logError(e);
            }
            return files;
        } else {
            return null;
        }
    }
}
