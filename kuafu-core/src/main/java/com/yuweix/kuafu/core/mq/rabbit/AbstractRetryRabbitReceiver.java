package com.yuweix.kuafu.core.mq.rabbit;


import com.rabbitmq.client.Channel;
import com.yuweix.kuafu.core.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;


/**
 * @author yuwei
 * @date 2024-08-17 13:35:35
 */
public abstract class AbstractRetryRabbitReceiver<T> extends AbstractBaseRabbitReceiver<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractRetryRabbitReceiver.class);

    private volatile RetryTemplate retryTemplate;
    private final Object LOCK = new Object();


    @RabbitHandler(isDefault = true)
    public void onMessage(Message message, Channel channel) {
        getRetryTemplate().execute(retryCallback -> {
            super.handleMessage(message, channel);
            return null;
        }, recoveryCallback -> {
            recover(message, channel);
            return null;
        });
    }

    @Override
    protected void handleException(Message message, Channel channel, Exception ex) {
        throw new RuntimeException(ex);
    }

    protected void recover(Message message, Channel channel) {
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        } catch (IOException ioe) {
            log.error("拒绝Rabbit消息异常, Error: {}", ioe.getMessage(), ioe);
            throw new RuntimeException(ioe);
        }
    }

    public void setRetryTemplate(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    protected RetryTemplate getRetryTemplate() {
        if (retryTemplate != null) {
            return retryTemplate;
        }
        synchronized (LOCK) {
            if (retryTemplate == null) {
                retryTemplate = SpringContext.getBean(RabbitRetryTemplate.class);
            }
        }
        return retryTemplate;
    }
}
