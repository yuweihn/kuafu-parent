package com.yuweix.kuafu.session.filter;


import com.yuweix.kuafu.core.json.Fastjson;
import com.yuweix.kuafu.core.json.Json;
import com.yuweix.kuafu.session.CacheHttpServletRequest;
import com.yuweix.kuafu.session.SessionAttribute;
import com.yuweix.kuafu.session.cache.SessionCache;
import com.yuweix.kuafu.session.conf.PathPattern;
import com.yuweix.kuafu.session.conf.SessionConf;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


/**
 * @author yuwei
 */
public abstract class SessionFilter implements Filter {
	/**
	 * 需要排除的URI
	 */
	private static final String EXCLUSIVE = "exclusive";


	public SessionFilter() {
		this(null);
	}
	public SessionFilter(SessionCache cache) {
		this(cache, new Fastjson());
	}
	public SessionFilter(SessionCache cache, Json json) {
		setCache(cache);
		setJson(json);
	}

	public void setCache(SessionCache cache) {
		SessionConf.getInstance().setCache(cache);
	}
	
	public void setJson(Json json) {
		if (json instanceof Fastjson) {
			Fastjson fastjson = (Fastjson) json;
			fastjson.addAccept(SessionAttribute.class.getName());
			SessionConf.getInstance().setJson(fastjson);
		} else {
			SessionConf.getInstance().setJson(json);
		}
	}
	/**
	 * 设置session失效时间(分钟)
	 */
	public void setMaxInactiveInterval(int maxInactiveInterval) {
		SessionConf.getInstance().setMaxInactiveInterval(maxInactiveInterval);
	}
	public void setApplicationName(String applicationName) {
		SessionConf.getInstance().setApplicationName(applicationName);
	}


	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		PathPattern exclusivePattern = SessionConf.getInstance().getExclusivePattern();
		if (exclusivePattern != null && exclusivePattern.matches(httpRequest)) {
			chain.doFilter(request, response);
			return;
		}

		before(httpRequest, httpResponse);
		CacheHttpServletRequest cacheRequest = new CacheHttpServletRequest(httpRequest, getSessionId(httpRequest, httpResponse));

		chain.doFilter(cacheRequest, httpResponse);
		cacheRequest.sync();
		after(cacheRequest, httpResponse);
	}

	protected abstract String getSessionId(HttpServletRequest request, HttpServletResponse response);

	protected void before(HttpServletRequest request, HttpServletResponse response) {

	}

	protected void after(HttpServletRequest request, HttpServletResponse response) {

	}

	@Override
	public void init(FilterConfig config) {
		SessionConf sessionConf = SessionConf.getInstance();

		String exclusive = config.getInitParameter(EXCLUSIVE);
		if (exclusive != null && !"".equals(exclusive.trim())) {
			sessionConf.setExclusivePattern(exclusive.split(","));
		}

		sessionConf.check();
	}

	@Override
	public void destroy() {

	}
}
