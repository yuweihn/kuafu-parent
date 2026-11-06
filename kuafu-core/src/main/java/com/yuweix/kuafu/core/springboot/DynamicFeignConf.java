package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.feign.DynamicClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;


/**
 * @author yuwei
 */
public class DynamicFeignConf {
	@ConditionalOnMissingBean(DynamicClient.class)
	@Bean
	public DynamicClient dynamicClient(ApplicationContext context) {
		return new DynamicClient(context);
	}
}
