package com.yuweix.kuafu.boot.web;


import com.yuweix.kuafu.web.springboot.I18nConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.i18n.enabled")
@Import({I18nConf.class})
public class I18nAutoConfiguration {

}
