package com.yuweix.kuafu.core.json;


import com.alibaba.fastjson2.TypeReference;


/**
 * @author yuwei
 */
public class JsonUtil {
	private static Fastjson json = new Fastjson();

	private JsonUtil(Fastjson json) {
		JsonUtil.json = json;
	}

	public static String toJSONString(Object object) {
		return json.toJSONString(object);
	}
	public static Object parse(String text) {
		return json.parse(text);
	}
	public static<T> T parseObject(String text, TypeReference<T> type) {
		return json.parseObject(text, type);
	}
	public static<T> T parseObject(String text, Class<T> clazz) {
		return json.parseObject(text, clazz);
	}

	public static void addAccept(String name) {
		json.addAccept(name);
	}
}
