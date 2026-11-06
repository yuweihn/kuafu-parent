package com.yuweix.kuafu.core.feign;


import com.yuweix.kuafu.core.feign.annotations.PreFeign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yuwei
 */
public class FeignRequestInterceptor implements RequestInterceptor {
	private static final Logger log = LoggerFactory.getLogger(FeignRequestInterceptor.class);

	private static final Map<Class<? extends PreHandler>, PreHandler> PRE_HANDLER_MAP = new ConcurrentHashMap<>();
	private static final Map<Class<? extends PreHandler>, Constructor<? extends PreHandler>> CONSTRUCTOR_MAP = new ConcurrentHashMap<>();


	@Override
	public void apply(RequestTemplate template) {
		Class<?> clz = template.feignTarget().type();
		PreFeign preFeign = clz.getAnnotation(PreFeign.class);
		if (preFeign == null) {
			return;
		}
		Class<? extends PreHandler> preHandlerClz = preFeign.value();
		PreHandler preHandler = PRE_HANDLER_MAP.computeIfAbsent(preHandlerClz, this::createPreHandler);
		preHandler.apply(template);
	}

	private PreHandler createPreHandler(Class<? extends PreHandler> clz) {
		try {
			Constructor<? extends PreHandler> constructor = CONSTRUCTOR_MAP.computeIfAbsent(clz, c -> {
				try {
					return c.getDeclaredConstructor();
				} catch (NoSuchMethodException ex) {
					throw new RuntimeException("PreHandler缺少无参构造函数: " + c.getName(), ex);
				}
			});
			return constructor.newInstance();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("PreHandler初始化失败: {}", ex.getMessage(), ex);
			throw new RuntimeException("PreHandler初始化失败: " + clz.getName(), ex);
		}
	}
}
