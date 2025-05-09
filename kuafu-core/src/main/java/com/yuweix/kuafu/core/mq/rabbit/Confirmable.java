package com.yuweix.kuafu.core.mq.rabbit;


/**
 * @author yuwei
 * @date 2024-08-17
 */
public interface Confirmable {
    /**
     * 生产者重发消息给交换机
     */
    void confirm(ConfirmData confirmData);
}
