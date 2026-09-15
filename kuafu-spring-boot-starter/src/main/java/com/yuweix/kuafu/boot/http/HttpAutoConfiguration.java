package com.yuweix.kuafu.boot.http;


import com.yuweix.kuafu.http.springboot.HttpConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.http.enabled")
@Import({HttpConf.class})
public class HttpAutoConfiguration {

}
