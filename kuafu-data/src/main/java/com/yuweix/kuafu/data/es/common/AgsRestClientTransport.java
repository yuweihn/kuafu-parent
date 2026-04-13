package com.yuweix.kuafu.data.es.common;


import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.entity.ContentType;
import org.elasticsearch.client.RestClient;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


/**
 * 服务器ES版本为7.2.1，不接受 application/vnd.elasticsearch+json 这种header，但客户端(7.17.9)在访问ES的时候会带上这个header。
 * 当前类就是为了修改这个header
 * @author yuwei
 */
public class AgsRestClientTransport extends RestClientTransport {
	static {
		try {
			Field field = RestClientTransport.class.getDeclaredField("JsonContentType");
			field.setAccessible(true);

			Field modifiers = field.getClass().getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			field.set(null, ContentType.APPLICATION_JSON);

			modifiers.setInt(field, field.getModifiers() & Modifier.FINAL);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public AgsRestClientTransport(RestClient restClient, JsonpMapper mapper) {
		super(restClient, mapper);
	}
}
