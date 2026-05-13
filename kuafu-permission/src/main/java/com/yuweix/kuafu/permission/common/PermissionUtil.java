package com.yuweix.kuafu.permission.common;


import com.yuweix.kuafu.core.ActionUtil;
import com.yuweix.kuafu.core.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;


/**
 * @author yuwei
 */
public class PermissionUtil {
	private static final Logger log = LoggerFactory.getLogger(PermissionUtil.class);


	public static<K> K getLoginAccountId() {
		Properties properties = SpringContext.getBean(Properties.class);
		String actionClass = properties.getActionClass();
		String actionMethod = properties.getActionMethod();

		if (actionClass == null || "".equals(actionClass.trim())
				|| actionMethod == null || "".equals(actionMethod.trim())) {
			return ActionUtil.getLoginAccountId();
		} else {
			try {
				Class<?> clz = Class.forName(actionClass);
				Method method = clz.getMethod(actionMethod);
				return (K) method.invoke(null);
			} catch (Exception ex) {
				log.error("PermissionUtil.getLoginAccountId error: {}", ex.getMessage(), ex);
				throw new RuntimeException(ex);
			}
		}
	}
}
