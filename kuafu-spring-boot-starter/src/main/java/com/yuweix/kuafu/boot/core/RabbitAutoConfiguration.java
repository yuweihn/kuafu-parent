package com.yuweix.kuafu.boot.core;


import com.yuweix.kuafu.core.springboot.DefaultConf;
import com.yuweix.kuafu.core.springboot.RabbitConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.mq.rabbit.enabled")
@Import({DefaultConf.class, RabbitConf.class})
public class RabbitAutoConfiguration {

}
