package com.yuweix.kuafu.data.es.common;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class SingleESConnector extends AbstractESConnector {
	private static final Logger log = LoggerFactory.getLogger(SingleESConnector.class);

	/**
	 * 172.31.28.171:9200,172.31.28.172:9200,172.31.28.173:9200,172.31.28.174:9200
	 */
	private String ipAddress;
	private String scheme;
	private boolean needCredentials = false;
	private String userName;
	private String password;
	private int connTimeout;
	private int socketTimeout;
	private int connRequestTimeout;

	private ElasticsearchClient CLIENT;


	public SingleESConnector(String ipAddress) {
		this(ipAddress, 5000, 10000, 5000);
	}
	public SingleESConnector(String ipAddress, int connTimeout, int socketTimeout, int connRequestTimeout) {
		this(ipAddress, false, null, null, connTimeout, socketTimeout, connRequestTimeout);
	}
	public SingleESConnector(String ipAddress, boolean needCredentials, String userName, String password
			, int connTimeout, int socketTimeout, int connRequestTimeout) {
		this(ipAddress, "http", needCredentials, userName, password, connTimeout, socketTimeout, connRequestTimeout);
	}
	public SingleESConnector(String ipAddress, String scheme, boolean needCredentials, String userName, String password
			, int connTimeout, int socketTimeout, int connRequestTimeout) {
		this.ipAddress = ipAddress;
		this.scheme = scheme;
		this.needCredentials = needCredentials;
		this.userName = userName;
		this.password = password;
		this.connTimeout = connTimeout;
		this.socketTimeout = socketTimeout;
		this.connRequestTimeout = connRequestTimeout;
	}

	@Override
	public void init() {
		log.info("[开始创建ES连接，IP: {}]", ipAddress);
		List<HttpHost> hosts = Arrays.stream(ipAddress.split(","))
				.map(ipPort -> toHttpHost(ipPort, scheme))
				.filter(Objects::nonNull).collect(Collectors.toList());
		CLIENT = toElasticsearchClient(hosts, needCredentials, userName, password, connTimeout, socketTimeout, connRequestTimeout);
		log.info("[ES连接建立成功，IP: {}]", ipAddress);
	}

	@Override
	public ElasticsearchClient acquire() {
		return CLIENT;
	}

	@Override
	public void release(ElasticsearchClient client) {

	}
}
