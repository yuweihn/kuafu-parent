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
            String queueName = item.getQueue();
            String deadLetterQueueName = ("dlq_" + queueName).toLowerCase();

            String exchangeName = item.getExchange();
            String deadLetterExchangeName = ("dle_" + exchangeName).toLowerCase();

            String routeKey = item.getRouteKey();
            String deadLetterRouteKey = routeKey == null ? null :("dlk_" + routeKey).toLowerCase();

            String exchangeType = item.getExchangeType();
            exchangeType = exchangeType == null ? null : exchangeType.trim();

            Queue deadLetterQueue = new Queue(deadLetterQueueName, true);
            SpringContext.register(deadLetterQueueName, deadLetterQueue, true);
            Queue queue = QueueBuilder.durable(queueName)
                    .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                    .withArgument("x-dead-letter-routing-key", deadLetterRouteKey)
                    .build();
            SpringContext.register(queue.getName(), queue, true);

            Exchange exchange;
            Exchange deadLetterExchange;
            if (exchangeType == null || ExchangeTypes.DIRECT.equals(exchangeType)) {
                exchange = new DirectExchange(exchangeName, true, false);
                deadLetterExchange = new DirectExchange(deadLetterExchangeName, true, false);
            } else if (ExchangeTypes.FANOUT.equals(exchangeType)) {
                exchange = new FanoutExchange(exchangeName, true, false);
                deadLetterExchange = new FanoutExchange(deadLetterExchangeName, true, false);
            } else if (ExchangeTypes.TOPIC.equals(exchangeType)) {
                exchange = new TopicExchange(exchangeName, true, false);
                deadLetterExchange = new TopicExchange(deadLetterExchangeName, true, false);
            } else {
                throw new IllegalArgumentException("Invalid exchangeType: " + exchangeType);
            }

            SpringContext.register(exchange.getName(), exchange, true);
            Binding bd = BindingBuilder.bind(queue).to(exchange).with(routeKey).noargs();
            SpringContext.register("rabbitBinding" + i, bd, true);

            SpringContext.register(deadLetterExchange.getName(), deadLetterExchange, true);
            Binding bdDeadLetter = BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(deadLetterRouteKey).noargs();
            SpringContext.register("rabbitBindingDeadLetter" + i, bdDeadLetter, true);
        }
        return obj;
    }

    @ConditionalOnMissingBean(name = "rabbitMessageConverter")
    @Bean("rabbitMessageConverter")
    public MessageConverter rabbitMessageConverter() {
        return new SimpleMessageConverter();
    }

    @ConditionalOnMissingBean(RabbitRetryTemplate.class)
    @Bean
    public RabbitRetryTemplate rabbitRetryTemplate(@Value("${kuafu.rabbit.retry.max-attempts:3}") int maxAttempts
            , @Value("${kuafu.rabbit.retry.initial-interval:3000}") long initialInterval
            , @Value("${kuafu.rabbit.retry.max-interval:5000}") long maxInterval
            , @Value("${kuafu.rabbit.retry.multiplier:2}") double multiplier) {
        RabbitRetryTemplate retryTemplate = new RabbitRetryTemplate();
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
    public RabbitTemplate.ConfirmCallback confirmCallback(@Value("${kuafu.rabbit.confirm.retry.max-attempts:3}") int maxAttempts) {
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
            , RabbitRetryTemplate rabbitRetryTemplate
            , @Autowired(required = false) @Qualifier("rabbitMessageConverter") MessageConverter messageConverter
            , RabbitTemplate.ConfirmCallback confirmCallback) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        if (messageConverter != null) {
            template.setMessageConverter(messageConverter);
        }
        template.setRetryTemplate(rabbitRetryTemplate);
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
