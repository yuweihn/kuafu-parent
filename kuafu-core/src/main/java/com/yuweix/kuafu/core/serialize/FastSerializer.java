package com.yuweix.kuafu.core.serialize;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.filter.Filter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author yuwei
 */
public class FastSerializer implements Serializer {
	private static final List<String> autoTypes = new CopyOnWriteArrayList<>();
	private static Filter autoTypeFilter;

	public FastSerializer() {

	}
	public FastSerializer(String autoType) {
		addAccept(autoType);
	}
	@Override
	public void addAccept(String autoType) {
		if (!autoTypes.contains(autoType)) {
			autoTypes.add(autoType);
		}
		autoTypeFilter = JSONReader.autoTypeFilter(autoTypes.toArray(new String[0]));
	}

	@Override
	public <T>String serialize(T t) {
		if (t == null) {
			return null;
		}
		return JSON.toJSONString(t, JSONWriter.Feature.WriteClassName);
	}

	@Override
	public <T>T deserialize(String str) {
		if (str == null) {
			return null;
		}
		if (autoTypeFilter == null) {
			return JSON.parseObject(str, new TypeReference<T>() {});
		} else {
			return JSON.parseObject(str, new TypeReference<T>() {}, autoTypeFilter);
		}
	}
}
