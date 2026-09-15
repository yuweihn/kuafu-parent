package com.yuweix.kuafu.http.strategy.retry;


import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;


/**
 * 异常恢复机制 连接失败后，可以针对相应的异常进行相应的处理措施；
 * @author yuwei
 */
public class NeedRetryHandler implements HttpRequestRetryHandler {
	private int maxRetries;


	public NeedRetryHandler() {
		this.maxRetries = 3;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		/**
		 * 如果连接次数超过××次，就不进行重复连接
		 */
		if (executionCount >= this.maxRetries) {
			return false;
		}
		/**
		 * 连接超时
		 */
		if (exception instanceof ConnectTimeoutException) {
			return true;
		}
		/**
		 * io操作中断
		 */
		if (exception instanceof InterruptedIOException) {
			return false;
		}
		/**
		 * 未找到主机
		 */
		if (exception instanceof UnknownHostException) {
			return false;
		}
		/**
		 * SSL handshake exception
		 */
		if (exception instanceof SSLException) {
			return false;
		}
		HttpClientContext clientContext = HttpClientContext.adapt(context);
		HttpRequest request = clientContext.getRequest();
		boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);

		/**
		 * Retry if the request is considered idempotent
		 */
		if (idempotent) {
			return true;
		}
		return false;
	}
}
