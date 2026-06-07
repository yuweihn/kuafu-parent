package com.yuweix.kuafu.boot.serialize;


import com.yuweix.kuafu.core.springboot.DefaultConf;
import com.yuweix.kuafu.core.springboot.FastjsonConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.json.fastjson.enabled", matchIfMissing = true)
@Import({DefaultConf.class, FastjsonConf.class})
public class FastjsonAutoConfiguration {

}
