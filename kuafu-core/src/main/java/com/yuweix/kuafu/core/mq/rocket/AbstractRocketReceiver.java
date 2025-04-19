package com.yuweix.kuafu.core.mq.rocket;


import com.yuweix.kuafu.core.MdcUtil;
import com.yuweix.kuafu.core.json.JsonUtil;
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
    protected RocketSerializer rocketSerializer;

    @SuppressWarnings("unchecked")
    public AbstractRocketReceiver(RocketSerializer rocketSerializer) {
        this.clz = null;
        Type t = getClass().getGenericSuperclass();
        if (t instanceof ParameterizedType) {
            this.clz = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
        }
        this.rocketSerializer = rocketSerializer;
    }

    @Override
    public void onMessage(Message message) {
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
            log.info("body: {}", body);
            T t = deserialize(body);
            Object result = process(t);
            log.info("消费完成, Result: {}", JsonUtil.toJSONString(result));
        } catch (Exception e) {
            log.error("消费异常message: {}, Error: {}", JsonUtil.toJSONString(message), e.getMessage());
            throw new RuntimeException(e);
        } finally {
            MdcUtil.removeTraceId();
            MdcUtil.removeRequestId();
            MdcUtil.removeSpanId();
            after(message);
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
}
