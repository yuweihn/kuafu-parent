package com.yuweix.kuafu.boot.sequence;


import com.yuweix.kuafu.sequence.springboot.SequenceConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.sequence.enabled")
@Import({SequenceConf.class})
public class SequenceAutoConfiguration {

}
