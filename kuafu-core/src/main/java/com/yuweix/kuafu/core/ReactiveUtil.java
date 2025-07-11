package com.yuweix.kuafu.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @author yuwei
 */
public abstract class ReactiveUtil {
	private static final Logger log = LoggerFactory.getLogger(ReactiveUtil.class);


	/**
	 * 获得客户端IP
	 * @return
	 */
	public static String getRequestIP(ServerHttpRequest request) {
		HttpHeaders headers = request.getHeaders();

		String ip = headers.getFirst("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = headers.getFirst("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = headers.getFirst("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = headers.getFirst("X-Real-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddress().toString();
		}

		if (ip == null) {
			return null;
		}
		return ip.split(",")[0];
	}

	/**
	 * 获取本机内网IP
	 */
	public static String getLocalInnerIP() {
		String innerIp = null;
		try {
			innerIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.error("获取本机内网IP失败, Error: {}", e.getMessage(), e);
		}
		return innerIp;
	}
}
