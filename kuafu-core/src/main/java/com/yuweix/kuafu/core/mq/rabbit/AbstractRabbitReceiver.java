package com.yuweix.kuafu.core.mq.rabbit;


import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;


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
}
