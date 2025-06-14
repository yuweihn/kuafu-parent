package com.yuweix.kuafu.core.mq.rabbit;


import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.retry.support.RetryTemplate;

import javax.annotation.Resource;


/**
 * @author yuwei
 * @date 2024-08-17 13:35:35
 */
public abstract class AbstractRetryRabbitReceiver<T> extends AbstractBaseRabbitReceiver<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractRetryRabbitReceiver.class);

    @Resource
    protected RetryTemplate retryTemplate;


    @RabbitHandler(isDefault = true)
    public void onMessage(Message message, Channel channel) {
        retryTemplate.execute(retryCallback -> {
            super.handleMessage(message, channel);
            return null;
        }, recoveryCallback -> {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            return null;
        });
    }
}
