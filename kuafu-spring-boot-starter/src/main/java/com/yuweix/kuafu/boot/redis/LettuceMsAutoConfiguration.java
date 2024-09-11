package com.yuweix.kuafu.boot.redis;


import com.yuweix.kuafu.data.springboot.lettuce.LettuceMsConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.lettuce.ms.enabled")
@Import({LettuceMsConf.class})
public class LettuceMsAutoConfiguration {

}
