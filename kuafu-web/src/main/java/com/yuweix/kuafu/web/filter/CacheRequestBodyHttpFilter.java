package com.yuweix.kuafu.web.filter;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * @author yuwei
 */
public class CacheRequestBodyHttpFilter extends AbstractFilter<CacheBodyRequestWrapper, HttpServletResponse> {
	@Override
	protected CacheBodyRequestWrapper wrap(HttpServletRequest request) {
		return new CacheBodyRequestWrapper(request);
	}

	@Override
	protected byte[] getRequestBodyBytes(CacheBodyRequestWrapper request) {
		return request.getRequestBody();
	}
}
