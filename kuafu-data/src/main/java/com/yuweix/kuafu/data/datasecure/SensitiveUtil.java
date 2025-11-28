package com.yuweix.kuafu.data.datasecure;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;


/**
 * 脱敏工具类
 */
public class SensitiveUtil {
    private static final Logger log = LoggerFactory.getLogger(SensitiveUtil.class);


    public static<T> T shield(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            T val = (T) field.get(object);
            return shield(object, fieldName, val);
        } catch (Exception ex) {
            log.error("获取字段出错，Error: {}", ex.getMessage(), ex);
            return null;
        }
    }

    public static<T> T shield(Object object, String fieldName, T val) {
        try {
            if (!(val instanceof String) || "".equals(val)) {
                return val;
            }
            Field field = object.getClass().getDeclaredField(fieldName);
            Sensitive sensitive;
            if (String.class != field.getType() || (sensitive = field.getAnnotation(Sensitive.class)) == null) {
                return val;
            }
            return (T) replace((String) val, sensitive.regex(), sensitive.replacement());
        } catch (Exception ex) {
            log.error("获取字段出错，Error: {}", ex.getMessage(), ex);
            return null;
        }
    }

    private static String replace(String val, String regex, String replacement) {
        if (val == null || "".equals(val)
                || regex == null || "".equals(regex.trim())
                || replacement == null || "".equals(replacement.trim())) {
            return val;
        }
        return val.replaceAll(regex.trim(), replacement.trim());
    }
}
