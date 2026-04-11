package com.yuweix.kuafu.boot.feign;


import com.yuweix.kuafu.core.springboot.FeignFallbackConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.feign.fallback.enabled")
@Import({FeignFallbackConf.class})
public class FeignFallbackAutoConfiguration {

}
