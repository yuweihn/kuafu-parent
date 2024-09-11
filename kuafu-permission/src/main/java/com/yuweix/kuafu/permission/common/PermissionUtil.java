package com.yuweix.kuafu.permission.common;


import com.yuweix.kuafu.core.SpringContext;
import com.yuweix.kuafu.permission.dto.AdminDto;

import java.lang.reflect.Method;


/**
 * @author yuwei
 */
public class PermissionUtil {
	public static AdminDto getLoginAccount() {
		Properties properties = SpringContext.getBean(Properties.class);
		String actionClass = properties.getActionClass();
		String actionMethod = properties.getActionMethod();

		try {
			Class<?> clz = Class.forName(actionClass);
			Method method = clz.getMethod(actionMethod);
			return (AdminDto) method.invoke(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
