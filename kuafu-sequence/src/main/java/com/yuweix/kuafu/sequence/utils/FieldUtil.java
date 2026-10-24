package com.yuweix.kuafu.sequence.utils;


import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yuwei
 */
public abstract class FieldUtil {
	private static volatile SoftReference<Map<String, List<Field>>> FIELD_REF;
	private static final Object fieldLock = new Object();

	private static Map<String, List<Field>> getFieldMap() {
		SoftReference<Map<String, List<Field>>> ref = FIELD_REF;
		Map<String, List<Field>> map = (ref == null) ? null : ref.get();
		if (map == null) {
			synchronized (fieldLock) {
				ref = FIELD_REF;
				map = (ref == null) ? null : ref.get();
				if (map == null) {
					map = new ConcurrentHashMap<>();
					FIELD_REF = new SoftReference<>(map);
				}
			}
		}
		return map;
	}

	public static List<Field> getAllFieldList(Class<?> clz) {
		Map<String, List<Field>> map = getFieldMap();
		return map.computeIfAbsent(clz.getName(), key -> getAllFieldList0(clz));
	}

	/**
	 * Gets all fields of the given class and its parents (if any).
	 * @return
	 */
	private static List<Field> getAllFieldList0(Class<?> clz) {
		final List<Field> allFields = new ArrayList<>();
		Class<?> currentClass = clz;
		while (currentClass != null) {
			allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
			currentClass = currentClass.getSuperclass();
		}
		return Collections.unmodifiableList(allFields);
	}
}