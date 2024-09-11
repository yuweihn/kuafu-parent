package com.yuweix.kuafu.boot.redis;


import com.yuweix.kuafu.data.springboot.lettuce.LettuceConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.lettuce.enabled")
@Import({LettuceConf.class})
public class LettuceAutoConfiguration {

}
