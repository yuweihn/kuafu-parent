package com.yuweix.kuafu.http.request;


import com.yuweix.kuafu.core.serialize.JsonUtil;
import com.yuweix.kuafu.http.CallbackResponseHandler;
import com.yuweix.kuafu.http.DefaultHttpDelete;
import com.yuweix.kuafu.http.HttpMethod;
import com.yuweix.kuafu.http.JsonParser;
import com.yuweix.kuafu.http.response.ErrorHttpResponse;
import com.yuweix.kuafu.http.response.HttpResponse;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author yuwei
 */
public abstract class AbstractHttpRequest<T extends AbstractHttpRequest<T>> implements HttpRequest {
	private static final Logger log = LoggerFactory.getLogger(AbstractHttpRequest.class);

	private HttpUriRequest httpUriRequest;
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

	protected AbstractHttpRequest() {
		this.responseTypeClass = String.class;
		this.jsonParser = DEFAULT_JSON_PARSER;
	}

	protected void setHttpUriRequest(HttpUriRequest httpUriRequest) {
		this.httpUriRequest = httpUriRequest;
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
		requestBase.setConfig(requestConfig);
		return requestBase;
	}

	protected <B>HttpResponse<B> execute0() {
		/**
		 * header
		 */
		ContentType hct = getHeaderContentType();
		if (hct != null) {
			httpUriRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, hct.toString()));
		}
		if (headerList != null && headerList.size() > 0) {
			for (Header header: headerList) {
				httpUriRequest.setHeader(header);
			}
		}
		/**
		 * cookie
		 */
		if (cookieList != null && cookieList.size() > 0) {
			String cookieStr = cookieList.stream()
					.map(c -> c.getName() + "=" + c.getValue())
					.collect(Collectors.joining(";"));
			httpUriRequest.setHeader("Cookie", cookieStr);
		}
		/**
		 * context
		 */
		HttpClientContext context = new HttpClientContext(new BasicHttpContext());
		context.setCookieStore(new BasicCookieStore());

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
			resp = httpClient.execute(httpUriRequest, handler, context);
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
}
