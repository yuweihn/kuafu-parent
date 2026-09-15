package com.yuweix.kuafu.http.ssl;


import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;


/**
 * 1、不进行主机名验证
 * 2、不进行SSL信任验证
 * @author yuwei
 */
public class TrustAllSslSocketFactory extends SSLConnectionSocketFactory {
	private static final Logger log = LoggerFactory.getLogger(TrustAllSslSocketFactory.class);

	private static final String[] DEFAULT_SUPPORTED_PROTOCOLS = new String[] {"TLSv1.1", "TLSv1.2"};


	public TrustAllSslSocketFactory() {
		this(createSslContext(), DEFAULT_SUPPORTED_PROTOCOLS);
	}
	public TrustAllSslSocketFactory(String[] supportedProtocols) {
		this(createSslContext(), supportedProtocols);
	}
	public TrustAllSslSocketFactory(SSLContext sslContext, String[] supportedProtocols) {
		super(sslContext, supportedProtocols, null, NoopHostnameVerifier.INSTANCE);
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
