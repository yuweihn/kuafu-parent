package com.yuweix.kuafu.core;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.TimeZone;


/**
 * @author yuwei
 */
public abstract class JacksonUtil {
    private static final Logger log = LoggerFactory.getLogger(JacksonUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setTimeZone(TimeZone.getTimeZone(Constant.DEFAULT_TIME_ZONE));
        mapper.setDateFormat(new com.fasterxml.jackson.databind.util.StdDateFormat());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 配置类型验证器以支持安全的多态类型处理
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        // 启用默认类型信息，用于序列化和反序列化时保留类型信息
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
    }


    public static String toJson(Object obj) {
        if (obj == null) {
            log.error("Object cannot be null");
            return null;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.error("Object cannot be processed. Error: {}", ex.getMessage());
            return null;
        }
    }
    public static Object toObject(String text) {
        if (text == null || text.isEmpty()) {
            log.error("JSON string cannot be null or empty");
            return null;
        }
        try {
            return mapper.readValue(text, Object.class);
        } catch (JsonProcessingException ex) {
            log.error("JSON string cannot be parsed. Error: {}", ex.getMessage());
            return null;
        }
    }
    public static<T> T toObject(String text, Type type) {
        if (text == null || text.isEmpty()) {
            log.error("JSON string cannot be null or empty");
            return null;
        }
        if (type == null) {
            log.error("Type cannot be null");
            return null;
        }
        try {
            JavaType javaType = TypeFactory.defaultInstance().constructType(type);
            return mapper.readValue(text, javaType);
        } catch (JsonProcessingException ex) {
            log.error("JSON string cannot be parsed to type {}. Error: {}", type, ex.getMessage());
            return null;
        }
    }
    public static<T> T toObject(String text, TypeReference<T> type) {
        if (text == null || text.isEmpty()) {
            log.error("JSON string cannot be null or empty");
            return null;
        }
        if (type == null) {
            log.error("Type cannot be null");
            return null;
        }
        try {
            return mapper.readValue(text, type);
        } catch (JsonProcessingException ex) {
            log.error("JSON string cannot be parsed to type {}. Error: {}", type, ex.getMessage());
            return null;
        }
    }
    public static<T> T toObject(String text, Class<T> clz) {
        if (text == null || text.isEmpty()) {
            log.error("JSON string cannot be null or empty");
            return null;
        }
        if (clz == null) {
            log.error("Clz cannot be null");
            return null;
        }
        try {
            return mapper.readValue(text, clz);
        } catch (JsonProcessingException ex) {
            log.error("JSON string cannot be parsed to clz {}. Error: {}", clz, ex.getMessage());
            return null;
        }
    }
}
