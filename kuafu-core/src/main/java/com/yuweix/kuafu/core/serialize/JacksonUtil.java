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
     * 默认的ObjectMapper
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * 不脱敏、保留类型信息
     */
    private static final ObjectMapper MAPPER2 = new ObjectMapper();
    /**
     * 不脱敏、不保留类型信息
     */
    private static final ObjectMapper MAPPER3 = new ObjectMapper();
    static {
        initMapper();
        initMapper2();
        initMapper3();
    }

    private static void initMapper() {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        MAPPER.setTimeZone(TimeZone.getTimeZone(Constant.DEFAULT_TIME_ZONE));
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /**
         * 脱敏配置
         */
        JacksonSensitiveFilter jacksonSensitiveFilter = new JacksonSensitiveFilter();
        SimpleModule jacksonSensitiveFilterModule = new SimpleModule(jacksonSensitiveFilter.getClass().getName());
        jacksonSensitiveFilterModule.setSerializerModifier(jacksonSensitiveFilter);
        MAPPER.registerModule(jacksonSensitiveFilterModule);

        // 配置类型验证器以支持安全的多态类型处理
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();
        // 启用默认类型信息，用于序列化和反序列化时保留类型信息
        MAPPER.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
    }
    private static void initMapper2() {
        MAPPER2.registerModule(new JavaTimeModule());
        MAPPER2.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        MAPPER2.setTimeZone(TimeZone.getTimeZone(Constant.DEFAULT_TIME_ZONE));
        MAPPER2.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER2.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER2.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 配置类型验证器以支持安全的多态类型处理
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();
        // 启用默认类型信息，用于序列化和反序列化时保留类型信息
        MAPPER2.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
    }
    private static void initMapper3() {
        MAPPER3.registerModule(new JavaTimeModule());
        MAPPER3.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        MAPPER3.setTimeZone(TimeZone.getTimeZone(Constant.DEFAULT_TIME_ZONE));
        MAPPER3.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER3.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER3.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public static String toJson(Object obj) {
        if (obj == null) {
            log.error("Object cannot be null");
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.error("Object cannot be processed. Error: {}", ex.getMessage(), ex);
            return null;
        }
    }
    public static<T> T toObject(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(text, new TypeReference<T>() {});
        } catch (Exception ex) {
            log.error("JSON string cannot be parsed. Error: {}", ex.getMessage(), ex);
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
            return MAPPER.readValue(text, javaType);
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
            return MAPPER.readValue(text, type);
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
            return MAPPER.readValue(text, clz);
        } catch (JsonProcessingException ex) {
            log.error("JSON string cannot be parsed to clz {}. Error: {}", clz, ex.getMessage(), ex);
            return null;
        }
    }
}
