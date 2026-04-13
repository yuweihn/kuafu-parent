package com.yuweix.kuafu.data.es.common;




/**
 * @author yuwei
 */
public interface Lock {
	/**
	 * @param timeout 单位：秒。
	 */
	boolean lock(String key, String owner, long timeout);
	boolean unlock(String key, String owner);
}
