package com.yuweix.kuafu.core.feign;


import com.yuweix.kuafu.core.feign.annotations.PreFeign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yuwei
 */
public class FeignRequestInterceptor implements RequestInterceptor {
	private static final Logger log = LoggerFactory.getLogger(FeignRequestInterceptor.class);

	private static SoftReference<Map<Class<? extends PreHandler>, PreHandler>> PRE_HANDLER_REF;
	private static final Object preHandlerLock = new Object();


	@Override
	public void apply(RequestTemplate template) {
		Class<?> clz = template.feignTarget().type();
		PreFeign preFeign = clz.getAnnotation(PreFeign.class);
		if (preFeign == null) {
			return;
		}
		Class<? extends PreHandler> preHandlerClz = preFeign.value();
		Map<Class<? extends PreHandler>, PreHandler> map = getPreHandlerMap();
		PreHandler preHandler = map.get(preHandlerClz);
		if (preHandler == null) {
			try {
				preHandler = preHandlerClz.getDeclaredConstructor().newInstance();
			} catch (Exception ex) {
				log.error("PreHandler初始化失败: {}", ex.getMessage(), ex);
				throw new RuntimeException(ex);
			}
			map.put(preHandlerClz, preHandler);
		}
		preHandler.apply(template);
	}
	private Map<Class<? extends PreHandler>, PreHandler> getPreHandlerMap() {
		Map<Class<? extends PreHandler>, PreHandler> map = null;
		if (PRE_HANDLER_REF == null || (map = PRE_HANDLER_REF.get()) == null) {
			synchronized (preHandlerLock) {
				if (PRE_HANDLER_REF == null || (map = PRE_HANDLER_REF.get()) == null) {
					map = new ConcurrentHashMap<>();
					PRE_HANDLER_REF = new SoftReference<>(map);
				}
			}
		}
		return map;
	}
}
