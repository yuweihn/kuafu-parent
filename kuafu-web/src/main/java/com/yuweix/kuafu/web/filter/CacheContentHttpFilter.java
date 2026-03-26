package com.yuweix.kuafu.web.filter;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author yuwei
 */
public class CacheContentHttpFilter extends AbstractFilter<CacheBodyRequestWrapper, ContentCachingResponseWrapper> {
	@Override
	protected CacheBodyRequestWrapper wrap(HttpServletRequest request) {
		return new CacheBodyRequestWrapper(request);
	}

	@Override
	protected ContentCachingResponseWrapper wrap(HttpServletResponse response) {
		return new ContentCachingResponseWrapper(response);
	}

	@Override
	protected Map<String, Object> logRequest(CacheBodyRequestWrapper request) {
		Map<String, Object> logInfoMap = super.logRequest(request);
		Object bodyInfo = getRequestBody(request);

		if (bodyInfo == null || "".equals(bodyInfo)) {
			return logInfoMap;
		}

		if (logInfoMap == null) {
			logInfoMap = new LinkedHashMap<>();
		}
		logInfoMap.put("requestBody", bodyInfo);
		return logInfoMap;
	}

	@Override
	protected Object getResponseBody(ContentCachingResponseWrapper response) {
		String str = new String(response.getContentAsByteArray(), Charset.forName(getEncoding()));
		return limit(str, getMaxResponseSize());
	}

	protected void writeResponseBody(ContentCachingResponseWrapper response) {
		try {
			response.copyBodyToResponse();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void afterFilter(CacheBodyRequestWrapper request, ContentCachingResponseWrapper response) {
		super.afterFilter(request, response);
		this.writeResponseBody(response);
	}
}
