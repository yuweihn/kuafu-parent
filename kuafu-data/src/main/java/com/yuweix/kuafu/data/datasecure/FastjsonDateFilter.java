package com.yuweix.kuafu.data.datasecure;


import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.yuweix.kuafu.core.DateUtil;

import java.lang.reflect.Field;
import java.util.Date;


/**
 * @author yuwei
 */
public class FastjsonDateFilter implements ValueFilter {
    private String defaultDateFormat = "yyyy-MM-dd HH:mm:ss";

    public FastjsonDateFilter() {
    }
    public FastjsonDateFilter(String defaultDateFormat) {
        this.defaultDateFormat = defaultDateFormat;
    }

    public void setDefaultDateFormat(String defaultDateFormat) {
        this.defaultDateFormat = defaultDateFormat;
    }

    @Override
    public Object apply(Object object, String name, Object value) {
        if (!(value instanceof Date)) {
            return value;
        }
        Field field = getField(object, name);
        if (field == null) {
            return value;
        }
        JSONField jsonField;
        if ((jsonField = field.getAnnotation(JSONField.class)) == null) {
            return value;
        }
        String format = jsonField.format();
        return DateUtil.formatDate((Date) value, format == null || format.length() <= 0 ? defaultDateFormat : format);
    }

    private static Field getField(Object object, String fieldName) {
        Field field = null;
        try {
            field = object.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ignored) {
        }
        return field;
    }
}
