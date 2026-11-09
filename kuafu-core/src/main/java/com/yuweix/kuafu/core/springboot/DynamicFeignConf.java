package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.feign.DynamicClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;


/**
 * @author yuwei
 */
public class DynamicFeignConf {
	@ConditionalOnMissingBean(DynamicClient.class)
	@Bean
	public DynamicClient dynamicClient(@Value("${kuafu.feign.client.dynamic.connect-timeout:3000}") long connectTimeout
			, @Value("${kuafu.feign.client.dynamic.read-timeout:10000}") long readTimeout
			, ApplicationContext context) {
		return new DynamicClient(context, connectTimeout, readTimeout);
	}
}
