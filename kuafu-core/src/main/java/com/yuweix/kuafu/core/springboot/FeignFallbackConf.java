package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.feign.aspect.FeignFallbackAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


/**
 * @author yuwei
 */
public class FeignFallbackConf {
	@ConditionalOnMissingBean(FeignFallbackAspect.class)
	@Bean
	public FeignFallbackAspect feignFallbackAspect() {
		return new FeignFallbackAspect();
	}
}
