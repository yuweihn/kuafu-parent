package com.yuweix.kuafu.http.request;


import com.yuweix.kuafu.core.serialize.JsonUtil;
import com.yuweix.kuafu.http.CallbackResponseHandler;
import com.yuweix.kuafu.http.DefaultHttpDelete;
import com.yuweix.kuafu.http.HttpMethod;
import com.yuweix.kuafu.http.JsonParser;
import com.yuweix.kuafu.http.response.ErrorHttpResponse;
import com.yuweix.kuafu.http.response.HttpResponse;
import com.yuweix.kuafu.http.ssl.TrustAllSslSocketFactory;
import com.yuweix.kuafu.http.strategy.connect.KeepAliveStrategy;
import com.yuweix.kuafu.http.strategy.redirect.NeedRedirectStrategy;
import com.yuweix.kuafu.http.strategy.retry.NeedRetryHandler;
import jakarta.servlet.http.Cookie;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * @author yuwei
 */
public abstract class AbstractHttpRequest<T extends AbstractHttpRequest<T>> implements HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpRequest.class);

    private HttpRequestBase httpRequest;
    private String url;
    private HttpMethod method;
    private Class<?> responseTypeClass;
    private Type responseType;
    private List<Cookie> cookieList;
    private List<Header> headerList;
    private RequestConfig requestConfig;
    private String charset;
    private JsonParser jsonParser;
    private HttpClient httpClient;


    private static final JsonParser DEFAULT_JSON_PARSER = new JsonParser() {
        @Override
        public String toJson(Object obj) {
            return JsonUtil.toJson(obj);
        }

        @Override
        public <V> V toObject(String text, Type type) {
            return JsonUtil.toObject(text, type);
        }

        @Override
        public <V> V toObject(String text, Class<V> clz) {
            return JsonUtil.toObject(text, clz);
        }
    };

    /**
     * 默认HttpClient单例
     */
    private static final AtomicReference<CloseableHttpClient> DEFAULT_HTTP_CLIENT_REF = new AtomicReference<>();


    protected AbstractHttpRequest() {
        this.responseTypeClass = String.class;
        this.jsonParser = DEFAULT_JSON_PARSER;
    }

    protected void setHttpRequest(HttpRequestBase httpRequest) {
        this.httpRequest = httpRequest;
    }


    @SuppressWarnings("unchecked")
    public T url(String url) {
        this.url = url;
        return (T) this;
    }
    public String getUrl() {
        return url;
    }

    @SuppressWarnings("unchecked")
    public T method(HttpMethod method) {
        this.method = method;
        return (T) this;
    }
    public HttpMethod getMethod() {
        return method;
    }

    @SuppressWarnings("unchecked")
    public T responseType(Class<?> responseTypeClass) {
        this.responseTypeClass = responseTypeClass;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T responseType(Type responseType) {
        this.responseType = responseType;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T cookieList(List<Cookie> cookieList) {
        this.cookieList = cookieList;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T headerList(List<Header> headerList) {
        this.headerList = headerList;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T requestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T charset(String charset) {
        this.charset = charset;
        return (T) this;
    }
    public String getCharset() {
        return charset;
    }

    @SuppressWarnings("unchecked")
    public T jsonParser(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return (T) this;
    }

    protected ContentType getHeaderContentType() {
        return null;
    }

    protected HttpEntityEnclosingRequestBase getRequestBase() {
        HttpEntityEnclosingRequestBase requestBase = null;
        if (HttpMethod.POST.equals(method)) {
            requestBase = new HttpPost(url);
        } else if (HttpMethod.PUT.equals(method)) {
            requestBase = new HttpPut(url);
        } else if (HttpMethod.DELETE.equals(method)) {
            requestBase = new DefaultHttpDelete(url);
        } else {
            requestBase = new HttpPost(url);
        }
        return requestBase;
    }

    protected <B>HttpResponse<B> doExecute() {
        if (requestConfig != null) {
            httpRequest.setConfig(requestConfig);
        }
        /**
         * header
         */
        ContentType hct = getHeaderContentType();
        if (hct != null) {
            httpRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, hct.toString()));
        }
        if (headerList != null && headerList.size() > 0) {
            for (Header header: headerList) {
                httpRequest.setHeader(header);
            }
        }
        /**
         * cookie
         */
        if (cookieList != null && cookieList.size() > 0) {
            String cookieStr = cookieList.stream()
                    .map(c -> c.getName() + "=" + c.getValue())
                    .collect(Collectors.joining(";"));
            httpRequest.setHeader("Cookie", cookieStr);
        }
        /**
         * context
         */
        HttpClientContext context = new HttpClientContext(new BasicHttpContext());
        context.setCookieStore(new BasicCookieStore());

        HttpClient client = this.httpClient;
        if (client == null) {
            client = getDefaultHttpClient();
        }

        CallbackResponseHandler<B> handler = CallbackResponseHandler.<B>create()
                .responseType(responseTypeClass)
                .responseType(responseType)
                .context(context)
                .charset(charset)
                .jsonParser(jsonParser);
        HttpResponse<B> resp = null;
        long startTime = System.currentTimeMillis();
        try {
            log.info("Http请求开始, url: {}, method: {}", url, method);
            resp = client.execute(httpRequest, handler, context);
            return resp;
        } catch (Exception ex) {
            log.error("HttpClient.execute失败, Error: {}", ex.getMessage(), ex);
            return new ErrorHttpResponse<>(HttpStatus.SC_INTERNAL_SERVER_ERROR, ex.toString());
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("Http请求结束, url: {}, method: {}, status: {}, body: {}, 耗时: {}ms", url, method
                    , resp == null ? "" : resp.getStatus(), resp == null || resp.getBody() == null ? "" : jsonParser.toJson(resp.getBody())
                    , endTime - startTime);
        }
    }

    /**
     * 获取默认的HttpClient（单例）
     */
    private static CloseableHttpClient getDefaultHttpClient() {
        CloseableHttpClient client = DEFAULT_HTTP_CLIENT_REF.get();
        if (client != null) {
            return client;
        }
        synchronized (AbstractHttpRequest.class) {
            client = DEFAULT_HTTP_CLIENT_REF.get();
            if (client != null) {
                return client;
            }
            client = createDefaultHttpClient();
            DEFAULT_HTTP_CLIENT_REF.set(client);
            return client;
        }
    }
    private static CloseableHttpClient createDefaultHttpClient() {
        log.info("创建默认的HttpClient开始");
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(3000)
                .setConnectTimeout(3000)
                .setSocketTimeout(10000)
                .build();

        KeepAliveStrategy keepAliveStrategy = new KeepAliveStrategy();
        HttpRequestRetryHandler retryHandler = new NeedRetryHandler(3);
        RedirectStrategy redirectStrategy = new NeedRedirectStrategy();

        LayeredConnectionSocketFactory sslSocketFactory = new TrustAllSslSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslSocketFactory)
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(50);
        connectionManager.setValidateAfterInactivity(5000);

        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .setKeepAliveStrategy(keepAliveStrategy)
                .setRetryHandler(retryHandler)
                .setRedirectStrategy(redirectStrategy)
                .setConnectionManager(connectionManager)
                .evictExpiredConnections()
                .evictIdleConnections(30000, TimeUnit.MILLISECONDS);
        CloseableHttpClient httpClient = builder.build();
        log.info("创建默认的HttpClient结束");
        return httpClient;
    }

    public static void shutdownDefaultHttpClient() {
        CloseableHttpClient client = DEFAULT_HTTP_CLIENT_REF.getAndSet(null);
        if (client != null) {
            try {
                client.close();
            } catch (Exception ex) {
                log.error("关闭默认HttpClient失败, Error: {}", ex.getMessage(), ex);
            }
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(AbstractHttpRequest::shutdownDefaultHttpClient));
    }
}
