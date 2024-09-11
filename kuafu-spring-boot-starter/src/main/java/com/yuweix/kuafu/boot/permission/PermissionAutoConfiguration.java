package com.yuweix.kuafu.boot.permission;


import com.yuweix.kuafu.permission.springboot.PermissionConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.permission.enabled")
@Import({PermissionConf.class})
public class PermissionAutoConfiguration {

}
