package com.yuweix.kuafu.core.serialize.jackson;


import com.yuweix.kuafu.core.serialize.JacksonUtil;
import com.yuweix.kuafu.core.serialize.Serializer;


/**
 * @author yuwei
 */
public class JacksonSerializer implements Serializer {
    /**
     * 序列化对象为JSON字符串
     */
    @Override
    public <T> String serialize(T t) {
        return JacksonUtil.serialize(t);
    }

    /**
     * 反序列化JSON字符串为对象
     */
    @Override
    public <T> T deserialize(String str) {
        return JacksonUtil.deserialize(str);
    }
}
