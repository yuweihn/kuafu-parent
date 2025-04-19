package com.yuweix.kuafu.data.serializer;




/**
 * @author yuwei
 */
public interface CacheSerializer {
	<T>String serialize(T t);
	<T>T deserialize(String str);
}
