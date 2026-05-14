package com.yuweix.kuafu.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;


/**
 * @author yuwei
 */
public abstract class BeanUtil extends StrUtil {
	private static final Logger log = LoggerFactory.getLogger(BeanUtil.class);


	/**
	 * 阿尔法排序后的键值对。
	 * appkey=miutest[conn]force=true[conn]order_id=YC1603290001101T[conn]tp_customer_phone=17717601007
	 */
	public static String getAlphaString(Map<String, ? extends Object> map, String conn) {
		return getAlphaString(map, conn, null);
	}

	public static String getAlphaString(Map<String, ? extends Object> map, String conn, String charset) {
		Assert.notNull(conn, "[conn] is required.");
		if (map == null || map.isEmpty()) {
			return "";
		}

		List<String> list = new ArrayList<>();
		for (Map.Entry<String, ?> entry: map.entrySet()) {
			String k = entry.getKey();
			Object v = entry.getValue();
			if (v == null) {
				v = "";
			} else if (charset != null) {
				try {
					k = URLEncoder.encode(k, charset);
					v = URLEncoder.encode(v.toString(), charset);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}
			list.add(k + "=" + v);
		}

		Collections.sort(list);
		StringBuilder builder = new StringBuilder("");
		for (String kv: list) {
			builder.append(kv).append(conn);
		}

		if (conn.length() > 0) {
			builder.delete(builder.length() - conn.length(), builder.length());
		}
		return builder.toString();
	}

	/**
	 * 获取域名
	 * 如 http://www.yuweix.com/v1/file
	 * 返回 http://www.yuweix.com
	 * @param url
	 * @return
	 */
	public static String getDomainUrl(String url) {
		try {
			StringBuilder builder = new StringBuilder("");
			URL url0 = new URL(url);
			builder.append(url0.getProtocol()).append("://").append(url0.getHost());
			int port = url0.getPort();
			if (port != Constant.DEFAULT_HTTP_PORT && port != Constant.DEFAULT_HTTPS_PORT && port > 0) {
				builder.append(":").append(port);
			}
			return builder.toString();
		} catch (Exception ex) {
			log.error("getDomainUrl error: {}", ex.getMessage(), ex);
			return null;
		}
	}

	public static<T> T copyProperties(Object source, Class<T> targetClass, String... ignoreProperties) {
		if (source == null) {
			return null;
		}

		try {
			Constructor<T> constructor = targetClass.getDeclaredConstructor();
			constructor.setAccessible(true);
			T targetObj = constructor.newInstance();
			copyProperties(source, targetObj, ignoreProperties);
			return targetObj;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static<T> void copyProperties(Object source, T target, String... ignoreProperties) {
		if (source == null || target == null) {
			return;
		}

		try {
			BeanUtils.copyProperties(source, target, ignoreProperties);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 求集合的交集
	 * @param list1
	 * @param list2
	 * @param <T>
	 * @return
	 */
	public static<T> List<T> intersect(List<T> list1, List<T> list2) {
		if (list1 == null || list1.size() <= 0
				|| list2 == null || list2.size() <= 0) {
			return new ArrayList<>();
		}

		Set<T> set = new HashSet<>();
		set.addAll(list1);
		set.addAll(list2);

		List<T> list = new ArrayList<>(list1);
		list2.forEach(set::remove);
		list.removeAll(set);
		return list;
	}

	/**
	 * Gets all fields of the given class and its parents (if any).
	 * @return
	 */
	public static List<Field> getFieldList(Class<?> clz) {
		final List<Field> allFields = new ArrayList<>();
		Class<?> currentClass = clz;
		while (currentClass != null) {
			final Field[] declaredFields = currentClass.getDeclaredFields();
			allFields.addAll(Arrays.asList(declaredFields));
			currentClass = currentClass.getSuperclass();
		}
		return allFields;
	}

	public static boolean exists(Long[] list, long num) {
		if (list == null || list.length <= 0) {
			return false;
		}

		for (long anum: list) {
			if (anum == num) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 集合list中是否包含元素str
	 * @param list
	 * @param str
	 * @return
	 */
	public static boolean exists(List<String> list, String str) {
		if (list == null || str == null) {
			return false;
		}

		for (String astr: list) {
			if (astr.trim().equals(str.trim())) {
				return true;
			}
		}
		return false;
	}
	public static boolean exists(List<? extends Number> list, Number num) {
		for (Number anum: list) {
			if (anum.equals(num)) {
				return true;
			}
		}
		return false;
	}

	public static void setVal(Object obj, Field field, Object val) {
		try {
			field.setAccessible(true);
			field.set(obj, val);
		} catch (Exception ex) {
			log.error("setVal>>>Error: {}", ex.getMessage());
			throw new RuntimeException(ex);
		}
	}

	public static Object getVal(Object obj, Field field) {
		try {
			field.setAccessible(true);
			return field.get(obj);
		} catch (Exception ex) {
			log.error("getVal>>>Error: {}", ex.getMessage());
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 取嵌套Map的值
	 */
	@SuppressWarnings("unchecked")
	public static Object getNestedMapValue(Map<String, Object> root, String[] pathList) {
		Object current = root;
		for (String path: pathList) {
			if (!(current instanceof Map)) {
				return null;
			}
			Map<String, Object> curMap = (Map<String, Object>) current;
			current = curMap.get(path);
			if (current == null) {
				return null;
			}
		}
		return current;
	}

	public static<T> Class<T> getGenericClass(Class<?> clz) {
		return getGenericClass(clz, 0);
	}
	@SuppressWarnings("unchecked")
	public static<T> Class<T> getGenericClass(Class<?> clz, int index) {
		if (clz == null) {
			return null;
		}
		Class<T> genericClz = null;
		Type t = clz.getGenericSuperclass();
		if (t instanceof ParameterizedType) {
			genericClz = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[index];
		}
		return genericClz;
	}
}
