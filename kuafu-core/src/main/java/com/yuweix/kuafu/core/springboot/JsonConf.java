package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.json.Fastjson;
import com.yuweix.kuafu.core.json.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;


/**
 * @author yuwei
 */
public class JsonConf {
    @ConditionalOnMissingBean(Fastjson.class)
    @Bean
    public Fastjson fastjson(@Value("${kuafu.json.fastjson.accept:}") String accepts) {
        Fastjson json = new Fastjson();
        if (accepts != null && !"".equals(accepts.trim())) {
            String[] arr = accepts.trim().split(",");
            for (String accept: arr) {
                if (accept != null && !"".equals(accept.trim())) {
                    json.addAccept(accept.trim());
                }
            }
        }
        return json;
    }

    @ConditionalOnMissingBean(JsonUtil.class)
    @Bean
    public JsonUtil fastjsonUtil(Fastjson json) {
        try {
            Class<?> clz = Class.forName(JsonUtil.class.getName());
            Constructor<?> constructor = clz.getDeclaredConstructor(Fastjson.class);
            constructor.setAccessible(true);
            return (JsonUtil) constructor.newInstance(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
