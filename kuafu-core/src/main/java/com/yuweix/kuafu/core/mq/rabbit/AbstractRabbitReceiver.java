package com.yuweix.kuafu.core.mq.rabbit;


import com.rabbitmq.client.Channel;
import com.yuweix.kuafu.core.MdcUtil;
import com.yuweix.kuafu.core.json.JsonUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.retry.RetryException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


/**
 * @author yuwei
 * @date 2024-08-17 13:35:35
 */
public abstract class AbstractRabbitReceiver<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractRabbitReceiver.class);

    protected Class<T> clz;

    @Resource
    protected RabbitSerializer rabbitSerializer;

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
            log.info("接收消息: {}", JsonUtil.toJSONString(message));
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
            log.info("body: {}", body);
            T t = deserialize(body);
            Object result = process(t);
            channel.basicAck(deliveryTag, false);
            log.info("消费完成, Result: {}", JsonUtil.toJSONString(result));
        } catch (RetryException ex) {
            log.error("消费异常message: {}, RetryException: {}", body, ex.getMessage());
            throw ex;
        } catch (Exception e) {
            log.error("消费异常message: {}, Exception: {}", body, e.getMessage());
        } finally {
            MdcUtil.removeTraceId();
            MdcUtil.removeRequestId();
            MdcUtil.removeSpanId();
            after(message, channel);
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

    public void setRabbitSerializer(RabbitSerializer rabbitSerializer) {
        this.rabbitSerializer = rabbitSerializer;
    }
}
