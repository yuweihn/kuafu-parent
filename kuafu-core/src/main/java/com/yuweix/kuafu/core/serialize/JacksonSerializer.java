package com.yuweix.kuafu.core.serialize;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.yuweix.kuafu.core.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;


/**
 * 基于Jackson ObjectMapper的序列化器
 * @author yuwei
 */
public class JacksonSerializer implements Serializer {
    private static final Logger log = LoggerFactory.getLogger(JacksonSerializer.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    private static PolymorphicTypeValidator typeValidator;

    static {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        mapper.registerModule(javaTimeModule);
        mapper.setTimeZone(TimeZone.getTimeZone(Constant.DEFAULT_TIME_ZONE));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 配置类型验证器以支持安全的多态类型处理
        typeValidator = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();
        // 启用默认类型信息，用于序列化和反序列化时保留类型信息
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    /**
     * 序列化对象为JSON字符串
     */
    @Override
    public <T> String serialize(T t) {
        if (t == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(t);
        } catch (Exception ex) {
            log.error("Error on serialize: {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * 反序列化JSON字符串为对象
     */
    @Override
    public <T> T deserialize(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            // 使用TypeReference来保持泛型信息
            return mapper.readValue(str, new TypeReference<T>() {});
        } catch (Exception ex) {
            log.error("Error on deserialize: {}", ex.getMessage(), ex);
            return null;
        }
    }
}
