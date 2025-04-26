package com.yuweix.kuafu.core.serialize;




/**
 * @author yuwei
 */
public interface Serializer {
	<T>String serialize(T t);
	<T>T deserialize(String str);
}
