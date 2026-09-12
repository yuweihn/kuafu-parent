package com.yuweix.kuafu.boot;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;


public class EnvPostProcessor implements EnvironmentPostProcessor {
    private static final String PROPERTY_ALLOW_DEF_OVERRIDING = "spring.main.allow-bean-definition-overriding";
    private static final String SPRING_ENV_DEFAULT_PROPERTIES_SOURCE_NAME = "spring.env.default.properties";


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> map = new HashMap<>();
        if (!environment.containsProperty(PROPERTY_ALLOW_DEF_OVERRIDING)) {
            map.put(PROPERTY_ALLOW_DEF_OVERRIDING, true);
        }
        if (!map.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(SPRING_ENV_DEFAULT_PROPERTIES_SOURCE_NAME, map));
        }
    }
}
