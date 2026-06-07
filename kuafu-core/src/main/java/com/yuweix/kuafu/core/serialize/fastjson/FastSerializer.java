package com.yuweix.kuafu.core.serialize.fastjson;


import com.yuweix.kuafu.core.serialize.JsonUtil;
import com.yuweix.kuafu.core.serialize.Serializer;


/**
 * @author yuwei
 */
public class FastSerializer implements Serializer {
	@Override
	public void addAccept(String autoType) {
		JsonUtil.Context.addAccept(autoType);
	}

	@Override
	public <T>String serialize(T t) {
		return JsonUtil.serialize(t);
	}

	@Override
	public <T>T deserialize(String str) {
		return JsonUtil.deserialize(str);
	}
}
