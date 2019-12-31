/**
 * @Title: PlayListManager.java
 * @Package com.audioweb.service
 * @Description: TODO(用一句话描述该文件做什么)
 * @author: Shuofang
 * @date: 2019年1月19日 下午4:30:11
 * @version V1.0
 * @Copyright: 2019
 */
package com.audioweb.service;

import com.audioweb.entity.Page;
import com.audioweb.entity.PlayList;

import java.util.List;

/**
 * @author Shuofang
 *	TODO
 */
public interface PlayListManager {
    /**获取音频列表信息
     * @return
     * @throws Exception
     */
    public List<PlayList> getAllList(Page page) throws Exception;

    /**
     * 新建音频列表信息
     * @param playList 音频列表
     * @throws Exception
     */
    public void savePlayList(PlayList playList) throws Exception;

    /**
     * 修改音频列表信息
     * @param playList 音频列表
     * @throws Exception
     */
    public void editPlayList(PlayList playList) throws Exception;

    /**
     * 删除音频列表信息
     * @param playid 列表ID
     * @throws Exception
     */
    public void DeletePlayList(String playid) throws Exception;
}
