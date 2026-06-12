package com.yuweix.kuafu.core.serialize;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yuweix.kuafu.core.Constant;
import com.yuweix.kuafu.core.serialize.jackson.JacksonSensitiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


/**
 * @author yuwei
 */
public abstract class JacksonUtil {
    private static final Logger log = LoggerFactory.getLogger(JacksonUtil.class);

    /**
     * 脱敏、保留类型信息
     */
    private static final ObjectMapper SERIALIZE_MAPPER = new ObjectMapper();
    /**
     * 不脱敏、不保留类型信息
     */
    private static final ObjectMapper BASE_MAPPER = new ObjectMapper();
    static {
        initSerializeMapper();
        initBaseMapper();
    }

    private static void initSerializeMapper() {
        SERIALIZE_MAPPER.registerModule(new JavaTimeModule());
        SERIALIZE_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        SERIALIZE_MAPPER.setTimeZone(TimeZone.getTimeZone(Constant.DEFAULT_TIME_ZONE));
        SERIALIZE_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SERIALIZE_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SERIALIZE_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /**
         * 脱敏配置
         */
        JacksonSensitiveFilter jacksonSensitiveFilter = new JacksonSensitiveFilter();
        SimpleModule jacksonSensitiveFilterModule = new SimpleModule(jacksonSensitiveFilter.getClass().getName());
        jacksonSensitiveFilterModule.setSerializerModifier(jacksonSensitiveFilter);
        SERIALIZE_MAPPER.registerModule(jacksonSensitiveFilterModule);

        // 配置类型验证器以支持安全的多态类型处理
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();
        // 启用默认类型信息，用于序列化和反序列化时保留类型信息
        SERIALIZE_MAPPER.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
    }
    private static void initBaseMapper() {
        BASE_MAPPER.registerModule(new JavaTimeModule());
        BASE_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        BASE_MAPPER.setTimeZone(TimeZone.getTimeZone(Constant.DEFAULT_TIME_ZONE));
        BASE_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        BASE_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        BASE_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static String toJson(Object obj) {
        if (obj == null) {
            log.error("Object cannot be null");
            return null;
        }
        try {
            return BASE_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.error("Object cannot be processed. Error: {}", ex.getMessage(), ex);
            return null;
        }
    }
    public static Object toObject(String text) {
        if (text == null || text.isEmpty()) {
            log.error("JSON string cannot be null or empty");
            return null;
        }
        try {
            return BASE_MAPPER.readValue(text, Object.class);
        } catch (Exception ex) {
            log.error("JSON string cannot be parsed. Error: {}", ex.getMessage(), ex);
            return null;
        }
    }
    public static Object tryParse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return BASE_MAPPER.readValue(text, Object.class);
        } catch (Exception ex) {
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
            return BASE_MAPPER.readValue(text, javaType);
        } catch (JsonProcessingException ex) {
            log.error("JSON string cannot be parsed to type {}. Error: {}", type, ex.getMessage(), ex);
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
            return BASE_MAPPER.readValue(text, type);
        } catch (JsonProcessingException ex) {
            log.error("JSON string cannot be parsed to type {}. Error: {}", type, ex.getMessage(), ex);
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
            return BASE_MAPPER.readValue(text, clz);
        } catch (JsonProcessingException ex) {
            log.error("JSON string cannot be parsed to clz {}. Error: {}", clz, ex.getMessage(), ex);
            return null;
        }
    }

    public static String serialize(Object obj) {
        if (obj == null) {
            log.error("Object cannot be null");
            return null;
        }
        try {
            return SERIALIZE_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.error("序列化失败. Error: {}", ex.getMessage(), ex);
            return null;
        }
    }
    public static<T> T deserialize(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return SERIALIZE_MAPPER.readValue(text, new TypeReference<T>() {});
        } catch (Exception ex) {
            log.error("JSON string cannot be parsed. text: {}. Error: {}", text, ex.getMessage(), ex);
            return null;
        }
    }
}
