package com.yuweix.kuafu.core.mq.rocket;


/**
 * @author yuwei
 **/
public interface RocketSender {
    <T>void send(String topic, T payload);
}
