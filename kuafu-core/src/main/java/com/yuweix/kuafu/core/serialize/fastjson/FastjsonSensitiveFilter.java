package com.yuweix.kuafu.core.serialize.fastjson;


import com.alibaba.fastjson2.filter.ValueFilter;
import com.yuweix.kuafu.core.serialize.SensitiveUtil;


/**
 * @author yuwei
 */
public class FastjsonSensitiveFilter implements ValueFilter {
	@Override
    public Object apply(Object object, String name, Object value) {
        if (!(value instanceof String)) {
            return value;
        }
        return SensitiveUtil.shield(object, name, value);
    }
}
