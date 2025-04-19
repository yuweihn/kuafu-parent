package com.yuweix.kuafu.data.springboot.lettuce;


import com.yuweix.kuafu.core.json.Json;
import com.yuweix.kuafu.data.cache.redis.lettuce.LettuceCache;
import com.yuweix.kuafu.data.serializer.CacheSerializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;


/**
 * 单实例redis
 * @author yuwei
 */
public class LettuceConf {
	@Bean(name = "lettuceClientConfiguration")
	public LettuceClientConfiguration clientConfiguration(@Value("${kuafu.redis.pool.max-total:20}") int maxTotal
			, @Value("${kuafu.redis.pool.max-idle:10}") int maxIdle
			, @Value("${kuafu.redis.pool.min-idle:10}") int minIdle
			, @Value("${kuafu.redis.pool.max-wait-millis:10000}") long maxWaitMillis
			, @Value("${kuafu.redis.pool.time-between-eviction-runs-millis:-1}") long timeBetweenEvictionRunsMillis
			, @Value("${kuafu.redis.pool.test-on-borrow:false}") boolean testOnBorrow
			, @Value("${kuafu.redis.timeout-millis:5000}") long timeoutMillis) {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxWaitMillis(maxWaitMillis);
		poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		poolConfig.setTestOnBorrow(testOnBorrow);
		LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
				.commandTimeout(Duration.ofMillis(timeoutMillis))
				.poolConfig(poolConfig)
				.build();
		return clientConfig;
	}

	@Bean(name = "redisStandaloneConfiguration")
	public RedisStandaloneConfiguration redisStandaloneConfiguration(@Value("${kuafu.redis.host:}") String host
			, @Value("${kuafu.redis.port:0}") int port
			, @Value("${kuafu.redis.db-index:0}") int dbIndex
			, @Value("${kuafu.redis.need-password:false}") boolean needPassword
			, @Value("${kuafu.redis.password:}") String password) {
		RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
		conf.setHostName(host);
		if (port > 0) {
			conf.setPort(port);
		}
		if (dbIndex >= 0) {
			conf.setDatabase(dbIndex);
		}
		if (needPassword) {
			conf.setPassword(RedisPassword.of(password));
		}
		return conf;
	}

	@Primary
	@ConditionalOnMissingBean(name = "lettuceConnectionFactory")
	@Bean(name = "lettuceConnectionFactory")
	public LettuceConnectionFactory lettuceConnectionFactory(@Qualifier("lettuceClientConfiguration") LettuceClientConfiguration clientConfig
			, @Qualifier("redisStandaloneConfiguration") RedisStandaloneConfiguration config) {
		LettuceConnectionFactory connFactory = new LettuceConnectionFactory(config, clientConfig);
		connFactory.setValidateConnection(false);
		connFactory.setShareNativeConnection(true);
		return connFactory;
	}

	@Bean(name = "redisTemplate")
	public RedisTemplate<String, Object> redisTemplate(@Qualifier("lettuceConnectionFactory") LettuceConnectionFactory connFactory) {
		RedisSerializer<?> redisSerializer = new StringRedisSerializer();
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connFactory);
		template.setKeySerializer(redisSerializer);
		template.setValueSerializer(redisSerializer);
		template.setEnableDefaultSerializer(true);
//		template.setEnableTransactionSupport(true);
		return template;
	}

	@ConditionalOnMissingBean(RedisMessageListenerContainer.class)
	@Bean
	public RedisMessageListenerContainer messageContainer(@Qualifier("lettuceConnectionFactory") LettuceConnectionFactory connFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connFactory);
		return container;
	}

	@ConditionalOnMissingBean(CacheSerializer.class)
	@Bean
	public CacheSerializer cacheSerializer(Json json) {
		return new CacheSerializer() {
			@Override
			public <T> String serialize(T t) {
				return json.serialize(t);
			}

			@Override
			public <T> T deserialize(String str) {
				return json.deserialize(str);
			}
		};
	}

	@ConditionalOnMissingBean(name = "redisCache")
	@Bean(name = "redisCache")
	public LettuceCache redisCache(@Qualifier("redisTemplate") RedisTemplate<String, Object> template
			, CacheSerializer serializer
			, RedisMessageListenerContainer messageContainer) {
		LettuceCache cache = new LettuceCache(template, serializer);
		cache.setMessageContainer(messageContainer);
		return cache;
	}
}
