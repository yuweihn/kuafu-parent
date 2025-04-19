package com.yuweix.kuafu.boot.core;


import com.yuweix.kuafu.core.springboot.JsonConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.json.enabled", matchIfMissing = true)
@Import({JsonConf.class})
public class JsonAutoConfiguration {

}
