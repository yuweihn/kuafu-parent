package com.yuweix.kuafu.boot;


import com.yuweix.kuafu.core.springboot.DefaultConf;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@AutoConfigureOrder(Integer.MIN_VALUE)
@ConditionalOnProperty(name = "kuafu.boot.default.enabled", matchIfMissing = true)
@Import({DefaultConf.class})
public class DefaultAutoConfiguration {

}
