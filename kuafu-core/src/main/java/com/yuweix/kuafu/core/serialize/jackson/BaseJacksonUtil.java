package com.yuweix.kuafu.core.serialize.jackson;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yuweix.kuafu.core.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


/**
 * @author yuwei
 */
public abstract class BaseJacksonUtil {
    private static final Logger log = LoggerFactory.getLogger(BaseJacksonUtil.class);

    /**
     * 不脱敏、不保留类型信息
     */
    private static final ObjectMapper BASE_MAPPER = new ObjectMapper();
    static {
        initBaseMapper();
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
    public static<T> T toObject(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return BASE_MAPPER.readValue(text, new TypeReference<T>() {});
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
}
