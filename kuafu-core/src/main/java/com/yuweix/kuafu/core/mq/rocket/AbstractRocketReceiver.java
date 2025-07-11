package com.yuweix.kuafu.core.mq.rocket;


import com.yuweix.kuafu.core.JsonUtil;
import com.yuweix.kuafu.core.MdcUtil;
import jakarta.annotation.Resource;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;


/**
 * @author yuwei
 **/
public abstract class AbstractRocketReceiver<T> implements RocketMQListener<Message> {
    private static final Logger log = LoggerFactory.getLogger(AbstractRocketReceiver.class);

    protected Class<T> clz;
    @Resource
    protected RocketSerializer rocketSerializer;


    @SuppressWarnings("unchecked")
    public AbstractRocketReceiver() {
        this.clz = null;
        Type t = getClass().getGenericSuperclass();
        if (t instanceof ParameterizedType) {
            this.clz = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
        }
    }

    @Override
    public void onMessage(Message message) {
        handleMessage(message);
    }

    protected void handleMessage(Message message) {
        String spanId = UUID.randomUUID().toString().replace("-", "").toLowerCase();

        String traceId = message.getProperty(RocketConstant.TRACE_ID_KEY);
        if (traceId == null || "".equals(traceId)) {
            traceId = spanId;
        }
        String requestId = message.getProperty(RocketConstant.REQUEST_ID_KEY);
        if (requestId == null || "".equals(requestId)) {
            requestId = spanId;
        }
        spanId = spanId.length() <= 16 ? spanId : spanId.substring(spanId.length() - 16);
        try {
            MdcUtil.setTraceId(traceId);
            MdcUtil.setRequestId(requestId);
            MdcUtil.setSpanId(spanId);
            before(message);
            String body = new String(message.getBody());
            log.info("Rocket消息Body: {}", body);
            T t = deserialize(body);
            Object result = process(t);
            log.info("Rocket消费完成, Result: {}", JsonUtil.toJSONString(result));
        } catch (Exception ex) {
            log.error("Rocket消费异常message: {}, Exception: {}, ex: ", JsonUtil.toJSONString(message), ex.getMessage(), ex);
            handleException(message, ex);
        } finally {
            after(message);
            MdcUtil.removeTraceId();
            MdcUtil.removeRequestId();
            MdcUtil.removeSpanId();
        }
    }

    protected T deserialize(String str) {
        return rocketSerializer.deserialize(str, clz);
    }

    protected abstract Object process(T t);

    protected void before(Message message) {

    }

    protected void after(Message message) {

    }

    protected void handleException(Message message, Exception ex) {

    }

    public void setRocketSerializer(RocketSerializer rocketSerializer) {
        this.rocketSerializer = rocketSerializer;
    }
}
