package com.yuweix.kuafu.boot.exception;


import com.yuweix.kuafu.core.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.exception.enabled", matchIfMissing = true)
public class ExceptionAutoConfiguration {
	@ConditionalOnMissingBean(GlobalExceptionHandler.class)
	@Bean
	public GlobalExceptionHandler globalExceptionHandler() {
		return new GlobalExceptionHandler();
	}
}
