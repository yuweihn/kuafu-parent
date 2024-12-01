package com.yuweix.kuafu.core.mq.rabbit;


import com.rabbitmq.client.Channel;
import com.yuweix.kuafu.core.MdcUtil;
import com.yuweix.kuafu.core.json.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;


/**
 * @author yuwei
 * @date 2024-08-17 13:35:35
 */
public abstract class AbstractRabbitReceiver<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractRabbitReceiver.class);

    protected Class<T> clz;

    @SuppressWarnings("unchecked")
    public AbstractRabbitReceiver() {
        this.clz = null;
        Type t = getClass().getGenericSuperclass();
        if (t instanceof ParameterizedType) {
            this.clz = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
        }
    }

    @RabbitHandler(isDefault = true)
    public void onMessage(Message message, Channel channel) {
        String body = null;
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        String spanId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        String traceId = messageProperties.getHeader(RabbitConstant.TRACE_ID_KEY);
        if (traceId == null || "".equals(traceId)) {
            traceId = spanId;
        }
        try {
            MdcUtil.setTraceId(traceId);
            MdcUtil.setSpanId(traceId);
            log.info("接收消息: {}", JsonUtil.toJSONString(message));
            byte[] bytes = message.getBody();
            if (bytes == null || bytes.length <= 0) {
                channel.basicAck(deliveryTag, false);
                return;
            }
            body = new String(bytes);
            if (body.isEmpty()) {
                channel.basicAck(deliveryTag, false);
                return;
            }
            log.info("body: {}", body);
            before(message, channel);
            T t = JsonUtil.parseObject(body, clz);
            Object result = process(t);
            channel.basicAck(deliveryTag, false);
            log.info("消费完成, Result: {}", JsonUtil.toJSONString(result));
            after(message, channel);
        } catch (Exception e) {
            log.error("消费异常message: {}, Error: {}", body, e.getMessage());
            throw new RuntimeException(e);
        } finally {
            MdcUtil.removeTraceId();
            MdcUtil.removeSpanId();
        }
    }

    protected abstract Object process(T t);

    protected void before(Message message, Channel channel) {

    }

    protected void after(Message message, Channel channel) {

    }
}
