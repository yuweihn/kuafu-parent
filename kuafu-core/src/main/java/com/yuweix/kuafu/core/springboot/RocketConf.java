package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.JsonUtil;
import com.yuweix.kuafu.core.mq.rocket.DefaultRocketSender;
import com.yuweix.kuafu.core.mq.rocket.RocketRetryTemplate;
import com.yuweix.kuafu.core.mq.rocket.RocketSender;
import com.yuweix.kuafu.core.mq.rocket.RocketSerializer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;


/**
 * @author yuwei
 */
public class RocketConf {
    @ConditionalOnMissingBean(RocketSerializer.class)
    @Bean
    public RocketSerializer rocketSerializer() {
        return new RocketSerializer() {
            @Override
            public <T> String serialize(T t) {
                return JsonUtil.toString(t);
            }

            @Override
            public <T> T deserialize(String str, Class<T> clz) {
                return JsonUtil.toObject(str, clz);
            }
        };
    }

    @ConditionalOnMissingBean(RocketRetryTemplate.class)
    @Bean
    public RocketRetryTemplate rocketRetryTemplate(@Value("${kuafu.rocket.retry.max-attempts:3}") int maxAttempts
            , @Value("${kuafu.rocket.retry.initial-interval:3000}") long initialInterval
            , @Value("${kuafu.rocket.retry.max-interval:5000}") long maxInterval
            , @Value("${kuafu.rocket.retry.multiplier:2}") double multiplier) {
        RocketRetryTemplate retryTemplate = new RocketRetryTemplate();
        /**
         * 设置重试策略
         */
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);

        /**
         * 设置退避策略
         */
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMaxInterval(maxInterval);
        backOffPolicy.setMultiplier(multiplier);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

    @ConditionalOnMissingBean(RocketSender.class)
    @Bean
    public RocketSender rocketSender(RocketMQTemplate rocketMQTemplate, RocketSerializer rocketSerializer) {
        DefaultRocketSender sender = new DefaultRocketSender(rocketMQTemplate, rocketSerializer);
        return sender;
    }
}
