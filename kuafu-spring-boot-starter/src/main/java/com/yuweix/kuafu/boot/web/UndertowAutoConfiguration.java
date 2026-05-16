package com.yuweix.kuafu.boot.web;


import com.yuweix.kuafu.web.springboot.UndertowConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.web.undertow.enabled")
@Import({UndertowConf.class})
public class UndertowAutoConfiguration {

}
