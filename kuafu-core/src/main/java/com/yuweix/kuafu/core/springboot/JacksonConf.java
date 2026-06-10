package com.yuweix.kuafu.core.springboot;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yuweix.kuafu.core.Constant;
import com.yuweix.kuafu.core.serialize.jackson.JacksonSensitiveFilter;
import com.yuweix.kuafu.core.serialize.jackson.JacksonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;


/**
 * @author yuwei
 */
public class JacksonConf {
    private static final Logger log = LoggerFactory.getLogger(JacksonConf.class);

    /**
     * @param timeZone
     * @param serializerModifiers  {@link BeanSerializerModifier}类列表，多个类用逗号分隔
     * @return
     */
    @ConditionalOnMissingBean(ObjectMapper.class)
    @Bean
    public ObjectMapper objectMapper(@Value("${kuafu.json.jackson.mapper.time-zone:" + Constant.DEFAULT_TIME_ZONE + "}") String timeZone
            , @Value("${kuafu.json.jackson.mapper.sensitive:true}") boolean sensitive
            , @Value("${kuafu.json.jackson.mapper.serializer-modifiers:}") String serializerModifiers) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setTimeZone(TimeZone.getTimeZone(timeZone));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /**
         * 脱敏配置
         */
        if (sensitive) {
            JacksonSensitiveFilter jacksonSensitiveFilter = new JacksonSensitiveFilter();
            SimpleModule jacksonSensitiveFilterModule = new SimpleModule(jacksonSensitiveFilter.getClass().getName());
            jacksonSensitiveFilterModule.setSerializerModifier(jacksonSensitiveFilter);
            mapper.registerModule(jacksonSensitiveFilterModule);
        }

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
                        BeanSerializerModifier modifier = (BeanSerializerModifier) clz.getDeclaredConstructor().newInstance();
                        modifiers.add(modifier);
                    } catch (Exception ex) {
                        log.error("BeanSerializerModifier cannot be created. Error: {}", ex.getMessage(), ex);
                    }
                }
            }
            for (BeanSerializerModifier modifier : modifiers) {
                SimpleModule module = new SimpleModule(modifier.getClass().getName());
                module.setSerializerModifier(modifier);
                mapper.registerModule(module);
            }
        }
        return mapper;
    }

    @ConditionalOnMissingBean(JacksonSerializer.class)
    @Bean
    public JacksonSerializer jacksonSerializer() {
        return new JacksonSerializer();
    }
}
