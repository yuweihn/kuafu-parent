package com.yuweix.kuafu.core.mq.activemq.message;




/**
 * 消息接收器接口
 * @author yuwei
 */
public interface IReceiver {
	void receive(String channel, String message);
}
