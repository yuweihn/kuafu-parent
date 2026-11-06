package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.feign.DynamicClient;
import com.yuweix.kuafu.core.feign.DynamicFeignClientFactory;
import com.yuweix.kuafu.core.feign.DynamicService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;


/**
 * @author yuwei
 */
public class DynamicFeignConf {
	@ConditionalOnMissingBean(DynamicFeignClientFactory.class)
	@Bean
	public DynamicFeignClientFactory<DynamicService> dynamicFeignClientFactory(ApplicationContext appContext) {
		return new DynamicFeignClientFactory<>(appContext);
	}

	@ConditionalOnMissingBean(DynamicClient.class)
	@Bean
	public DynamicClient dynamicClient(DynamicFeignClientFactory<DynamicService> dynamicFeignClientFactory) {
		return new DynamicClient(dynamicFeignClientFactory);
	}
}
