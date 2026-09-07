package com.yuweix.kuafu.boot;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;


public class EnvPostProcessor implements EnvironmentPostProcessor {
    private static final String PROPERTY_ALLOW_DEF_OVERRIDING = "spring.main.allow-bean-definition-overriding";


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.containsProperty(PROPERTY_ALLOW_DEF_OVERRIDING)) {
            Map<String, Object> map = new HashMap<>();
            map.put(PROPERTY_ALLOW_DEF_OVERRIDING, true);
            environment.getPropertySources().addLast(new MapPropertySource("defaultProperties", map));
        }
    }
}
