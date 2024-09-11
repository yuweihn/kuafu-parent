package com.yuweix.kuafu.boot.schedule;


import com.yuweix.kuafu.schedule.springboot.ScheduleConf;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.schedule.enabled", matchIfMissing = true)
@Import({ScheduleConf.class})
public class ScheduleAutoConfiguration {

}
