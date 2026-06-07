package com.yuweix.kuafu.core;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author yuwei
 */
public abstract class JsonUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    public static class Context {
        private static final List<String> AUTO_TYPES = new CopyOnWriteArrayList<>();
        private static Filter AUTO_TYPE_FILTER;


        public static void addAccept(String autoType) {
            if (!AUTO_TYPES.contains(autoType)) {
                AUTO_TYPES.add(autoType);
            }
            AUTO_TYPE_FILTER = JSONReader.autoTypeFilter(AUTO_TYPES.toArray(new String[0]));
        }
    }

    public static String toJson(Object obj) {
        try {
            return JSON.toJSONString(obj);
        } catch (Exception ex) {
            log.error("序列化失败. Error: {}", ex.getMessage(), ex);
            return null;
        }
    }
    public static Object toObject(String text) {
        try {
            return JSON.parse(text);
        } catch (Exception ex) {
            log.error("JSON string cannot be parsed. Error: {}", ex.getMessage(), ex);
            return null;
        }
    }
    public static<T> T toObject(String text, Type type) {
        try {
            return JSON.parseObject(text, type);
        } catch (Exception ex) {
            log.error("JSON string cannot be parsed to type {}. Error: {}", type, ex.getMessage(), ex);
            return null;
        }
    }
    public static<T> T toObject(String text, TypeReference<T> type) {
        try {
            return JSON.parseObject(text, type);
        } catch (Exception ex) {
            log.error("JSON string cannot be parsed to type {}. Error: {}", type, ex.getMessage(), ex);
            return null;
        }
    }
    public static<T> T toObject(String text, Class<T> clz) {
        try {
            return JSON.parseObject(text, clz);
        } catch (Exception ex) {
            log.error("JSON string cannot be parsed to clz {}. Error: {}", clz, ex.getMessage(), ex);
            return null;
        }
    }

    public static<T> String serialize(T t) {
        if (t == null) {
            return null;
        }
        try {
            return JSON.toJSONString(t, JSONWriter.Feature.WriteClassName);
        } catch (Exception ex) {
            log.error("序列化失败. Error: {}", ex.getMessage(), ex);
            return null;
        }
    }

    public static<T> T deserialize(String str) {
        if (str == null) {
            return null;
        }
        try {
            if (Context.AUTO_TYPE_FILTER == null) {
                return JSON.parseObject(str, new TypeReference<T>() {});
            } else {
                return JSON.parseObject(str, new TypeReference<T>() {}, Context.AUTO_TYPE_FILTER);
            }
        } catch (Exception ex) {
            log.error("JSON string cannot be parsed. str: {}. Error: {}", str, ex.getMessage(), ex);
            return null;
        }
    }
}
