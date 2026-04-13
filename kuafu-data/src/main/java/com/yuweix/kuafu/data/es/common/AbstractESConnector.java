package com.yuweix.kuafu.data.es.common;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * @author yuwei
 */
public abstract class AbstractESConnector implements ESConnector {
	private static final Logger log = LoggerFactory.getLogger(AbstractESConnector.class);

	@PostConstruct
	public void init() {

	}
	@PreDestroy
	public void destroy() {

	}

	protected HttpHost toHttpHost(String ipPort, String scheme) {
		String[] address = ipPort.split(":");
		if (address.length != 2) {
			return null;
		}
		String ip = address[0];
		int port = Integer.parseInt(address[1]);
		return new HttpHost(ip, port, scheme);
	}

	protected ElasticsearchClient toElasticsearchClient(List<HttpHost> hosts, boolean needCredentials, String userName, String password
			, int connTimeout, int socketTimeout, int connRequestTimeout) {
		RestClient restClient = RestClient.builder(hosts.toArray(new HttpHost[0]))
				.setHttpClientConfigCallback(httpClientBuilder -> {
					RequestConfig.Builder confBuilder = RequestConfig.custom();
					confBuilder.setConnectTimeout(connTimeout);
					confBuilder.setSocketTimeout(socketTimeout);
					confBuilder.setConnectionRequestTimeout(connRequestTimeout);
					httpClientBuilder.addInterceptorLast((HttpResponseInterceptor) (response, context) -> {
						if (!response.containsHeader("X-Elastic-Product")) {
							response.addHeader("X-Elastic-Product", "Elasticsearch");
						}
					});
					httpClientBuilder.setDefaultRequestConfig(confBuilder.build());
					if (needCredentials) {
						BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
						credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
						httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
					}
					return httpClientBuilder;
				}).setFailureListener(new RestClient.FailureListener() {
					@Override
					public void onFailure(Node node) {
						log.error("ES节点故障, Node: {}", node);
					}
				}).build();
		ElasticsearchTransport transport = new AgsRestClientTransport(restClient, new JacksonJsonpMapper());
		return new ElasticsearchClient(transport);
	}
}
