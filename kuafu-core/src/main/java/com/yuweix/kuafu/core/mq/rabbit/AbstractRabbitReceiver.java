package com.yuweix.kuafu.core.mq.rabbit;


import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

import java.io.IOException;


/**
 * @author yuwei
 * @date 2024-08-17 13:35:35
 */
public abstract class AbstractRabbitReceiver<T> extends AbstractBaseRabbitReceiver<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractRabbitReceiver.class);

    @RabbitHandler(isDefault = true)
    public void onMessage(Message message, Channel channel) {
        super.handleMessage(message, channel);
    }

    @Override
    protected void handleException(Message message, Channel channel, Exception ex) {
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        } catch (IOException ioe) {
            log.error("拒绝Rabbit消息异常, Error: {}", ioe.getMessage(), ioe);
            throw new RuntimeException(ioe);
        }
    }
}
