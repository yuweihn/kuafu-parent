package com.yuweix.kuafu.core.mq.activemq.message;




/**
 * 消息发送器接口
 * @author yuwei
 */
public interface ISender {
	void send(String channel, String message);
}
