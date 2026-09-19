package com.yuweix.kuafu.data.springboot.jedis;


import com.yuweix.kuafu.core.serialize.Serializer;
import com.yuweix.kuafu.data.cache.redis.jedis.JedisClusterCache;
import com.yuweix.kuafu.data.cache.redis.jedis.JedisClusterFactory;
import com.yuweix.kuafu.data.serializer.CacheSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;


/**
 * redis集群
 * @author yuwei
 */
public class JedisClusterConf {

	@Bean(name = "jedisPoolConfig")
	public JedisPoolConfig jedisPoolConfig(@Value("${kuafu.redis.pool.max-total:20}") int maxTotal
			, @Value("${kuafu.redis.pool.max-idle:10}") int maxIdle
			, @Value("${kuafu.redis.pool.min-idle:10}") int minIdle
			, @Value("${kuafu.redis.pool.max-wait-millis:3000}") long maxWaitMillis
			, @Value("${kuafu.redis.pool.time-between-eviction-runs-millis:30000}") long timeBetweenEvictionRunsMillis
			, @Value("${kuafu.redis.pool.test-on-borrow:false}") boolean testOnBorrow
			, @Value("${kuafu.redis.pool.test-while-idle:true}") boolean testWhileIdle) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMinIdle(minIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		config.setTestOnBorrow(testOnBorrow);
		config.setTestWhileIdle(testWhileIdle);
		return config;
	}

	@Bean(name = "jedisCluster", initMethod = "init")
	public JedisClusterFactory jedisClusterFactory(@Qualifier("jedisPoolConfig") JedisPoolConfig jedisPoolConfig
			, @Qualifier("redisNodeList") List<HostAndPort> redisNodeList
			, @Value("${kuafu.redis.cluster.timeout:5000}") int timeout
			, @Value("${kuafu.redis.cluster.max-redirections:6}") int maxRedirections) {
		JedisClusterFactory factory = new JedisClusterFactory();
		factory.setJedisPoolConfig(jedisPoolConfig);
		factory.setRedisNodeList(redisNodeList);
		factory.setTimeout(timeout);
		factory.setMaxRedirections(maxRedirections);
		return factory;
	}

	@ConditionalOnMissingBean(CacheSerializer.class)
	@Bean
	public CacheSerializer cacheSerializer(Serializer serializer) {
		return new CacheSerializer() {
			@Override
			public <T> String serialize(T t) {
				return serializer.serialize(t);
			}

			@Override
			public <T> T deserialize(String str) {
				return serializer.deserialize(str);
			}
		};
	}

	@ConditionalOnMissingBean(JedisClusterCache.class)
	@Bean
	public JedisClusterCache redisClusterCache(@Qualifier("jedisCluster") JedisCluster jedisCluster
			, CacheSerializer serializer) {
		JedisClusterCache cache = new JedisClusterCache(serializer);
		cache.setJedisCluster(jedisCluster);
		return cache;
	}
}
