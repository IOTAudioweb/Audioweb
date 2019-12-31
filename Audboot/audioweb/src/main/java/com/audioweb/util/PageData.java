package com.audioweb.util;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 说明：参数封装Map
 */
public class PageData extends HashMap implements Map {

    private static final long serialVersionUID = 1L;

    Map map = null;
    HttpServletRequest request;

    public PageData(HttpServletRequest request) {
        this.request = request;
        Map properties = request.getParameterMap();
        Map returnMap = new HashMap();
        Iterator entries = properties.entrySet().iterator();
        Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
            entry = (Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            returnMap.put(name, value);
        }
        map = returnMap;
    }

    public PageData() {
        map = new HashMap();
    }

    @Override
    public Object get(Object key) {
        Object obj = null;
        if (map.get(key) instanceof Object[]) {
            Object[] arr = (Object[]) map.get(key);
            obj = request == null ? arr : (request.getParameter((String) key) == null ? arr : arr[0]);
        } else {
            obj = map.get(key);
        }
        return obj;
    }

    public String getString(Object key) {
        return (String) get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PageData other = (PageData) obj;
        if (!map.equals(other.map)) {
            return false;
        } else if (!request.equals(other.request))
            return false;
        return true;
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return map.containsValue(value);
    }

    public Set entrySet() {
        // TODO Auto-generated method stub
        return map.entrySet();
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return map.isEmpty();
    }

    public Set keySet() {
        // TODO Auto-generated method stub
        return map.keySet();
    }

    @SuppressWarnings("unchecked")
    public void putAll(Map t) {
        // TODO Auto-generated method stub
        map.putAll(t);
    }

    public int size() {
        // TODO Auto-generated method stub
        return map.size();
    }

    public Collection values() {
        // TODO Auto-generated method stub
        return map.values();
    }

    /**
     * Map集合模糊匹配
     *
     * @param map     map集合
     * @param keyLike 模糊key
     * @return
     */
    public static List<Object> getLikeByPangData(PageData map, String keyLike) {
        List<Object> list = new ArrayList<>();
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next();
            if ((entry.getKey().toString()).indexOf(keyLike) > -1) {
                list.add(entry.getValue());
            }
        }
        return list;
    }
}
