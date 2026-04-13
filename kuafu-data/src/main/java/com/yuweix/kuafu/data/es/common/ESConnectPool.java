package com.yuweix.kuafu.data.es.common;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


public class ESConnectPool extends AbstractESConnector {
	private static final Logger log = LoggerFactory.getLogger(ESConnectPool.class);

	/**
	 * 172.31.28.171:9200,172.31.28.172:9200,172.31.28.173:9200,172.31.28.174:9200
	 */
	private String ipAddress;
	private String scheme;
	private boolean needCredentials = false;
	private String userName;
	private String password;
	private int connPoolSize;
	private int connTimeout;
	private int socketTimeout;
	private int connRequestTimeout;
	private int maxAcquireTimes;

	private LinkedBlockingQueue<ElasticsearchClient> clients = new LinkedBlockingQueue<>();
	private ElasticsearchClient DEFAULT_CLIENT;


	public ESConnectPool(String ipAddress) {
		this(ipAddress, 20, 5000, 10000, 5000, 5);
	}
	public ESConnectPool(String ipAddress, int connPoolSize, int connTimeout
			, int socketTimeout, int connRequestTimeout, int maxAcquireTimes) {
		this(ipAddress, false, null, null, connPoolSize
				, connTimeout, socketTimeout, connRequestTimeout, maxAcquireTimes);
	}
	public ESConnectPool(String ipAddress, boolean needCredentials, String userName, String password
			, int connPoolSize, int connTimeout, int socketTimeout, int connRequestTimeout, int maxAcquireTimes) {
		this(ipAddress, "http", needCredentials, userName, password, connPoolSize
				, connTimeout, socketTimeout, connRequestTimeout, maxAcquireTimes);
	}
	public ESConnectPool(String ipAddress, String scheme, boolean needCredentials, String userName, String password
			, int connPoolSize, int connTimeout, int socketTimeout, int connRequestTimeout, int maxAcquireTimes) {
		this.ipAddress = ipAddress;
		this.scheme = scheme;
		this.needCredentials = needCredentials;
		this.userName = userName;
		this.password = password;
		this.connPoolSize = connPoolSize;
		this.connTimeout = connTimeout;
		this.socketTimeout = socketTimeout;
		this.connRequestTimeout = connRequestTimeout;
		this.maxAcquireTimes = maxAcquireTimes;
	}

	@Override
	public void init() {
		log.info("[开始创建ES连接，IP: {}]", ipAddress);
		List<HttpHost> hosts = Arrays.stream(ipAddress.split(","))
				.map(ipPort -> toHttpHost(ipPort, scheme))
				.filter(Objects::nonNull).collect(Collectors.toList());
		for (int i = 0; i < connPoolSize; i++) {
			try {
				clients.put(toElasticsearchClient(hosts, needCredentials, userName, password, connTimeout, socketTimeout, connRequestTimeout));
			} catch (InterruptedException e) {
				log.error("创建ES连接失败, Error: {}", e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
		DEFAULT_CLIENT = toElasticsearchClient(hosts, needCredentials, userName, password, connTimeout, socketTimeout, connRequestTimeout);
		log.info("[ES连接建立成功]");
	}

	/**
	 * @Description：获取连接资源
	 */
	@Override
	public ElasticsearchClient acquire() {
		int cnt = 0;
		long startTime = System.currentTimeMillis();
		while (clients.isEmpty()) {
			if (++cnt > this.maxAcquireTimes) {
				break;
			}
			try {
				Thread.sleep(connTimeout);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		ElasticsearchClient client = clients.poll();
		long timeCost = System.currentTimeMillis() - startTime;
		if (timeCost > 10) {
			log.info("Acquire ES Client cost {}."
					, timeCost >= 1000 ? (timeCost / 1000.0) + "s" : timeCost + "ms");
		}
		return client != null ? client : DEFAULT_CLIENT;
	}

	/**
	 * @Description：释放资源
	 */
	@Override
	public void release(ElasticsearchClient client) {
		if (client == DEFAULT_CLIENT) {
			return;
		}
		try {
			clients.put(client);
		} catch (InterruptedException e) {
			log.error("释放ES连接失败, Error: {}", e.getMessage(), e);
		}
	}
}
