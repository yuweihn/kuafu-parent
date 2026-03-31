package com.yuweix.kuafu.permission.common;


import com.yuweix.kuafu.core.ActionUtil;
import com.yuweix.kuafu.core.SpringContext;
import java.lang.reflect.Method;


/**
 * @author yuwei
 */
public class PermissionUtil {
	public static<T> T getLoginAccount() {
		Properties properties = SpringContext.getBean(Properties.class);
		String actionClass = properties.getActionClass();
		String actionMethod = properties.getActionMethod();

		if (actionClass == null || "".equals(actionClass.trim())
				|| actionMethod == null || "".equals(actionMethod.trim())) {
			return ActionUtil.getLoginAccount();
		} else {
			try {
				Class<?> clz = Class.forName(actionClass);
				Method method = clz.getMethod(actionMethod);
				return (T) method.invoke(null);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
