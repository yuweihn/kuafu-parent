package com.yuweix.kuafu.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwei
 */
public abstract class MapUtil {
	private static final Logger log = LoggerFactory.getLogger(MapUtil.class);


	/**
	 * 合并多个Map，返回一个新的HashMap。后面的Map会覆盖前面的同名Key。
	 * @param maps 待合并的Map列表
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return 合并后的新Map
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> merge(Map<K, V>... maps) {
		Map<K, V> result = new HashMap<>();
		if (maps == null || maps.length <= 0) {
			return result;
		}
		for (Map<K, V> map : maps) {
			if (map == null) {
				continue;
			}
			result.putAll(map);
		}
		return result;
	}

	/**
	 * 合并多个Map，忽略值为null的项。返回一个新的HashMap。
	 * @param maps 待合并的Map列表
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return 合并后的新Map
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> mergeIgnoreNull(Map<K, V>... maps) {
		Map<K, V> result = new HashMap<>();
		if (maps == null || maps.length <= 0) {
			return result;
		}
		for (Map<K, V> map : maps) {
			if (map == null) {
				continue;
			}
			for (Map.Entry<K, V> entry : map.entrySet()) {
				if (entry.getValue() != null) {
					result.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * 将多个Map合并到指定的目标Map中。后面的Map会覆盖前面的同名Key。
	 * @param target 目标Map
	 * @param maps   待合并的Map列表
	 * @param <K>    键类型
	 * @param <V>    值类型
	 * @return 合并后的目标Map
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> mergeAll(Map<K, V> target, Map<K, V>... maps) {
		if (target == null) {
			throw new IllegalArgumentException("[target] is required.");
		}
		if (maps == null || maps.length <= 0) {
			return target;
		}
		for (Map<K, V> map : maps) {
			if (map == null) {
				continue;
			}
			target.putAll(map);
		}
		return target;
	}

	/**
	 * 合并多个Map，当Key冲突时，使用自定义的冲突解决策略。
	 * @param remapping 冲突解决函数，参数为(oldValue, newValue)
	 * @param maps      待合并的Map列表
	 * @param <K>       键类型
	 * @param <V>       值类型
	 * @return 合并后的新Map
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> mergeWithRemapping(java.util.function.BiFunction<? super V, ? super V, ? extends V> remapping
			, Map<K, V>... maps) {
		Map<K, V> result = new HashMap<>();
		if (maps == null || maps.length <= 0) {
			return result;
		}
		if (remapping == null) {
			return merge(maps);
		}
		for (Map<K, V> map : maps) {
			if (map == null) {
				continue;
			}
			for (Map.Entry<K, V> entry : map.entrySet()) {
				K key = entry.getKey();
				V value = entry.getValue();
				result.merge(key, value, remapping);
			}
		}
		return result;
	}

	/**
	 * 深度合并多个Map。仅当Value为Map类型时，会递归合并。
	 * 注意：非Map类型的Value会被后面的覆盖。
	 * @param maps 待合并的Map列表
	 * @return 合并后的新Map
	 */
	@SafeVarargs
	public static Map<Object, Object> mergeDeep(Map<Object, Object>... maps) {
		Map<Object, Object> result = new HashMap<>();
		if (maps == null || maps.length <= 0) {
			return result;
		}
		for (Map<Object, Object> map : maps) {
			if (map == null) {
				continue;
			}
			for (Map.Entry<Object, Object> entry : map.entrySet()) {
				Object key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof Map && result.get(key) instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<Object, Object> merged = mergeDeep(
							(Map<Object, Object>) result.get(key),
							(Map<Object, Object>) value
					);
					result.put(key, merged);
				} else {
					result.put(key, value);
				}
			}
		}
		return result;
	}

	/**
	 * 判断Map是否为空（null 或 空Map）
	 * @param map 待判断的Map
	 * @return true 表示为空
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * 判断Map是否不为空
	 * @param map 待判断的Map
	 * @return true 表示不为空
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}
}