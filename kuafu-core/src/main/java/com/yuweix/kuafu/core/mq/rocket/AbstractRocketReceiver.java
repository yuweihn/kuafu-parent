package com.yuweix.kuafu.core.mq.rocket;


import com.yuweix.kuafu.core.MdcUtil;
import com.yuweix.kuafu.core.json.JsonUtil;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;


/**
 * @author yuwei
 **/
public abstract class AbstractRocketReceiver<T> implements RocketMQListener<MessageExt> {
    private static final Logger log = LoggerFactory.getLogger(AbstractRocketReceiver.class);

    protected Class<T> clz;

    @SuppressWarnings("unchecked")
    public AbstractRocketReceiver() {
        this.clz = null;
        Type t = getClass().getGenericSuperclass();
        if (t instanceof ParameterizedType) {
            this.clz = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
        }
    }

    @Override
    public void onMessage(MessageExt message) {
        String spanId = UUID.randomUUID().toString().replace("-", "").toLowerCase();

        String traceId = message.getUserProperty(RocketConstant.TRACE_ID_KEY);
        if (traceId == null || "".equals(traceId)) {
            traceId = spanId;
        }
        String requestId = message.getUserProperty(RocketConstant.REQUEST_ID_KEY);
        if (requestId == null || "".equals(requestId)) {
            requestId = spanId;
        }
        spanId = spanId.length() <= 16 ? spanId : spanId.substring(spanId.length() - 16);
        try {
            MdcUtil.setTraceId(traceId);
            MdcUtil.setRequestId(requestId);
            MdcUtil.setSpanId(spanId);
            String body = new String(message.getBody());
            log.info("body: {}", body);
            T t = JsonUtil.parseObject(body, clz);
            before(t);
            Object result = process(t);
            log.info("消费完成, Result: {}", JsonUtil.toJSONString(result));
            after(t);
        } catch (Exception e) {
            log.error("消费异常message: {}, Error: {}", JsonUtil.toJSONString(message), e.getMessage());
            throw new RuntimeException(e);
        } finally {
            MdcUtil.removeTraceId();
            MdcUtil.removeRequestId();
            MdcUtil.removeSpanId();
        }
    }

    protected abstract Object process(T t);

    protected void before(T t) {

    }

    protected void after(T t) {

    }
}
