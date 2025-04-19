package com.yuweix.kuafu.core.json;




/**
 * @author yuwei
 */
public interface Json {
	<T>String serialize(T t);
	<T>T deserialize(String str);
	String toJSONString(Object object);
	Object parse(String text);
	<T>T parseObject(String text, Class<T> clz);
}
