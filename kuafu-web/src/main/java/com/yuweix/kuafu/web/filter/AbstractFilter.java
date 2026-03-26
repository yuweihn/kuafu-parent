package com.yuweix.kuafu.web.filter;


import com.yuweix.kuafu.core.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * @author yuwei
 */
public abstract class AbstractFilter<R extends HttpServletRequest, T extends HttpServletResponse> extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(AbstractFilter.class);

	private static final String DEFAULT_METHOD_PARAM = "_method";
	private static final String DEFAULT_ENCODING = Constant.ENCODING_UTF_8;
	private static final String DEFAULT_STATIC_PATH = "/static/";

	private String methodParam = DEFAULT_METHOD_PARAM;
	private String encoding = DEFAULT_ENCODING;
	private String staticPath = DEFAULT_STATIC_PATH;
	/**
	 * 跨域白名单
	 * 如果originWhiteList为空，所有origin都可访问，否则只允许规定的origin访问
	 */
	private List<String> originWhiteList = new ArrayList<>();
	private boolean allowLogRequest = true;

	/**
	 * 允许打印的header
	 * 1、如果为空，表示不打印任一header；
	 * 2、如果仅包含一个元素且为星号(*)，则打印全部header；
	 * 3、只打印指定的header，多个用逗号(,)隔开
	 */
	private List<String> logHeaders = null;
	/**
	 * 忽略的url
	 */
	private PathPattern exclusivePattern;
	/**
	 * 需打印的Request中的Attribute
	 */
	private List<String> requestAttributes = null;

	private Integer maxRequestSize = null;
	private Integer maxResponseSize = null;


	public void setMaxRequestSize(Integer maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}
	public void setMaxResponseSize(Integer maxResponseSize) {
		this.maxResponseSize = maxResponseSize;
	}
	public Integer getMaxRequestSize() {
		return maxRequestSize;
	}
	public Integer getMaxResponseSize() {
		return maxResponseSize;
	}

	public void setMethodParam(String methodParam) {
		this.methodParam = methodParam;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setStaticPath(String staticPath) {
		this.staticPath = staticPath;
	}

	public void setOriginWhiteList(List<String> originWhiteList) {
		this.originWhiteList = originWhiteList;
	}

	public void setAllowLogRequest(boolean allowLogRequest) {
		this.allowLogRequest = allowLogRequest;
	}


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			initTraceProperties(request);
			doFilter(request, response, filterChain);
		} finally {
			removeTraceProperties();
		}
	}
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (exclusivePattern != null && exclusivePattern.matches(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		long startTimeMillis = System.currentTimeMillis();
		R req = wrap(adjustMethod(request));
		T resp = wrap(response);
		Map<String, Object> logInfoMap = new LinkedHashMap<>();
		try {
            beforeFilter(req, resp);
			setCharacterEncoding(req, resp);
			setContextPath(req);
			ActionUtil.setAccessControl(req, resp, originWhiteList);
			ActionUtil.addExposeHeader(resp, Constant.HEADER_X_TRACE_ID, MdcUtil.getTraceId());

			filterChain.doFilter(req, resp);

			if (allowLogRequest) {
				Map<String, Object> map = logRequest(req);
				if (map != null && !map.isEmpty()) {
					logInfoMap.putAll(map);
				}
			}
			Map<String, Object> requestHeader = getRequestHeader(req);
			if (requestHeader != null && !requestHeader.isEmpty()) {
				logInfoMap.put("headers", requestHeader);
			}
			Object responseBody = getResponseBody(resp);
			if (responseBody != null) {
				logInfoMap.put("responseBody", responseBody);
			}
		} finally {
			afterFilter(req, resp);
		}
		long endTimeMillis = System.currentTimeMillis();
		logInfoMap.put("status", resp.getStatus());
		logInfoMap.put("timeCost", (endTimeMillis - startTimeMillis) + "ms");
		log.info("{}", JsonUtil.toString(logInfoMap));
	}

	private void initTraceProperties(HttpServletRequest request) {
		String requestId = request.getHeader(Constant.REQUEST_HEADER_X_REQUEST_ID);
		if (requestId == null || "".equals(requestId)) {
			requestId = MdcUtil.getTraceId();
		}
		if (requestId == null || "".equals(requestId)) {
			requestId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
		}
		MdcUtil.setRequestId(requestId);
	}
	private void removeTraceProperties() {
		MdcUtil.removeRequestId();
	}

	@SuppressWarnings("unchecked")
	protected R wrap(HttpServletRequest request) {
		return (R) request;
	}

	@SuppressWarnings("unchecked")
	protected T wrap(HttpServletResponse response) {
		return (T) response;
	}

	/**
	 * 浏览器不支持put、delete等method，需要将/service?_method=delete转换为标准的http delete方法
	 **/
	private HttpServletRequest adjustMethod(HttpServletRequest request) {
		if (!"post".equalsIgnoreCase(request.getMethod())) {
			return request;
		}

		String paramValue = request.getParameter(methodParam);
		if (paramValue == null || "".equals(paramValue.trim())) {
			return request;
		}

		final String method = paramValue.trim().toUpperCase(Locale.ENGLISH);
		return new HttpServletRequestWrapper(request) {
			@Override
			public String getMethod() {
				return method;
			}
		};
	}

	/**
	 * 打印请求参数
	 */
	protected Map<String, Object> logRequest(R request) {
		String url = URLDecoder.decode(request.getRequestURL().toString(), StandardCharsets.UTF_8);
        String contentType = request.getContentType();
		Map<String, String[]> params = request.getParameterMap();

		Map<String, Object> baseLogMap = new LinkedHashMap<>();
		baseLogMap.put("ip", ActionUtil.getRequestIP());
		baseLogMap.put("method", request.getMethod().toLowerCase());
		baseLogMap.put("url", url);
		if (contentType != null) {
			baseLogMap.put("contentType", contentType);
		}
		if (params != null && !params.isEmpty()) {
			baseLogMap.put("params", params);
		}
		Object bodyInfo = getRequestBody(request);
		if (bodyInfo == null || "".equals(bodyInfo)) {
			baseLogMap.put("requestBody", bodyInfo);
		}

		Map<String, Object> preLogMap = preLog(request);
		Map<String, Object> postLogMap = postLog(request);
		Map<String, Object> allLogMap = new LinkedHashMap<>();
		if (preLogMap != null) {
			allLogMap.putAll(preLogMap);
		}
		allLogMap.putAll(baseLogMap);
		if (postLogMap != null) {
			allLogMap.putAll(postLogMap);
		}

		return allLogMap;
	}

	protected Object getRequestBody(R request) {
		byte[] bytes = getRequestBodyBytes(request);
		if (bytes == null || bytes.length <= 0) {
			return null;
		}

		String content = new String(bytes, Charset.forName(getEncoding()));
		if ("".equals(content)) {
			return null;
		}
		try {
			content = URLDecoder.decode(content, getEncoding());
		} catch (Exception ignored) {
		}
		return limit(content, maxRequestSize);
	}
	protected byte[] getRequestBodyBytes(R request) {
		return null;
	}

	protected Map<String, Object> preLog(R request) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("traceId", MdcUtil.getTraceId());
		map.put("spanId", MdcUtil.getSpanId());
		return map;
	}

	protected Map<String, Object> postLog(R request) {
		if (this.requestAttributes == null || this.requestAttributes.size() <= 0) {
			return null;
		}

		Map<String, Object> map = new LinkedHashMap<>();
		for (String att : this.requestAttributes) {
			Object val = request.getAttribute(att);
			if (val == null) {
				continue;
			}
			map.put("att", val);
		}
		return map;
	}

	/**
	 * 设置字符集
	 **/
	protected void setCharacterEncoding(R request, T response) {
		try {
			request.setCharacterEncoding(encoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		response.setCharacterEncoding(encoding);
	}

	/**
	 * 将站点域名和static资源地址存入context
	 **/
	protected void setContextPath(R request) {
		ActionUtil.addContextPath(request);
		ActionUtil.addStaticPath(request, staticPath);
	}

	protected void beforeFilter(R request, T response) {

	}

	protected Map<String, Object> getRequestHeader(R request) {
		if (this.logHeaders.isEmpty()) {
			return null;
		}
		if (this.logHeaders.size() == 1 && "*".equals(this.logHeaders.get(0))) {
			Enumeration<String> headerNames = request.getHeaderNames();
			if (headerNames == null || !headerNames.hasMoreElements()) {
				return null;
			}
			Map<String, Object> map = new HashMap<>();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				String headerVal = request.getHeader(headerName);
				if (headerName != null && headerVal != null) {
					map.put(headerName, headerVal);
				}
			}
			return map;
		}
		Map<String, Object> map = new HashMap<>();
		for (String h: this.logHeaders) {
			if (h == null) {
				continue;
			}
			String val = request.getHeader(h);
			if (val == null) {
				continue;
			}
			map.put(h, val);
		}
		return map;
	}

	protected Object getResponseBody(T response) {
		return null;
	}

	protected void afterFilter(R request, T response) {

	}

	protected Object limit(String str, Integer maxSize) {
		if (maxSize == null || maxSize < 0) {
			try {
				return JsonUtil.toObject(str);
			} catch (Exception e) {
				return str;
			}
		}
		if (maxSize == 0) {
			return null;
		}
		if (str != null && str.length() > maxSize) {
			str = str.substring(0, maxSize) + "......";
		}
		try {
			return JsonUtil.toObject(str);
		} catch (Exception e) {
			return str;
		}
	}

	@Override
	public void initFilterBean() throws ServletException {
		super.initFilterBean();
		FilterConfig config = this.getFilterConfig();
		assert config != null;

		String exclusive = config.getInitParameter("exclusive");
		if (exclusive != null && !"".equals(exclusive.trim())) {
			this.exclusivePattern = new PathPattern(exclusive.split(","));
		}

		String logHeader = config.getInitParameter("logHeader");
		if (logHeader != null && !"".equals(logHeader.trim())) {
			this.logHeaders = BeanUtil.split(Collections.singletonList(logHeader), ",");
		}

		String requestAtt = config.getInitParameter("requestAtt");
		if (requestAtt != null && !"".equals(requestAtt.trim())) {
			this.requestAttributes = BeanUtil.split(Collections.singletonList(requestAtt), ",");
		}
	}
}
