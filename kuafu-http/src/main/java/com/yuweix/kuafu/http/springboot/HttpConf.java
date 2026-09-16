package com.yuweix.kuafu.http.springboot;


import com.yuweix.kuafu.http.conn.CloseablePoolingHttpClientConnectionManager;
import com.yuweix.kuafu.http.ssl.TrustAllSslSocketFactory;
import com.yuweix.kuafu.http.strategy.connect.KeepAliveStrategy;
import com.yuweix.kuafu.http.strategy.redirect.NeedRedirectStrategy;
import com.yuweix.kuafu.http.strategy.retry.NeedRetryHandler;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(HttpConf.class);


    @ConditionalOnMissingBean(name = "defaultRequestConfig")
    @Bean(name = "defaultRequestConfig")
    public RequestConfig defaultRequestConfig(@Value("${kuafu.http.client.default-request-config.connect-timeout:3000}") int connectTimeout
            , @Value("${kuafu.http.client.default-request-config.socket-timeout:10000}") int socketTimeout
            , @Value("${kuafu.http.client.default-request-config.connection-request-timeout:3000}") int connectionRequestTimeout) {
        return RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();
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
    public LayeredConnectionSocketFactory sslSocketFactory(@Value("${kuafu.http.client.ssl.protocols:TLSv1.1,TLSv1.2,TLSv1.3}") String[] protocols) {
        return new TrustAllSslSocketFactory(protocols);
    }

    @ConditionalOnMissingBean(name = "httpClientConnectionManager")
    @Bean(name = "httpClientConnectionManager", destroyMethod = "shutdown")
    public HttpClientConnectionManager httpClientConnectionManager(@Value("${kuafu.http.client.pooling.max-total:200}") int maxTotal
            , @Value("${kuafu.http.client.pooling.max-per-route:20}") int maxPerRoute
            , @Value("${kuafu.http.client.pooling.idle-timeout:30000}") long idleTimeout
            , @Value("${kuafu.http.client.pooling.check-interval:5000}") long checkInterval
            , LayeredConnectionSocketFactory sslSocketFactory) {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslSocketFactory)
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .build();

        CloseablePoolingHttpClientConnectionManager cm = new CloseablePoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(maxPerRoute);
        cm.startEvictor(idleTimeout, checkInterval);
        return cm;
    }

    @ConditionalOnMissingBean(CloseableHttpClient.class)
    @Bean(name = "closeableHttpClient", destroyMethod = "close")
    public CloseableHttpClient closeableHttpClient(@Qualifier("defaultRequestConfig") RequestConfig defaultRequestConfig
            , KeepAliveStrategy keepAliveStrategy, HttpRequestRetryHandler httpRequestRetryHandler, RedirectStrategy redirectStrategy
//            , LayeredConnectionSocketFactory sslSocketFactory
            , HttpClientConnectionManager httpClientConnectionManager
            , @Autowired(required = false) @Qualifier("firstRequestInterceptorList") List<HttpRequestInterceptor> firstRequestInterceptorList
            , @Autowired(required = false) @Qualifier("lastRequestInterceptorList") List<HttpRequestInterceptor> lastRequestInterceptorList
            , @Autowired(required = false) @Qualifier("firstResponseInterceptorList") List<HttpResponseInterceptor> firstResponseInterceptorList
            , @Autowired(required = false) @Qualifier("lastResponseInterceptorList") List<HttpResponseInterceptor> lastResponseInterceptorList) {

        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .setKeepAliveStrategy(keepAliveStrategy)
                .setRetryHandler(httpRequestRetryHandler)
                .setRedirectStrategy(redirectStrategy)
//                .setSSLSocketFactory(sslSocketFactory)
                .setConnectionManager(httpClientConnectionManager)
                .setConnectionManagerShared(true);

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
