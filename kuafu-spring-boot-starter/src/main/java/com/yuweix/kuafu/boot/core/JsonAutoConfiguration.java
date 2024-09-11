package com.yuweix.kuafu.boot.core;


import com.yuweix.kuafu.core.json.Fastjson;
import com.yuweix.kuafu.core.json.Json;
import com.yuweix.kuafu.core.json.JsonUtil;
import com.yuweix.kuafu.session.SessionAttribute;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.json.enabled", matchIfMissing = true)
public class JsonAutoConfiguration {
	@ConditionalOnMissingBean(Json.class)
	@Bean
	public Json json(@Value("${kuafu.json.accept:}") String accepts) {
		Json json = new Fastjson();
		json.addAccept(SessionAttribute.class.getName());
		if (accepts != null && !"".equals(accepts.trim())) {
			String[] arr = accepts.trim().split(",");
			for (String accept: arr) {
				if (accept != null && !"".equals(accept.trim())) {
					json.addAccept(accept.trim());
				}
			}
		}
		return json;
	}

	@Bean("kuafu#jsonUtil")
	public JsonUtil jsonUtil(Json json) {
		try {
			Class<?> clz = Class.forName(JsonUtil.class.getName());
			Constructor<?> constructor = clz.getDeclaredConstructor(Json.class);
			constructor.setAccessible(true);
			return (JsonUtil) constructor.newInstance(json);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
