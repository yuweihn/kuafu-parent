package com.yuweix.kuafu.boot.redis;


import com.yuweix.kuafu.data.springboot.jedis.JedisMsConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.jedis.ms.enabled")
@Import({JedisMsConf.class})
public class JedisMsAutoConfiguration {

}
