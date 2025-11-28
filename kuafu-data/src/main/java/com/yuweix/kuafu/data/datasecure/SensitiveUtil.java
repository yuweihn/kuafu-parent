package com.yuweix.kuafu.data.datasecure;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;


/**
 * 脱敏工具类
 */
public class SensitiveUtil {
    private static final Logger log = LoggerFactory.getLogger(SensitiveUtil.class);


    private static Field getField(Object object, String fieldName) {
        Field field = null;
        try {
            field = object.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ignored) {
        }
        return field;
    }

    public static<T> T shield(Object object, String fieldName) {
        Field field = getField(object, fieldName);
        if (field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            T val = (T) field.get(object);
            return shield(field, val);
        } catch (Exception ex) {
            log.error("脱敏失败，Error: {}", ex.getMessage(), ex);
            return null;
        }
    }

    public static<T> T shield(Object object, String fieldName, T val) {
        Field field = getField(object, fieldName);
        return shield(field, val);
    }

    private static<T> T shield(Field field, T val) {
        if (field == null || val == null) {
            return val;
        }
        if (!(val instanceof String) || "".equals(val)) {
            return val;
        }
        Sensitive sensitive;
        if (String.class != field.getType() || (sensitive = field.getAnnotation(Sensitive.class)) == null) {
            return val;
        }
        return (T) replace((String) val, sensitive.regex(), sensitive.replacement());
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
