/**
 * @Title: PlayList.java
 * @Package com.audioweb.entity
 * @Description: TODO(音频播放列表信息)
 * @author: Shuofang
 * @date: 2019年1月19日 下午4:11:13
 * @version V1.0
 * @Copyright: 2019
 */
package com.audioweb.entity;

/**
 * @author Shuofang
 *	TODO
 */
public class PlayList {
    private int playid;
    private String playname;
    private String palypath;
    private String playmusic;
    private String note;

    /**
     * @return the playid
     */
    public int getPlayid() {
        return playid;
    }

    /**
     * @param playid the playid to set
     */
    public void setPlayid(int playid) {
        this.playid = playid;
    }

    /**
     * @return the playname
     */
    public String getPlayname() {
        return playname;
    }

    /**
     * @param playname the playname to set
     */
    public void setPlayname(String playname) {
        this.playname = playname;
    }

    /**
     * @return the palypath
     */
    public String getPalypath() {
        return palypath;
    }

    /**
     * @param palypath the palypath to set
     */
    public void setPalypath(String palypath) {
        this.palypath = palypath;
    }

    /**
     * @return the playmusic
     */
    public String getPlaymusic() {
        return playmusic;
    }

    /**
     * @param playmusic the playmusic to set
     */
    public void setPlaymusic(String playmusic) {
        this.playmusic = playmusic;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }


}
