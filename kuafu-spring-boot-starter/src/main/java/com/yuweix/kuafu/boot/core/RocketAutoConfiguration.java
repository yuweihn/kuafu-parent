package com.yuweix.kuafu.boot.core;


import com.yuweix.kuafu.core.springboot.DefaultConf;
import com.yuweix.kuafu.core.springboot.RocketConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.mq.rocket.enabled")
@Import({DefaultConf.class, RocketConf.class})
public class RocketAutoConfiguration {

}
