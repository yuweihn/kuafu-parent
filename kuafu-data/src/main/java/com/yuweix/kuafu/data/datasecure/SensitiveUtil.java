package com.yuweix.kuafu.data.datasecure;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;


/**
 * 脱敏工具类
 */
public class SensitiveUtil {
    private static final Logger log = LoggerFactory.getLogger(SensitiveUtil.class);


    public static Object shield(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            Object val = field.get(object);
            return shield(object, fieldName, val);
        } catch (Exception ex) {
            log.error("获取字段出错，Error: {}", ex.getMessage(), ex);
            return null;
        }
    }

    public static Object shield(Object object, String fieldName, Object val) {
        try {
            if (!(val instanceof String) || "".equals(val)) {
                return val;
            }
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Sensitive sensitive;
            if (String.class != field.getType() || (sensitive = field.getAnnotation(Sensitive.class)) == null) {
                return val;
            }
            String shieldVal = replace((String) val, sensitive.regex(), sensitive.replacement());
            field.set(object, shieldVal);
            return shieldVal;
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
