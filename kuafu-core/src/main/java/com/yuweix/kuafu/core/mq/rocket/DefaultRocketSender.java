package com.yuweix.kuafu.core.mq.rocket;


import com.yuweix.kuafu.core.MdcUtil;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwei
 **/
public class DefaultRocketSender implements RocketSender {
    private RocketMQTemplate rocketMQTemplate;
    private RocketSerializer rocketSerializer;

    public DefaultRocketSender(RocketMQTemplate rocketMQTemplate, RocketSerializer rocketSerializer) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.rocketSerializer = rocketSerializer;
    }


    @Override
    public <T>void send(String topic, T message) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(RocketConstant.TRACE_ID_KEY, MdcUtil.getTraceId());
        headers.put(RocketConstant.REQUEST_ID_KEY, MdcUtil.getRequestId());
        preHandle(headers);

        String payload = serialize(message);
        rocketMQTemplate.convertAndSend(topic, payload, headers);
    }

    protected void preHandle(Map<String, Object> headers) {

    }

    protected String serialize(Object obj) {
        return rocketSerializer.serialize(obj);
    }
}
