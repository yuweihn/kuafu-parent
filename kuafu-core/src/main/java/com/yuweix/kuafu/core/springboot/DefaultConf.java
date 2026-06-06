package com.yuweix.kuafu.core.springboot;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.yuweix.kuafu.core.Constant;
import com.yuweix.kuafu.core.SpringContext;
import com.yuweix.kuafu.core.serialize.FastSerializer;
import com.yuweix.kuafu.core.serialize.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;
import java.util.TimeZone;


/**
 * @author yuwei
 */
public class DefaultConf {
    @ConditionalOnMissingBean(Constant.class)
    @Bean
    public Constant constant() {
        try {
            Class<?> clz = Class.forName(Constant.class.getName());
            Constructor<?> constructor = clz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Constant) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ConditionalOnMissingBean(SpringContext.class)
    @Bean
    public SpringContext springContext() {
        try {
            Class<?> clz = Class.forName(SpringContext.class.getName());
            Constructor<?> constructor = clz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (SpringContext) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ConditionalOnMissingBean(Serializer.class)
    @Bean
    public Serializer serializer(@Value("${kuafu.json.fastjson.accept:}") String accepts) {
        Serializer serializer = new FastSerializer();
        if (accepts != null && !"".equals(accepts.trim())) {
            String[] arr = accepts.trim().split(",");
            for (String accept: arr) {
                if (accept != null && !"".equals(accept.trim())) {
                    serializer.addAccept(accept.trim());
                }
            }
        }
        return serializer;
    }

    @ConditionalOnMissingBean(ObjectMapper.class)
    @Bean
    public ObjectMapper objectMapper(@Value("${kuafu.json.jackson.time-zone:Asia/Shanghai}") String timeZone) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone(timeZone));
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
        return mapper;
    }
}
