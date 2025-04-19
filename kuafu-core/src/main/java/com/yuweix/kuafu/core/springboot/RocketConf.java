package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.json.Json;
import com.yuweix.kuafu.core.mq.rocket.DefaultRocketSender;
import com.yuweix.kuafu.core.mq.rocket.RocketSender;
import com.yuweix.kuafu.core.mq.rocket.RocketSerializer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


/**
 * @author yuwei
 */
public class RocketConf {
    @ConditionalOnMissingBean(RocketSerializer.class)
    @Bean
    public RocketSerializer rocketSerializer(Json json) {
        return new RocketSerializer() {
            @Override
            public <T> String serialize(T t) {
                return json.toJSONString(t);
            }

            @Override
            public <T> T deserialize(String str, Class<T> clz) {
                return json.parseObject(str, clz);
            }
        };
    }

    @ConditionalOnMissingBean(RocketSender.class)
    @Bean
    public RocketSender rocketSender(RocketMQTemplate rocketMQTemplate, RocketSerializer rocketSerializer) {
        DefaultRocketSender sender = new DefaultRocketSender(rocketMQTemplate, rocketSerializer);
        return sender;
    }
}
