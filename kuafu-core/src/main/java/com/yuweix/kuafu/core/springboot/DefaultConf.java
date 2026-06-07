package com.yuweix.kuafu.core.springboot;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yuweix.kuafu.core.Constant;
import com.yuweix.kuafu.core.SpringContext;
import com.yuweix.kuafu.core.serialize.FastSerializer;
import com.yuweix.kuafu.core.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;


/**
 * @author yuwei
 */
public class DefaultConf {
    private static final Logger log = LoggerFactory.getLogger(DefaultConf.class);

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

    /**
     * @param timeZone
     * @param serializerModifiers  {@link BeanSerializerModifier}类列表，多个类用逗号分隔
     * @return
     */
    @ConditionalOnMissingBean(ObjectMapper.class)
    @Bean
    public ObjectMapper objectMapper(@Value("${kuafu.json.jackson.time-zone:" + Constant.DEFAULT_TIME_ZONE + "}") String timeZone
            , @Value("${kuafu.json.jackson.serializer-modifiers:}") String serializerModifiers) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setTimeZone(TimeZone.getTimeZone(timeZone));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (serializerModifiers != null && !"".equals(serializerModifiers.trim())) {
            List<BeanSerializerModifier> modifiers = new ArrayList<>();
            String[] arr = serializerModifiers.trim().split(",");
            if (arr != null && arr.length > 0) {
                for (String mod : arr) {
                    if (mod == null || "".equals(mod.trim())) {
                        continue;
                    }
                    mod = mod.trim();
                    try {
                        Class<?> clz = Class.forName(mod);
                        BeanSerializerModifier modifier = (BeanSerializerModifier) clz.newInstance();
                        modifiers.add(modifier);
                    } catch (Exception ex) {
                        log.error("BeanSerializerModifier cannot be created. Error: {}", ex.getMessage(), ex);
                    }
                }
            }
            for (BeanSerializerModifier modifier : modifiers) {
                SimpleModule module = new SimpleModule(modifier.getClass().getSimpleName());
                module.setSerializerModifier(modifier);
                mapper.registerModule(module);
            }
        }
        return mapper;
    }
}
