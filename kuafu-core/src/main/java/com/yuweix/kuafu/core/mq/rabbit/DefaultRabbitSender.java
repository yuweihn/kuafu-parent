package com.yuweix.kuafu.core.mq.rabbit;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;


/**
 * @author yuwei
 * @date 2024-08-17 13:36:40
 */
public class DefaultRabbitSender implements RabbitSender, Confirmable {
    private static final Logger log = LoggerFactory.getLogger(DefaultRabbitSender.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RabbitTemplate rabbitTemplate;

    public DefaultRabbitSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    public void send(String exchange, String routeKey, Object message) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setMessageId(UUID.randomUUID().toString().replace("-", ""));
            Message msg = MessageBuilder.withBody(objectMapper.writeValueAsString(message).getBytes(StandardCharsets.UTF_8))
                    .andProperties(properties).build();
            rabbitTemplate.convertAndSend(exchange, routeKey, msg, new ConfirmData(exchange, routeKey, msg, this));
        } catch (Exception e) {
            log.error("发送消息异常, Error: {}", e.getMessage(), e);
            throw new RuntimeException("发送消息异常" + e.getMessage());
        }
    }

    @Override
    public void confirm(ConfirmData confirmData) {
        rabbitTemplate.convertAndSend(confirmData.getExchange(), confirmData.getRouteKey(), confirmData.getMessage(), confirmData);
    }
}
