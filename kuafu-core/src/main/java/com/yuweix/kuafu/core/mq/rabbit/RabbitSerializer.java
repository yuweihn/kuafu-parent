package com.yuweix.kuafu.core.mq.rabbit;




/**
 * @author yuwei
 */
public interface RabbitSerializer {
	<T>String serialize(T t);
	<T>T deserialize(String str, Class<T> clz);
}
