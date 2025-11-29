package com.yuweix.kuafu.data.datasecure;


import com.alibaba.fastjson2.filter.ValueFilter;


/**
 * @author yuwei
 */
public class FastjsonSensitiveFilter implements ValueFilter {
	@Override
    public Object apply(Object object, String name, Object value) {
        if (!(object instanceof String)) {
            return value;
        }
        return SensitiveUtil.shield(object, name, value);
    }
}
