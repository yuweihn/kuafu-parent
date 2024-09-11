package com.yuweix.kuafu.boot.dao;


import com.yuweix.kuafu.dao.PersistCache;
import com.yuweix.kuafu.data.cache.Cache;
import com.yuweix.kuafu.dao.springboot.MybatisConf;
import com.yuweix.kuafu.sequence.springboot.SequenceConf;
import com.yuweix.kuafu.sharding.springboot.ShardingConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.dao.mybatis.enabled")
@Import({MybatisConf.class, SequenceConf.class, ShardingConf.class})
public class MybatisAutoConfiguration {
	@ConditionalOnMissingBean(PersistCache.class)
	@Bean(name = "persistCache")
	public PersistCache persistCache(@Autowired(required = false) Cache cache) {
		return new PersistCache() {
			@Override
			public <T> boolean put(String key, T t, long timeout) {
				return cache != null && cache.put(key, t, timeout);
			}
			@Override
			public <T> T get(String key) {
				return cache == null ? null : cache.get(key);
			}
			@Override
			public void remove(String key) {
				if (cache == null) {
					return;
				}
				cache.remove(key);
			}
		};
	}
}
