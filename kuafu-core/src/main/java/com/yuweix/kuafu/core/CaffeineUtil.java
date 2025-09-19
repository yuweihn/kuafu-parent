package com.yuweix.kuafu.core;


import com.github.benmanes.caffeine.cache.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 * @author yuwei
 */
public class CaffeineUtil {
	private static final Logger log = LoggerFactory.getLogger(CaffeineUtil.class);


	private LoadingCache<String, String> cache = null;


	private CaffeineUtil(long duration) {
		try {
            cache = Caffeine.newBuilder()
                    .expireAfterAccess(duration, TimeUnit.SECONDS)
                    .removalListener(new RemovalListener<String, String>() {
                        @Override
                        public void onRemoval(@Nullable String key, @Nullable String value, RemovalCause removalCause) {
                            if (log.isDebugEnabled()) {
                                log.debug("Caffeine缓存回收成功，键：{}, 值：{}", key, value);
                            }
                        }
                    }).build(new CacheLoader<String, String>() {
                        @Override
                        public String load(String key) {
                            if (log.isDebugEnabled()) {
                                log.debug("Caffeine缓存值不存在，初始化空值，Key：{}", key);
                            }
                            return null;
                        }
                    });
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param duration   缓存时间(单位：秒)
	 */
	public static CaffeineUtil create(long duration) {
		return new CaffeineUtil(duration);
	}

	public boolean put(String key, String value) {
		try {
            cache.put(key, value);
			if (log.isDebugEnabled()) {
				log.debug("Caffeine缓存命中率：{}，新值平均加载时间：{}", getHitRate(), getAverageLoadPenalty());
			}
			return true;
		} catch (Exception e) {
			log.error("Caffeine设置缓存值出错，Error: {}", e.getMessage(), e);
			return false;
		}
	}

	public boolean putAll(Map<String, String> map) {
		try {
            cache.putAll(map);
			if (log.isDebugEnabled()) {
				log.debug("Caffeine缓存命中率：{}，新值平均加载时间：{}", getHitRate(), getAverageLoadPenalty());
			}
			return true;
		} catch (Exception e) {
			log.error("Caffeine批量设置缓存值出错，Error: {}", e.getMessage(), e);
			return false;
		}
	}

	public String get(String key) {
		String val = null;
		try {
			val = cache.get(key, t -> null);
			if (log.isDebugEnabled()) {
				log.debug("Caffeine缓存命中率：{}，新值平均加载时间：{}", getHitRate(), getAverageLoadPenalty());
			}
		} catch (Exception e) {
			log.error("Caffeine获取缓存值出错，Error: {}", e.getMessage(), e);
		}
		return val;
	}

	public String getIfPresent(String key) {
		String val = null;
		try {
			val = cache.getIfPresent(key);
			if (log.isDebugEnabled()) {
				log.debug("Caffeine缓存命中率：{}，新值平均加载时间：{}", getHitRate(), getAverageLoadPenalty());
			}
		} catch (Exception e) {
			log.error("Caffeine获取缓存值出错，Error: {}", e.getMessage(), e);
		}
		return val;
	}

	public boolean remove(String key) {
		try {
            cache.invalidate(key);
			if (log.isDebugEnabled()) {
				log.debug("Caffeine缓存命中率：{}，新值平均加载时间：{}", getHitRate(), getAverageLoadPenalty());
			}
			return true;
		} catch (Exception e) {
			log.error("Caffeine移除缓存出错，Error: {}", e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 批量移除缓存
	 */
	public boolean removeAll(Iterable<String> keys) {
		try {
            cache.invalidateAll(keys);
			if (log.isDebugEnabled()) {
				log.debug("Caffeine缓存命中率：{}，新值平均加载时间：{}", getHitRate(), getAverageLoadPenalty());
			}
			return true;
		} catch (Exception e) {
			log.error("Caffeine批量移除缓存出错，Error: {}", e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 清空所有缓存
	 */
	public boolean removeAll() {
		try {
            cache.invalidateAll();
			if (log.isDebugEnabled()) {
				log.debug("Caffeine缓存命中率：{}，新值平均加载时间：{}", getHitRate(), getAverageLoadPenalty());
			}
			return true;
		} catch (Exception e) {
			log.error("Caffeine清空所有缓存出错，Error: {}", e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 获取缓存项数量
	 */
	public long size() {
		long size = 0;
		try {
			size = cache.estimatedSize();
			if (log.isDebugEnabled()) {
				log.debug("Caffeine缓存命中率：{}，新值平均加载时间：{}", getHitRate(), getAverageLoadPenalty());
			}
		} catch (Exception e) {
			log.error("Caffeine获取缓存项数量出错，Error: {}", e.getMessage(), e);
		}
		return size;
	}

	/**
	 * 获取所有缓存项的键
	 */
	public List<String> keys() {
		List<String> list = new ArrayList<>();
		try {
			ConcurrentMap<String, String> map = cache.asMap();
			for (Map.Entry<String, String> item : map.entrySet()) {
				list.add(item.getKey());
			}
			if (log.isDebugEnabled()) {
				log.debug("Caffeine缓存命中率：{}，新值平均加载时间：{}", getHitRate(), getAverageLoadPenalty());
			}
		} catch (Exception e) {
			log.error("Caffeine获取所有缓存项的键出错，Error: {}", e.getMessage(), e);
		}
		return list;
	}

	/**
	 * 缓存命中率
	 */
	public double getHitRate() {
		return cache.stats().hitRate();
	}

	/**
	 * 加载新值的平均时间，单位为纳秒
	 */
	public double getAverageLoadPenalty() {
		return cache.stats().averageLoadPenalty();
	}

	/**
	 * 缓存项被回收的总数，不包括显式清除
	 */
	public long getEvictionCount() {
		return cache.stats().evictionCount();
	}
}
