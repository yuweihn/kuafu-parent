package com.yuweix.kuafu.core.serialize.jackson;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * Jackson脱敏过滤器
 * 对标注了{@link com.yuweix.kuafu.core.serialize.Sensitive}注解的String字段在序列化时自动脱敏
 * @author yuwei
 */
public class JacksonSensitiveFilter extends BeanSerializerModifier {
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        List<BeanPropertyWriter> result = new ArrayList<>(beanProperties.size());
        for (BeanPropertyWriter writer : beanProperties) {
            if (writer.getType().getRawClass() == String.class) {
                Field field = getField(beanDesc.getBeanClass(), writer.getName());
                if (field != null) {
                    com.yuweix.kuafu.core.serialize.Sensitive sensitive = field.getAnnotation(com.yuweix.kuafu.core.serialize.Sensitive.class);
                    if (sensitive != null) {
                        String regex = sensitive.regex();
                        String replacement = sensitive.replacement();
                        if (regex != null && !"".equals(regex.trim()) && replacement != null && !"".equals(replacement.trim())) {
                            result.add(new SensitivePropertyWriter(writer, regex.trim(), replacement.trim()));
                            continue;
                        }
                    }
                }
            }
            result.add(writer);
        }
        return result;
    }

    private static Field getField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 脱敏属性写入器，对String值在序列化前进行正则替换脱敏
     */
    private static class SensitivePropertyWriter extends BeanPropertyWriter {
        private final String regex;
        private final String replacement;

        SensitivePropertyWriter(BeanPropertyWriter base, String regex, String replacement) {
            super(base);
            this.regex = regex;
            this.replacement = replacement;
        }

        @Override
        public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
            Object value = get(bean);
            if (value instanceof String) {
                String strValue = (String) value;
                if (!"".equals(strValue)) {
                    gen.writeFieldName(getName());
                    gen.writeString(strValue.replaceAll(regex, replacement));
                    return;
                }
            }
            super.serializeAsField(bean, gen, prov);
        }
    }
}
