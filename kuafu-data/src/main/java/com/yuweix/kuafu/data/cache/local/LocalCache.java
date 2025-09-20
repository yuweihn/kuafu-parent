package com.yuweix.kuafu.data.cache.local;


import java.util.List;
import java.util.Map;


/**
 * @author yuwei
 */
public interface LocalCache {
    boolean put(String key, String value);
    boolean put(Map<String, String> map);
    String get(String key);
    String getIfPresent(String key);
    boolean remove(String key);
    boolean remove(List<String> keys);
    boolean removeAll();
    long size();
    List<String> keys();
}
