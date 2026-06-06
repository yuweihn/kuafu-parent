package com.yuweix.kuafu.core.serialize;


import com.yuweix.kuafu.core.JacksonUtil;


/**
 * @author yuwei
 */
public class JacksonSerializer implements Serializer {
    /**
     * 序列化对象为JSON字符串
     */
    @Override
    public <T> String serialize(T t) {
        return JacksonUtil.toJson(t);
    }

    /**
     * 反序列化JSON字符串为对象
     */
    @Override
    public <T> T deserialize(String str) {
        return JacksonUtil.toObject(str);
    }
}
