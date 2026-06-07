package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.mq.rocket.DefaultRocketSender;
import com.yuweix.kuafu.core.mq.rocket.RocketRetryTemplate;
import com.yuweix.kuafu.core.mq.rocket.RocketSender;
import com.yuweix.kuafu.core.mq.rocket.RocketSerializer;
import com.yuweix.kuafu.core.serialize.JsonUtil;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;


/**
 * @author yuwei
 */
public class RocketConf {
    private static final Logger log = LoggerFactory.getLogger(RocketConf.class);


    @ConditionalOnMissingBean(RocketSerializer.class)
    @Bean
    public RocketSerializer rocketSerializer() {
        return new RocketSerializer() {
            @Override
            public <T> String serialize(T t) {
                return JsonUtil.serialize(t);
            }

            @Override
            public <T> T deserialize(String str) {
                return JsonUtil.deserialize(str);
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

        retryTemplate.registerListener(new RetryListenerSupport() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                log.error("Rocket消息处理重试，第{}次尝试，错误信息: {}", context.getRetryCount(), throwable.getMessage(), throwable);
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                if (context.getRetryCount() > 0) {
                    if (throwable != null) {
                        log.error("Rocket消息处理最终失败，共重试{}次", context.getRetryCount(), throwable);
                    } else {
                        log.info("Rocket消息处理成功，经过{}次重试", context.getRetryCount());
                    }
                }
            }
        });
        return retryTemplate;
    }

    @ConditionalOnMissingBean(RocketSender.class)
    @Bean
    public RocketSender rocketSender(RocketMQTemplate rocketMQTemplate, RocketSerializer rocketSerializer) {
        DefaultRocketSender sender = new DefaultRocketSender(rocketMQTemplate, rocketSerializer);
        return sender;
    }
}
