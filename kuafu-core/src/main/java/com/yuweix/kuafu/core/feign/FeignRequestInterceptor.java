package com.yuweix.kuafu.core.feign;


import com.yuweix.kuafu.core.feign.annotations.FeignPre;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yuwei
 */
public class FeignRequestInterceptor implements RequestInterceptor {
	private static SoftReference<Map<Class<? extends PreHandler>, PreHandler>> PRE_HANDLER_REF;
	private static final Object preHandlerLock = new Object();


	@Override
	public void apply(RequestTemplate template) {
		Class<?> clz = template.feignTarget().type();
		FeignPre feignPre = clz.getAnnotation(FeignPre.class);
		if (feignPre == null) {
			return;
		}
		Class<? extends PreHandler> preHandlerClz = feignPre.value();
		Map<Class<? extends PreHandler>, PreHandler> map = getPreHandlerMap();
		PreHandler preHandler = map.get(preHandlerClz);
		if (preHandler == null) {
			try {
				preHandler = preHandlerClz.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
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
