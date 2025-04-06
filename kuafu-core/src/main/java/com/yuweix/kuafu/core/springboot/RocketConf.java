package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.mq.rocket.DefaultRocketSender;
import com.yuweix.kuafu.core.mq.rocket.RocketSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


/**
 * @author yuwei
 */
public class RocketConf {
    @ConditionalOnMissingBean(RocketSender.class)
    @Bean
    public RocketSender rocketSender(RocketMQTemplate rocketMQTemplate) {
        DefaultRocketSender sender = new DefaultRocketSender();
        sender.setRocketMQTemplate(rocketMQTemplate);
        return sender;
    }
}
