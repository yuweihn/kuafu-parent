package com.yuweix.kuafu.core.mq.rabbit;


/**
 * @author yuwei
 * @date 2024-08-17 13:36:40
 */
public interface RabbitSender {
    void send(String exchange, Object message);
    void send(String exchange, String routeKey, Object message);
}
