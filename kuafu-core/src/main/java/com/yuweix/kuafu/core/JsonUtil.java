package com.yuweix.kuafu.core;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.filter.Filter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author yuwei
 */
public abstract class JsonUtil {
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


	public static<T> String serialize(T t) {
		if (t == null) {
			return null;
		}
		return JSON.toJSONString(t, JSONWriter.Feature.WriteClassName);
	}

	public static<T> T deserialize(String str) {
		if (str == null) {
			return null;
		}
		if (Context.AUTO_TYPE_FILTER == null) {
			return JSON.parseObject(str, new TypeReference<T>() {});
		} else {
			return JSON.parseObject(str, new TypeReference<T>() {}, Context.AUTO_TYPE_FILTER);
		}
	}
}
