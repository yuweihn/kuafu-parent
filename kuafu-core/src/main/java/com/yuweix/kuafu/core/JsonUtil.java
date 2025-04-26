package com.yuweix.kuafu.core;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;


/**
 * @author yuwei
 */
public abstract class JsonUtil {
	public static String toJSONString(Object object) {
		return JSON.toJSONString(object);
	}
	public static Object parse(String text) {
		return JSON.parse(text);
	}
	public static<T> T parseObject(String text, TypeReference<T> type) {
		return JSON.parseObject(text, type);
	}
	public static<T> T parseObject(String text, Class<T> clazz) {
		return JSON.parseObject(text, clazz);
	}
}
