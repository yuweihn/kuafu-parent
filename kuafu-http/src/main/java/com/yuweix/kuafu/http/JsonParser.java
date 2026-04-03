package com.yuweix.kuafu.http;


import java.lang.reflect.Type;


/**
 * @author yuwei
 */
public interface JsonParser {
    String toString(Object obj);
    <T>T toObject(String text, Type type);
    <T>T toObject(String text, Class<T> clz);
}
