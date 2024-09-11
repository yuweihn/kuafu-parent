package com.yuweix.kuafu.http;





/**
 * @author yuwei
 */
public interface Decoder<T extends Decoder<T>> {
	T decode(String str);
}
