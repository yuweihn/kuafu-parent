package com.yuweix.kuafu.core;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author yuwei
 */
public abstract class JacksonUtil {
	private static ObjectMapper mapper = new ObjectMapper();

	public static String toJSONString(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	public static Object parse(String text) {
		try {
			return mapper.readValue(text, Object.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	public static<T> T parseObject(String text, TypeReference<T> type) {
		try {
			return mapper.readValue(text, type);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	public static<T> T parseObject(String text, Class<T> clz) {
		try {
			return mapper.readValue(text, clz);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
