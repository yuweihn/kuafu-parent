package com.yuweix.kuafu.data.cache;




/**
 * @author yuwei
 */
public interface MessageHandler {
	void handle(String channel, String message);
}
