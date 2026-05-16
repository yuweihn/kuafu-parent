package com.yuweix.kuafu.web.springboot;


import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;


/**
 * @author yuwei
 */
public class UndertowConf implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {
	@Value("${kuafu.web.undertow.buffer-pool-direct:false}")
	private boolean bufferPoolDirect;
	@Value("${kuafu.web.undertow.buffer-pool-size:1024}")
	private int bufferPoolSize;


	@Override
	public void customize(UndertowServletWebServerFactory factory) {
		factory.addDeploymentInfoCustomizers(deploymentInfo -> {
			WebSocketDeploymentInfo deployInfo = new WebSocketDeploymentInfo();
			deployInfo.setBuffers(new DefaultByteBufferPool(bufferPoolDirect, bufferPoolSize));
			deploymentInfo.addServletContextAttribute(WebSocketDeploymentInfo.class.getName(), deployInfo);
		});
	}
}
