package com.yuweix.kuafu.core.serialize;


import com.yuweix.kuafu.core.JsonUtil;


/**
 * @author yuwei
 */
public class FastSerializer implements Serializer {
	@Override
	public <T>String serialize(T t) {
		return JsonUtil.serialize(t);
	}

	@Override
	public <T>T deserialize(String str) {
		return JsonUtil.deserialize(str);
	}
}
