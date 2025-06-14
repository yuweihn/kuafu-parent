package com.yuweix.kuafu.core.mq.rabbit;


import com.rabbitmq.client.Channel;
import com.yuweix.kuafu.core.JsonUtil;
import com.yuweix.kuafu.core.MdcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


/**
 * @author yuwei
 * @date 2024-08-17 13:35:35
 */
public abstract class AbstractBaseRabbitReceiver<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractBaseRabbitReceiver.class);

    protected Class<T> clz;
    @Resource
    protected RabbitSerializer rabbitSerializer;


    @SuppressWarnings("unchecked")
    public AbstractBaseRabbitReceiver() {
        this.clz = null;
        Type t = getClass().getGenericSuperclass();
        if (t instanceof ParameterizedType) {
            this.clz = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
        }
    }

    protected void handleMessage(Message message, Channel channel) {
        String body = null;
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        String spanId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        String traceId = messageProperties.getHeader(RabbitConstant.TRACE_ID_KEY);
        if (traceId == null || "".equals(traceId)) {
            traceId = spanId;
        }
        String requestId = messageProperties.getHeader(RabbitConstant.REQUEST_ID_KEY);
        if (requestId == null || "".equals(requestId)) {
            requestId = spanId;
        }
        spanId = spanId.length() <= 16 ? spanId : spanId.substring(spanId.length() - 16);
        try {
            MdcUtil.setTraceId(traceId);
            MdcUtil.setRequestId(requestId);
            MdcUtil.setSpanId(spanId);
            before(message, channel);
            log.info("Rabbit接收消息: {}", JsonUtil.toJSONString(message));
            byte[] bytes = message.getBody();
            if (bytes == null || bytes.length <= 0) {
                channel.basicAck(deliveryTag, false);
                return;
            }
            body = new String(bytes, StandardCharsets.UTF_8);
            if (body.isEmpty()) {
                channel.basicAck(deliveryTag, false);
                return;
            }
            log.info("Rabbit消息Body: {}", body);
            T t = deserialize(body);
            Object result = process(t);
            channel.basicAck(deliveryTag, false);
            log.info("Rabbit消费完成, Result: {}", JsonUtil.toJSONString(result));
        } catch (Exception ex) {
            log.error("Rabbit消费异常message: {}, Exception: {}, ex: ", body, ex.getMessage(), ex);
            handleException(message, channel, ex);
        } finally {
            after(message, channel);
            MdcUtil.removeTraceId();
            MdcUtil.removeRequestId();
            MdcUtil.removeSpanId();
        }
    }

    protected T deserialize(String str) {
        return rabbitSerializer.deserialize(str, clz);
    }

    protected abstract Object process(T t);

    protected void before(Message message, Channel channel) {

    }

    protected void after(Message message, Channel channel) {

    }

    abstract void handleException(Message message, Channel channel, Exception ex);

    public void setRabbitSerializer(RabbitSerializer rabbitSerializer) {
        this.rabbitSerializer = rabbitSerializer;
    }
}
