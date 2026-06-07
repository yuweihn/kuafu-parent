package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.serialize.fastjson.FastSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;


/**
 * @author yuwei
 */
public class FastjsonConf {
    private static final Logger log = LoggerFactory.getLogger(FastjsonConf.class);

    @Primary
    @ConditionalOnMissingBean(FastSerializer.class)
    @Bean
    public FastSerializer fastSerializer(@Value("${kuafu.json.fastjson.accept:}") String accepts) {
        FastSerializer serializer = new FastSerializer();
        if (accepts != null && !"".equals(accepts.trim())) {
            String[] arr = accepts.trim().split(",");
            for (String accept: arr) {
                if (accept != null && !"".equals(accept.trim())) {
                    serializer.addAccept(accept.trim());
                }
            }
        }
        return serializer;
    }
}
