package com.yuweix.kuafu.data.springboot.lettuce;


import com.yuweix.kuafu.core.serialize.Serializer;
import com.yuweix.kuafu.data.cache.redis.lettuce.LettuceCache;
import com.yuweix.kuafu.data.serializer.CacheSerializer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
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
			, @Value("${kuafu.redis.pool.min-idle:2}") int minIdle
			, @Value("${kuafu.redis.pool.max-wait-millis:3000}") long maxWaitMillis
			, @Value("${kuafu.redis.pool.time-between-eviction-runs-millis:30000}") long timeBetweenEvictionRunsMillis
			, @Value("${kuafu.redis.pool.test-on-borrow:false}") boolean testOnBorrow
			, @Value("${kuafu.redis.pool.test-while-idle:true}") boolean testWhileIdle
			, @Value("${kuafu.redis.pool.min-evictable-idle-time-millis:60000}") long minEvictableIdleTimeMillis
			, @Value("${kuafu.redis.pool.soft-min-evictable-idle-time-millis:60000}") long softMinEvictableIdleTimeMillis
			, @Value("${kuafu.redis.pool.num-tests-per-eviction-run:3}") int numTestsPerEvictionRun
			, @Value("${kuafu.redis.pool.eviction-policy-class-name:}") String evictionPolicyClassName
			, @Value("${kuafu.redis.socket.connect-timeout-millis:3000}") long connectTimeoutMillis
			, @Value("${kuafu.redis.socket.command-timeout-millis:5000}") long commandTimeoutMillis
			, @Value("${kuafu.redis.socket.idle-millis:30000}") long socketIdleMillis
			, @Value("${kuafu.redis.socket.interval-millis:5000}") long socketIntervalMillis
			, @Value("${kuafu.redis.socket.max-attempts:3}") int maxAttempts
			, @Value("${kuafu.redis.client.request-queue-size:1024}") int requestQueueSize) {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxWait(Duration.ofMillis(maxWaitMillis));
		poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(timeBetweenEvictionRunsMillis));
		poolConfig.setTestOnBorrow(testOnBorrow);
		poolConfig.setTestWhileIdle(testWhileIdle);

		// ==================== Evict 策略配置 ====================
		// 连接空闲达到该时间后，可被驱逐线程回收（默认 60 秒）
		poolConfig.setMinEvictableIdleTime(Duration.ofMillis(minEvictableIdleTimeMillis));
		// 软驱逐：当空闲连接数 > minIdle 时，达到该时间也可被驱逐（与 minEvictableIdleTime 取更严格的）
		poolConfig.setSoftMinEvictableIdleTime(Duration.ofMillis(softMinEvictableIdleTimeMillis));
		// 每次驱逐线程运行时检测的连接数，-1 表示检测全部（生产环境建议保持 3 或根据池大小调整）
		poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		// 使用默认驱逐策略（如需自定义，可改为自定义类全限定名）
		if (evictionPolicyClassName != null && !evictionPolicyClassName.isEmpty()) {
			poolConfig.setEvictionPolicyClassName(evictionPolicyClassName);
		}

		SocketOptions.KeepAliveOptions keepAliveOptions = SocketOptions.KeepAliveOptions.builder()
				.enable() // 启用 TCP KeepAlive
				.idle(Duration.ofMillis(socketIdleMillis)) // 连接空闲××时间后开始发送第一个探测包
				.interval(Duration.ofMillis(socketIntervalMillis)) // 探测包发送间隔为××秒
				.count(maxAttempts) // 最多发送××次探测包，如果都无响应则判定连接失效
				.build();
		SocketOptions socketOptions = SocketOptions.builder()
				.keepAlive(keepAliveOptions) // 使用新的 KeepAliveOptions
				.connectTimeout(Duration.ofMillis(connectTimeoutMillis)) // 连接建立超时
				.tcpNoDelay(true) // 禁用 Nagle 算法，减少小包延迟
				.build();
		ClientOptions clientOptions = ClientOptions.builder()
				.socketOptions(socketOptions)
				.autoReconnect(true)
				.requestQueueSize(requestQueueSize)
				.disconnectedBehavior(ClientOptions.DisconnectedBehavior.DEFAULT)
				.build();

		LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
				.commandTimeout(Duration.ofMillis(commandTimeoutMillis))
				.poolConfig(poolConfig)
				.clientOptions(clientOptions)
				.build();
		return clientConfig;
	}

	@Bean(name = "redisStandaloneConfiguration")
	public RedisStandaloneConfiguration redisStandaloneConfiguration(@Value("${kuafu.redis.host:}") String host
			, @Value("${kuafu.redis.port:0}") int port
			, @Value("${kuafu.redis.db-index:0}") int dbIndex
			, @Value("${kuafu.redis.password-required:false}") boolean passwordRequired
			, @Value("${kuafu.redis.password:}") String password) {
		RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration();
		conf.setHostName(host);
		if (port > 0) {
			conf.setPort(port);
		}
		if (dbIndex >= 0) {
			conf.setDatabase(dbIndex);
		}
		if (passwordRequired) {
			conf.setPassword(RedisPassword.of(password));
		}
		return conf;
	}

	@Primary
	@ConditionalOnMissingBean(name = "lettuceConnectionFactory")
	@Bean(name = "lettuceConnectionFactory")
	public LettuceConnectionFactory lettuceConnectionFactory(@Qualifier("lettuceClientConfiguration") LettuceClientConfiguration clientConfig
			, @Qualifier("redisStandaloneConfiguration") RedisStandaloneConfiguration config
			, @Value("${kuafu.redis.conn.validate-connection:false}") boolean validateConnection
			, @Value("${kuafu.redis.conn.share-native-connection:true}") boolean shareNativeConnection) {
		LettuceConnectionFactory connFactory = new LettuceConnectionFactory(config, clientConfig);
		connFactory.setValidateConnection(validateConnection);
		connFactory.setShareNativeConnection(shareNativeConnection);
		return connFactory;
	}

	@Bean(name = "redisTemplate")
	public RedisTemplate<String, Object> redisTemplate(@Qualifier("lettuceConnectionFactory") LettuceConnectionFactory connFactory) {
		RedisSerializer<?> redisSerializer = new StringRedisSerializer();
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connFactory);
		template.setKeySerializer(redisSerializer);
		template.setValueSerializer(redisSerializer);
		template.setHashKeySerializer(redisSerializer);
		template.setHashValueSerializer(redisSerializer);
		template.setEnableDefaultSerializer(false);
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

	@ConditionalOnMissingBean(LettuceCache.class)
	@Bean
	public LettuceCache redisCache(@Qualifier("redisTemplate") RedisTemplate<String, Object> template
			, CacheSerializer serializer
			, RedisMessageListenerContainer messageContainer) {
		LettuceCache cache = new LettuceCache(template, serializer);
		cache.setMessageContainer(messageContainer);
		return cache;
	}
}
