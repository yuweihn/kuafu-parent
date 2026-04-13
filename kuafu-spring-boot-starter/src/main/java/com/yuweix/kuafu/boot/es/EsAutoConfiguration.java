package com.yuweix.kuafu.boot.es;


import com.yuweix.kuafu.data.springboot.es.EsConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.es.enabled")
@Import({EsConf.class})
public class EsAutoConfiguration {

}
