package com.yuweix.kuafu.core.mq.rocket;




/**
 * @author yuwei
 */
public interface RocketSerializer {
	<T>String serialize(T t);
	<T>T deserialize(String str, Class<T> clz);
}
