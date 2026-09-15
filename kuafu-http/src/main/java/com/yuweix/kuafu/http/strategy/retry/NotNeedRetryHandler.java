package com.yuweix.kuafu.http.strategy.retry;


import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;


/**
 * 不重试
 * @author yuwei
 */
public class NotNeedRetryHandler implements HttpRequestRetryHandler {
	public NotNeedRetryHandler() {

	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		return false;
	}
}
