package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.SpringContext;
import com.yuweix.kuafu.core.serialize.FastSerializer;
import com.yuweix.kuafu.core.serialize.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;


/**
 * @author yuwei
 */
public class DefaultConf {
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
}
