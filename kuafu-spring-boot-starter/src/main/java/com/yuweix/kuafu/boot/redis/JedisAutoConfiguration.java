package com.yuweix.kuafu.boot.redis;


import com.yuweix.kuafu.data.springboot.jedis.JedisConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.jedis.enabled")
@Import({JedisConf.class})
public class JedisAutoConfiguration {

}
