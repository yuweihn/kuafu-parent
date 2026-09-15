package com.yuweix.kuafu.http.strategy.redirect;


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;


/**
 * 重定向策略(不重定向)
 * @author yuwei
 */
public class NotNeedRedirectStrategy implements RedirectStrategy {
	public NotNeedRedirectStrategy() {
		super();
	}

	public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
		return false;
	}

	@Override
	public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
		return null;
	}
}
