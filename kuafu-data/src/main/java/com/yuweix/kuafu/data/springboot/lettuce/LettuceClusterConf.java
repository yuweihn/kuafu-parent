package com.yuweix.kuafu.data.springboot.lettuce;


import com.yuweix.kuafu.core.serialize.Serializer;
import com.yuweix.kuafu.data.cache.redis.lettuce.LettuceCache;
import com.yuweix.kuafu.data.serializer.CacheSerializer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;


/**
 * redis集群
 * @author yuwei
 */
public class LettuceClusterConf {
	@Bean(name = "lettuceClientConfiguration")
	public LettuceClientConfiguration clientConfiguration(@Value("${kuafu.redis.pool.max-total:20}") int maxTotal
			, @Value("${kuafu.redis.pool.max-idle:10}") int maxIdle
			, @Value("${kuafu.redis.pool.min-idle:4}") int minIdle
			, @Value("${kuafu.redis.pool.max-wait-millis:1000}") long maxWaitMillis
			, @Value("${kuafu.redis.pool.time-between-eviction-runs-millis:30000}") long timeBetweenEvictionRunsMillis
			, @Value("${kuafu.redis.pool.test-on-borrow:false}") boolean testOnBorrow
			, @Value("${kuafu.redis.pool.test-while-idle:true}") boolean testWhileIdle
			, @Value("${kuafu.redis.pool.min-evictable-idle-time-millis:1800000}") long minEvictableIdleTimeMillis
			, @Value("${kuafu.redis.pool.soft-min-evictable-idle-time-millis:120000}") long softMinEvictableIdleTimeMillis
			, @Value("${kuafu.redis.pool.num-tests-per-eviction-run:-1}") int numTestsPerEvictionRun
			, @Value("${kuafu.redis.pool.eviction-policy-class-name:}") String evictionPolicyClassName
			, @Value("${kuafu.redis.socket.connect-timeout-millis:2000}") long connectTimeoutMillis
			, @Value("${kuafu.redis.socket.command-timeout-millis:2000}") long commandTimeoutMillis
			, @Value("${kuafu.redis.socket.extended-keepalive:false}") boolean extendedKeepAlive
			, @Value("${kuafu.redis.socket.idle-millis:30000}") long socketIdleMillis
			, @Value("${kuafu.redis.socket.interval-millis:5000}") long socketIntervalMillis
			, @Value("${kuafu.redis.socket.max-attempts:3}") int maxAttempts
			, @Value("${kuafu.redis.client.request-queue-size:1024}") int requestQueueSize) {
		// ---------- 启动即校验，避免"配了但不生效"或"配错静默降级" ----------
		// 容量必须满足 0 <= min-idle <= max-idle <= max-total
		if (maxTotal < 1 || maxIdle < 0 || minIdle < 0 || minIdle > maxIdle || maxIdle > maxTotal) {
			throw new IllegalArgumentException("kuafu.redis.pool 容量配置不合法：min-idle=" + minIdle
					+ ", max-idle=" + maxIdle + ", max-total=" + maxTotal
					+ "，需满足 0 <= min-idle <= max-idle <= max-total");
		}
		// 非正值等价于"无限等待"，池耗尽时业务线程会一直挂住而不是抛异常
		if (maxWaitMillis <= 0) {
			throw new IllegalArgumentException("kuafu.redis.pool.max-wait-millis 必须大于 0，<=0 表示无限等待：" + maxWaitMillis);
		}
		// 巡检间隔 <=0 时 evictor 线程不启动，test-while-idle 的保活 PING 也不会执行
		if (timeBetweenEvictionRunsMillis <= 0) {
			throw new IllegalArgumentException("kuafu.redis.pool.time-between-eviction-runs-millis 必须大于 0：" + timeBetweenEvictionRunsMillis);
		}
		// 硬驱逐阈值 <=0 时，每轮巡检都会把全部空闲连接判定为可回收，池会被反复清空再重建
		if (minEvictableIdleTimeMillis <= 0) {
			throw new IllegalArgumentException("kuafu.redis.pool.min-evictable-idle-time-millis 必须大于 0：" + minEvictableIdleTimeMillis);
		}
		// Lettuce 下发 TCP_KEEPIDLE/TCP_KEEPINTERVAL 时用的是 getSeconds()，毫秒不足 1 秒会被截断成 0，内核直接拒绝
		if (extendedKeepAlive && (socketIdleMillis < 1000 || socketIntervalMillis < 1000 || maxAttempts < 1)) {
			throw new IllegalArgumentException("扩展 TCP keepalive 参数以整秒下发，idle/interval 不能小于 1000ms 且 max-attempts 不能小于 1：idle="
					+ socketIdleMillis + ", interval=" + socketIntervalMillis + ", max-attempts=" + maxAttempts);
		}

		GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setMaxIdle(maxIdle);
		// share-native-connection=true（默认）时，池只服务阻塞命令与事务，min-idle 不必很大；
		// 若关闭共享连接（每次操作单独借还），应把 min-idle 提到与 max-idle 一致，避免反复建连
		poolConfig.setMinIdle(minIdle);
		// 池耗尽时的最大等待：必须明显小于上游（网关/Feign）超时，否则等到的必然是一次超时失败
		poolConfig.setMaxWait(Duration.ofMillis(maxWaitMillis));
		poolConfig.setBlockWhenExhausted(true);
		poolConfig.setTestOnBorrow(testOnBorrow);
		poolConfig.setTestWhileIdle(testWhileIdle);
		poolConfig.setTestOnReturn(false);

		// ==================== Evict 策略配置 ====================
		// 巡检周期：evictor 每该毫秒数跑一轮，负责"校验（保活）+ 驱逐 + 补齐 min-idle"
		poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(timeBetweenEvictionRunsMillis));
		// 硬阈值：不管池里现有多少连接，空闲超过该时间一律销毁。默认 30 分钟；
		// 设得太小（如 60 秒）会让核心连接被反复销毁再重建，建连开销与 Redis 侧 session 抖动都会放大
		poolConfig.setMinEvictableIdleTime(Duration.ofMillis(minEvictableIdleTimeMillis));
		// 软阈值：仅当"空闲连接数 > min-idle"时生效，用于突发流量后的缩容，必须小于硬阈值，否则永远轮不到它
		poolConfig.setSoftMinEvictableIdleTime(Duration.ofMillis(softMinEvictableIdleTimeMillis));
		// 每轮巡检校验的连接数：-1 表示全量校验。默认 3 时，10 条空闲连接要 90 秒才轮到一次 PING，
		// 恰好给了中间设备（NAT/SLB/云代理）掐断空闲连接的机会，保活等于没做
		poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		// 使用默认驱逐策略（如需自定义，可改为自定义类全限定名）
		if (evictionPolicyClassName != null && !evictionPolicyClassName.isEmpty()) {
			poolConfig.setEvictionPolicyClassName(evictionPolicyClassName);
		}

		SocketOptions.Builder socketOptionsBuilder = SocketOptions.builder()
				.connectTimeout(Duration.ofMillis(connectTimeoutMillis)) // 连接建立超时，避免建连慢时长时间占线程
				.tcpNoDelay(true); // 禁用 Nagle 算法，减少小包延迟
		if (extendedKeepAlive) {
			// 内核级探测参数（TCP_KEEPIDLE/TCP_KEEPINTERVAL/TCP_KEEPCOUNT）。
			// 注意：JDK 8 + NioSocketChannel 上这组参数下不去，Lettuce 会退化成一条 WARN 后什么都不做；
			// 真要生效需要 netty-transport-native-epoll（得到 EpollSocketChannel）或 JDK 11+。
			// 连接池的 test-while-idle PING 已经能覆盖空闲连接的存活检测，所以默认关闭。
			socketOptionsBuilder.keepAlive(SocketOptions.KeepAliveOptions.builder()
					.enable()
					.idle(Duration.ofMillis(socketIdleMillis)) // 空闲多久后发第一个探测包
					.interval(Duration.ofMillis(socketIntervalMillis)) // 探测包发送间隔
					.count(maxAttempts) // 连续多少次无响应判定连接失效
					.build());
		} else {
			// 只开 SO_KEEPALIVE，交给内核默认参数（Linux 默认 7200 秒），不再尝试扩展参数，WARN 消失
			socketOptionsBuilder.keepAlive(true);
		}
		ClientOptions clientOptions = ClientOptions.builder()
				.socketOptions(socketOptionsBuilder.build())
				.autoReconnect(true)
				.requestQueueSize(requestQueueSize) // 必须有界，否则断连期间命令无限堆积
				// DEFAULT 在 autoReconnect=true 时等价于 ACCEPT_COMMANDS：断连期间命令进队列等待重连，
				// 恢复后集体回放，这些命令早已超过 commandTimeout，会把一次抖动放大成雪崩。
				// 这里改为"断连即失败"，让上层降级/熔断快速接管。
				.disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
				.build();

		// 超时预算：max-wait(1000) + command-timeout(2000) = 3 秒，应小于上游超时
		LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
				.commandTimeout(Duration.ofMillis(commandTimeoutMillis))
				.poolConfig(poolConfig)
				.clientOptions(clientOptions)
				.build();
		return clientConfig;
	}

	@Bean(name = "redisClusterConfiguration")
	public RedisClusterConfiguration redisClusterConfiguration(@Qualifier("redisNodeList") List<String> redisNodeList
			, @Value("${kuafu.redis.cluster.timeout:300000}") int timeout
			, @Value("${kuafu.redis.cluster.max-redirections:6}") int maxRedirections) {
		RedisClusterConfiguration conf = new RedisClusterConfiguration(redisNodeList);
		conf.setMaxRedirects(maxRedirections);
		return conf;
	}

	@Bean(name = "lettuceConnectionFactory")
	public LettuceConnectionFactory lettuceConnectionFactory(@Qualifier("lettuceClientConfiguration") LettuceClientConfiguration clientConfig
			, @Qualifier("redisClusterConfiguration") RedisClusterConfiguration config
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
