package com.yuweix.kuafu.boot.redis;


import com.yuweix.kuafu.data.springboot.lettuce.LettuceClusterConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.lettuce.cluster.enabled")
@Import({LettuceClusterConf.class})
public class LettuceClusterAutoConfiguration {

}
