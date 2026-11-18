package com.yuweix.kuafu.http.ssl;


import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.*;


/**
 * 1、不进行主机名验证
 * 2、不进行SSL信任验证
 * @author yuwei
 */
public class TrustAllSslSocketFactory extends SSLConnectionSocketFactory {
	private static final Logger log = LoggerFactory.getLogger(TrustAllSslSocketFactory.class);

	private static final String[] DEFAULT_SUPPORTED_PROTOCOLS = new String[] {"TLSv1.1", "TLSv1.2", "TLSv1.3"};


	public TrustAllSslSocketFactory() {
		this(createSslContext(), DEFAULT_SUPPORTED_PROTOCOLS);
	}
	public TrustAllSslSocketFactory(String[] supportedProtocols) {
		this(createSslContext(), supportedProtocols);
	}
	public TrustAllSslSocketFactory(SSLContext sslContext, String[] supportedProtocols) {
		super(sslContext
				, filterSupportedProtocols(sslContext
						, supportedProtocols == null || supportedProtocols.length == 0 ? DEFAULT_SUPPORTED_PROTOCOLS : supportedProtocols)
				, null, NoopHostnameVerifier.INSTANCE);
	}

	/**
	 * 只保留当前 JVM 的 SSLContext 真正支持的协议。
	 * 低版本 JDK（如 8u202）不认识 TLSv1.3，直接传给 {@code setEnabledProtocols} 会抛
	 * {@code IllegalArgumentException: TLSv1.3}，导致 HTTPS 请求失败。这里按运行时能力过滤，
	 * 既兼容老 JDK，也不影响新 JDK 使用 TLSv1.3。
	 */
	private static String[] filterSupportedProtocols(SSLContext sslContext, String[] protocols) {
		if (protocols == null || protocols.length == 0) {
			return protocols;
		}
		Set<String> jvmSupported = new HashSet<>();
		if (sslContext != null) {
			jvmSupported.addAll(Arrays.asList(sslContext.getSupportedSSLParameters().getProtocols()));
		}
		if (jvmSupported.isEmpty()) {
			log.warn("无法获取JVM支持的SSL协议列表，跳过协议过滤");
			return protocols;
		}

		List<String> filtered = new ArrayList<>(protocols.length);
		for (String protocol: protocols) {
			if (protocol == null) {
				continue;
			}
			if (jvmSupported.contains(protocol)) {
				filtered.add(protocol);
			} else {
				log.warn("当前JVM不支持SSL协议 {}, 已忽略", protocol);
			}
		}
		if (filtered.isEmpty()) {
			log.warn("过滤后没有任何可用的SSL协议，回退到原始配置: {}", Arrays.toString(protocols));
			return protocols;
		}
		return filtered.toArray(new String[filtered.size()]);
	}

	/**
	 * 不对主机名进行验证
	 */
	private static SSLContext createSslContext() {
		try {
			return new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) {
					return true;
				}
			}).build();
		} catch (Exception ex) {
			log.error("创建SSLContext失败, Error: {}", ex.getMessage(), ex);
			return null;
		}
	}
}
