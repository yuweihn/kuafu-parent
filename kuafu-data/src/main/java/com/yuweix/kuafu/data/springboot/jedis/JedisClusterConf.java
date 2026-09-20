package com.yuweix.kuafu.data.springboot.jedis;


import com.yuweix.kuafu.core.serialize.Serializer;
import com.yuweix.kuafu.data.cache.redis.jedis.JedisClusterCache;
import com.yuweix.kuafu.data.serializer.CacheSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;


/**
 * redis集群
 * @author yuwei
 */
public class JedisClusterConf {
	@Bean(name = "jedisPoolConfig")
	public JedisPoolConfig jedisPoolConfig(@Value("${kuafu.redis.pool.max-total:20}") int maxTotal
			, @Value("${kuafu.redis.pool.max-idle:10}") int maxIdle
			, @Value("${kuafu.redis.pool.min-idle:2}") int minIdle
			, @Value("${kuafu.redis.pool.max-wait-millis:3000}") long maxWaitMillis
			, @Value("${kuafu.redis.pool.time-between-eviction-runs-millis:30000}") long timeBetweenEvictionRunsMillis
			, @Value("${kuafu.redis.pool.test-on-borrow:false}") boolean testOnBorrow
			, @Value("${kuafu.redis.pool.test-while-idle:true}") boolean testWhileIdle
			, @Value("${kuafu.redis.pool.min-evictable-idle-time-millis:60000}") long minEvictableIdleTimeMillis
			, @Value("${kuafu.redis.pool.soft-min-evictable-idle-time-millis:60000}") long softMinEvictableIdleTimeMillis
			, @Value("${kuafu.redis.pool.num-tests-per-eviction-run:3}") int numTestsPerEvictionRun
			, @Value("${kuafu.redis.pool.eviction-policy-class-name:}") String evictionPolicyClassName) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMinIdle(minIdle);
		config.setMaxWait(Duration.ofMillis(maxWaitMillis));
		config.setTimeBetweenEvictionRuns(Duration.ofMillis(timeBetweenEvictionRunsMillis));
		config.setTestOnBorrow(testOnBorrow);
		config.setTestWhileIdle(testWhileIdle);

		// ==================== Evict 策略配置 ====================
		// 连接空闲达到该时间后，可被驱逐线程回收（默认 60 秒）
		config.setMinEvictableIdleTime(Duration.ofMillis(minEvictableIdleTimeMillis));
		// 软驱逐：当空闲连接数 > minIdle 时，达到该时间也可被驱逐（与 minEvictableIdleTime 取更严格的）
		config.setSoftMinEvictableIdleTime(Duration.ofMillis(softMinEvictableIdleTimeMillis));
		// 每次驱逐线程运行时检测的连接数，-1 表示检测全部（生产环境建议保持 3 或根据池大小调整）
		config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		// 使用默认驱逐策略（如需自定义，可改为自定义类全限定名）
		if (evictionPolicyClassName != null && !evictionPolicyClassName.isEmpty()) {
			config.setEvictionPolicyClassName(evictionPolicyClassName);
		}

		return config;
	}

	@Bean(name = "jedisCluster")
	public JedisCluster jedisCluster(@Qualifier("jedisPoolConfig") JedisPoolConfig jedisPoolConfig
			, @Qualifier("redisNodeList") List<HostAndPort> redisNodeList
			, @Value("${kuafu.redis.socket.connect-timeout-millis:3000}") long connectTimeoutMillis
			, @Value("${kuafu.redis.socket.command-timeout-millis:5000}") long commandTimeoutMillis
			, @Value("${kuafu.redis.socket.max-attempts:3}") int maxAttempts) {
		return new JedisCluster(new HashSet<>(redisNodeList), (int) connectTimeoutMillis, (int) commandTimeoutMillis
				, maxAttempts, jedisPoolConfig);
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
