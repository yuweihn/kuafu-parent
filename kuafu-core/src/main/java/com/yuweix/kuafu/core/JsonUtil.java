package com.yuweix.kuafu.core;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.lang.reflect.Type;


/**
 * @author yuwei
 */
public abstract class JsonUtil {
    public static String toString(Object obj) {
        return JSON.toJSONString(obj);
    }
    public static Object toObject(String text) {
        return JSON.parse(text);
    }
    public static<T> T toObject(String text, Type type) {
        return JSON.parseObject(text, type);
    }
    public static<T> T toObject(String text, TypeReference<T> type) {
        return JSON.parseObject(text, type);
    }
    public static<T> T toObject(String text, Class<T> clz) {
        return JSON.parseObject(text, clz);
    }
}
