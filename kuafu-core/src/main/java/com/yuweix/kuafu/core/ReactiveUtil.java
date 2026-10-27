package com.yuweix.kuafu.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
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
		if (request == null) {
			return null;
		}
		HttpHeaders headers = request.getHeaders();
		String[] headerNames = {"X-GW-Client-IP", "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "X-Real-IP"};
		String ip = null;
		for (String header : headerNames) {
			ip = headers.getFirst(header);
			if (isValidIP(ip)) {
				break;
			}
		}

		if (!isValidIP(ip)) {
			InetSocketAddress remoteAddress = request.getRemoteAddress();
			if (remoteAddress != null && remoteAddress.getAddress() != null) {
				ip = remoteAddress.getAddress().getHostAddress();
			}
		}

		if (!isValidIP(ip)) {
			return null;
		}

		// 处理多级代理情况，取第一个IP
		String clientIP = ip.split(",")[0].trim();

		// 处理本地地址
		if (isLocalAddress(clientIP)) {
			String localIP = getLocalInnerIP();
			if (isValidIP(localIP)) {
				return localIP;
			}
		}

		return clientIP;
	}

	/**
	 * 判断IP是否有效
	 */
	private static boolean isValidIP(String ip) {
		return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
	}

	/**
	 * 判断是否为本地地址
	 */
	private static boolean isLocalAddress(String ip) {
		if (ip == null || ip.isEmpty()) {
			return true;
		}
		try {
			InetAddress address = InetAddress.getByName(ip);
			return address.isLoopbackAddress() || address.isSiteLocalAddress();
		} catch (UnknownHostException e) {
			return false;
		}
	}

	/**
	 * 获取本机内网IP
	 */
	public static String getLocalInnerIP() {
		String innerIp = null;
		try {
			innerIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException ex) {
			log.error("获取本机内网IP失败, Error: {}", ex.getMessage(), ex);
		}
		return innerIp;
	}
}
