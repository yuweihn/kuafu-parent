package com.yuweix.kuafu.schedule.springboot;


import com.yuweix.kuafu.data.elect.Elector;
import com.yuweix.kuafu.schedule.ScheduleAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.Map;


/**
 * @author yuwei
 */
public class ScheduleConf {
	private static final Logger log = LoggerFactory.getLogger(ScheduleConf.class);

	@ConditionalOnMissingBean(ScheduleAspect.class)
	@Bean
	public ScheduleAspect scheduleAspect(ObjectProvider<Elector> electors
			, @Autowired(required = false) Map<String, Elector> electorBeans
			, @Value("${kuafu.schedule.elector:#{null}}") String electorName) {
		ScheduleAspect scheduleAspect = new ScheduleAspect();
		scheduleAspect.setElector(resolveElector(electors, electorBeans, electorName));
		return scheduleAspect;
	}

	/**
	 * 容器中存在多个Elector时，取 kuafu.schedule.elector 指定的bean名称对应的那个；
	 * 未指定时按类型取（唯一或标@Primary才返回，多个且无法判定时返回null）
	 */
	private static Elector resolveElector(ObjectProvider<Elector> electors
			, Map<String, Elector> electorBeans, String electorName) {
		if (electorName == null || electorName.trim().isEmpty()) {
			Elector elector = electors.getIfUnique();
			if (elector == null) {
				log.error("Multiple Elector beans exist and none is @Primary, "
								+ "set 'kuafu.schedule.elector' to specify one. available: {}"
						, electorBeans == null ? "[]" : electorBeans.keySet());
			}
			return elector;
		}

		String name = electorName.trim();
		Elector elector = electorBeans == null ? null : electorBeans.get(name);
		if (elector == null) {
			//配置的bean名称不存在时必须失败，否则会退化为本地选主导致所有节点重复执行任务
			throw new IllegalStateException("Elector bean '" + name + "' not found. available elector beans: "
					+ (electorBeans == null ? "[]" : electorBeans.keySet()));
		}
		return elector;
	}
}
