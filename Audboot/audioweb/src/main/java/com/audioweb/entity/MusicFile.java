package com.audioweb.entity;

public class MusicFile {
    private String FileName;
    private String FileSize;
    private String TimeSize;
    private String BitRate;
    private String Channels;
    private String SampleRate;

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public String getFileSize() {
        return FileSize;
    }

    public void setFileSize(String fileSize) {
        FileSize = fileSize;
    }

    public String getTimeSize() {
        return TimeSize;
    }

    public void setTimeSize(String timeSize) {
        TimeSize = timeSize;
    }

    public String getBitRate() {
        return BitRate;
    }

    public void setBitRate(String bitRate) {
        BitRate = bitRate;
    }

    public String getChannels() {
        return Channels;
    }

    public void setChannels(String channels) {
        Channels = channels;
    }

    public String getSampleRate() {
        return SampleRate;
    }

    public void setSampleRate(String sampleRate) {
        SampleRate = sampleRate;
    }


}
