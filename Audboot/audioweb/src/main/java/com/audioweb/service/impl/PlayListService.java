/**
 * @Title: PlayListService.java
 * @Package com.audioweb.service.impl
 * @Description: TODO(用一句话描述该文件做什么)
 * @author: Shuofang
 * @date: 2019年1月19日 下午4:33:16
 * @version V1.0
 * @Copyright: 2019
 */
package com.audioweb.service.impl;

import com.audioweb.dao.DaoSupport;
import com.audioweb.entity.Page;
import com.audioweb.entity.PlayList;
import com.audioweb.service.PlayListManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Shuofang
 *	TODO
 */
@Service("playListService")
public class PlayListService implements PlayListManager {
    @Resource(name = "daoSupport")
    private DaoSupport dao;

    /* (non-Javadoc)
     * @see com.audioweb.service.PlayListManager#getAllList(com.audioweb.entity.Page)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<PlayList> getAllList(Page page) throws Exception {
        // TODO 获取音频列表信息
        return (List<PlayList>) dao.findForList("PlayListMapper.getAlllistPage", page);
    }

    /* (non-Javadoc)
     * @see com.audioweb.service.PlayListManager#savePlayList(com.audioweb.entity.PlayList)
     */
    @Override
    public void savePlayList(PlayList playList) throws Exception {
        // TODO Auto-generated method stub
        dao.save("PlayListMapper.savePlayList", playList);
    }

    /* (non-Javadoc)
     * @see com.audioweb.service.PlayListManager#editPlayList(com.audioweb.entity.PlayList)
     */
    @Override
    public void editPlayList(PlayList playList) throws Exception {
        // TODO Auto-generated method stub
        dao.update("PlayListMapper.editPlayList", playList);
    }

    /* (non-Javadoc)
     * @see com.audioweb.service.PlayListManager#DeletePlayList(java.lang.String)
     */
    @Override
    public void DeletePlayList(String playid) throws Exception {
        // TODO Auto-generated method stub
        dao.delete("PlayListMapper.deletePlayList", playid);
    }

}
