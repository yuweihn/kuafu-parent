package com.yuweix.kuafu.boot.sharding;


import com.yuweix.kuafu.sharding.springboot.ShardingConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.sharding.enabled")
@Import({ShardingConf.class})
public class ShardingAutoConfiguration {

}
