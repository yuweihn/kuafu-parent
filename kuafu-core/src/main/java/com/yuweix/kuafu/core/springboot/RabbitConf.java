package com.yuweix.kuafu.core.springboot;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuweix.kuafu.core.SpringContext;
import com.yuweix.kuafu.core.mq.rabbit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * @author yuwei
 */
public class RabbitConf {
    private static final Logger log = LoggerFactory.getLogger(RabbitConf.class);

    @ConditionalOnMissingBean(BindingSetting.class)
    @Bean
    @ConfigurationProperties(prefix = "kuafu.rabbit.setting", ignoreUnknownFields = true)
    public BindingSetting bindingSetting() {
        return new BindingSetting() {
            private List<Item> itemList = new ArrayList<>();

            @Override
            public List<Item> getBindings() {
                return itemList;
            }
        };
    }

    @ConditionalOnMissingBean(name = "rabbitBinding")
    @Bean("rabbitBinding")
    public Object rabbitBinding(SpringContext springContext, BindingSetting data) {
        Object obj = new Object();
        List<BindingSetting.Item> bindings = data.getBindings();
        if (bindings == null || bindings.isEmpty()) {
            return obj;
        }
        for (int i = 0, sz = bindings.size(); i < sz; i++) {
            BindingSetting.Item item = bindings.get(i);
            Queue queue = new Queue(item.getQueue(), true);
            SpringContext.register(queue.getName(), queue, true);

            String exchangeType = item.getExchangeType();
            exchangeType = exchangeType == null ? null : exchangeType.trim();
            if (exchangeType == null || "".equals(exchangeType) || ExchangeTypes.DIRECT.equals(exchangeType)) {
                Exchange exchange = new DirectExchange(item.getExchange(), true, false);
                SpringContext.register(exchange.getName(), exchange, true);

                Binding bd = BindingBuilder.bind(queue).to(exchange).with(item.getRouteKey()).noargs();
                SpringContext.register("rabbitBinding" + i, bd, true);
            } else if (ExchangeTypes.FANOUT.equals(exchangeType)) {
                FanoutExchange exchange = new FanoutExchange(item.getExchange(), true, false);
                SpringContext.register(exchange.getName(), exchange, true);

                Binding bd = BindingBuilder.bind(queue).to(exchange);
                SpringContext.register("rabbitBinding" + i, bd, true);
            } else if (ExchangeTypes.TOPIC.equals(exchangeType)) {
                Exchange exchange = new TopicExchange(item.getExchange(), true, false);
                SpringContext.register(exchange.getName(), exchange, true);

                Binding bd = BindingBuilder.bind(queue).to(exchange).with(item.getRouteKey()).noargs();
                SpringContext.register("rabbitBinding" + i, bd, true);
            } else {
                throw new RuntimeException("[exchangeType: " + exchangeType + "]不正确！");
            }
        }
        return obj;
    }

    @ConditionalOnMissingBean(name = "rabbitMessageConverter")
    @Bean("rabbitMessageConverter")
    public MessageConverter rabbitMessageConverter() {
        return new SimpleMessageConverter();
    }

    @ConditionalOnMissingBean(name = "rabbitRetryTemplate")
    @Bean("rabbitRetryTemplate")
    public RetryTemplate rabbitRetryTemplate(@Value("${kuafu.rabbit.retry.max-attempts:3}") int maxAttempts
            , @Value("${kuafu.rabbit.retry.initial-interval:3000}") long initialInterval
            , @Value("${kuafu.rabbit.retry.max-interval:5000}") long maxInterval
            , @Value("${kuafu.rabbit.retry.multiplier:2}") double multiplier) {
        RetryTemplate retryTemplate = new RetryTemplate();
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

    @ConditionalOnMissingBean(RabbitTemplate.ConfirmCallback.class)
    @Bean
    public RabbitTemplate.ConfirmCallback confirmCallback(@Value("${kuafu.rabbit.confirm.retry.maxAttempts:3}") int maxAttempts) {
        return new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if (ack) {
                    return;
                }
                log.error("Rabbit消息投递CorrelationDataId: {}, Cause: {}", correlationData.getId(), cause);
                if (!(correlationData instanceof ConfirmData)) {
                    return;
                }
                ConfirmData confirmData = (ConfirmData) correlationData;
                Confirmable confirmable = confirmData.getConfirmable();
                if (confirmable == null) {
                    return;
                }

                int times = confirmData.getRetryTimes() + 1;
                if (times > maxAttempts) {
                    log.error("Rabbit消息投递超过最大可重试次数！");
                    return;
                }
                log.info("Rabbit消息投递重试第{}次", times);
                confirmData.setRetryTimes(times);
                confirmable.confirm(confirmData);
            }
        };
    }

    @ConditionalOnMissingBean(RabbitTemplate.class)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory
            , @Qualifier("rabbitRetryTemplate") RetryTemplate retryTemplate
            , @Autowired(required = false) @Qualifier("rabbitMessageConverter") MessageConverter messageConverter
            , RabbitTemplate.ConfirmCallback confirmCallback) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        if (messageConverter != null) {
            template.setMessageConverter(messageConverter);
        }
        template.setRetryTemplate(retryTemplate);
        template.setConfirmCallback(confirmCallback);
        return template;
    }

    @ConditionalOnMissingBean(RabbitSerializer.class)
    @Bean
    public RabbitSerializer rabbitSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new RabbitSerializer() {
            @Override
            public <T> String serialize(T t) {
                try {
                    return objectMapper.writeValueAsString(t);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public <T> T deserialize(String str, Class<T> clz) {
                try {
                    return objectMapper.readValue(str, clz);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @ConditionalOnMissingBean(RabbitSender.class)
    @Bean
    public RabbitSender rabbitSender(RabbitTemplate rabbitTemplate, RabbitSerializer rabbitSerializer) {
        DefaultRabbitSender sender = new DefaultRabbitSender(rabbitTemplate, rabbitSerializer);
        return sender;
    }
}
