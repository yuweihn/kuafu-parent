package com.yuweix.kuafu.http.springboot;


import com.yuweix.kuafu.http.ssl.TrustAllSslSocketFactory;
import com.yuweix.kuafu.http.strategy.connect.KeepAliveStrategy;
import com.yuweix.kuafu.http.strategy.redirect.NeedRedirectStrategy;
import com.yuweix.kuafu.http.strategy.retry.NeedRetryHandler;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;


/**
 * @author yuwei
 */
public class HttpConf {
	@ConditionalOnMissingBean(name = "defaultRequestConfig")
	@Bean(name = "defaultRequestConfig")
	public RequestConfig defaultRequestConfig(@Value("${kuafu.http.client.default-request-config.connect-timeout:3000}") int connectTimeout
			, @Value("${kuafu.http.client.default-request-config.socket-timeout:5000}") int socketTimeout) {
		return RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();
	}

	@ConditionalOnMissingBean(KeepAliveStrategy.class)
	@Bean
	public KeepAliveStrategy keepAliveStrategy() {
		return new KeepAliveStrategy();
	}

	@ConditionalOnMissingBean(name = "httpRequestRetryHandler")
	@Bean
	public HttpRequestRetryHandler httpRequestRetryHandler(@Value("${kuafu.http.client.retry.max-retries:3}") int maxRetries) {
		NeedRetryHandler retryHandler = new NeedRetryHandler();
		retryHandler.setMaxRetries(maxRetries);
		return retryHandler;
	}

	@ConditionalOnMissingBean(name = "redirectStrategy")
	@Bean
	public RedirectStrategy redirectStrategy() {
		return new NeedRedirectStrategy();
	}

	@ConditionalOnMissingBean(name = "sslSocketFactory")
	@Bean
	public LayeredConnectionSocketFactory sslSocketFactory() {
		return new TrustAllSslSocketFactory(new String[] {"TLSv1.1", "TLSv1.2"});
	}

	@ConditionalOnMissingBean(CloseableHttpClient.class)
	@Bean(name = "closeableHttpClient")
	public CloseableHttpClient closeableHttpClient(@Qualifier("defaultRequestConfig") RequestConfig defaultRequestConfig
			, KeepAliveStrategy keepAliveStrategy, HttpRequestRetryHandler httpRequestRetryHandler, RedirectStrategy redirectStrategy
			, LayeredConnectionSocketFactory sslSocketFactory
			, @Autowired(required = false) @Qualifier("firstRequestInterceptorList") List<HttpRequestInterceptor> firstRequestInterceptorList
			, @Autowired(required = false) @Qualifier("lastRequestInterceptorList") List<HttpRequestInterceptor> lastRequestInterceptorList
			, @Autowired(required = false) @Qualifier("firstResponseInterceptorList") List<HttpResponseInterceptor> firstResponseInterceptorList
			, @Autowired(required = false) @Qualifier("lastResponseInterceptorList") List<HttpResponseInterceptor> lastResponseInterceptorList) {

		HttpClientBuilder builder = HttpClients.custom()
												.setDefaultRequestConfig(defaultRequestConfig)
												.setKeepAliveStrategy(keepAliveStrategy)
												.setRetryHandler(httpRequestRetryHandler)
												.setRedirectStrategy(redirectStrategy)
												.setSSLSocketFactory(sslSocketFactory);

		/**
		 * add first http request interceptor list
		 */
		if (firstRequestInterceptorList != null && firstRequestInterceptorList.size() > 0) {
			for (HttpRequestInterceptor interceptor: firstRequestInterceptorList) {
				builder.addInterceptorFirst(interceptor);
			}
		}

		/**
		 * add last http request interceptor list
		 */
		if (lastRequestInterceptorList != null && lastRequestInterceptorList.size() > 0) {
			for (HttpRequestInterceptor interceptor: lastRequestInterceptorList) {
				builder.addInterceptorLast(interceptor);
			}
		}

		/**
		 * add first http response interceptor list
		 */
		if (firstResponseInterceptorList != null && firstResponseInterceptorList.size() > 0) {
			for (HttpResponseInterceptor interceptor: firstResponseInterceptorList) {
				builder.addInterceptorFirst(interceptor);
			}
		}

		/**
		 * add last http response interceptor list
		 */
		if (lastResponseInterceptorList != null && lastResponseInterceptorList.size() > 0) {
			for (HttpResponseInterceptor interceptor: lastResponseInterceptorList) {
				builder.addInterceptorLast(interceptor);
			}
		}

		return builder.build();
	}
}
