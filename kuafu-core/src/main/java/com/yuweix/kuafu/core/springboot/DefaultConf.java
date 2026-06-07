package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.Constant;
import com.yuweix.kuafu.core.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;


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
}
