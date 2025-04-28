package com.yuweix.kuafu.core.serialize;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwei
 */
public class JacksonSerializer implements Serializer {
	private static ObjectMapper mapper = new ObjectMapper();
	private static final String CLZ_NAME = "clzName";
	private static final String CONTENT = "content";

	@Override
	public <T>String serialize(T t) {
		if (t == null) {
			return null;
		}
		Map<String, Object> map = new HashMap<>();
		String clzName = t.getClass().getName();
		map.put(CLZ_NAME, clzName);
		map.put(CONTENT, t);
		String str = null;
		try {
			str = mapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return str;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T>T deserialize(String str) {
		if (str == null) {
			return null;
		}
		try {
			Map<String, Object> map = mapper.readValue(str, Map.class);
			String clzName = (String) map.get(CLZ_NAME);
			String txt = mapper.writeValueAsString(map.get(CONTENT));
			Class<?> clz = Class.forName(clzName);
			return (T) mapper.readValue(txt, clz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
