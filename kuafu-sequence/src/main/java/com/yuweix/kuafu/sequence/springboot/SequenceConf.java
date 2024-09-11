package com.yuweix.kuafu.sequence.springboot;


import com.yuweix.kuafu.sequence.base.DefaultSequence;
import com.yuweix.kuafu.sequence.base.SequenceBeanProcessor;
import com.yuweix.kuafu.sequence.base.SequenceBean;
import com.yuweix.kuafu.sequence.dao.SegmentSequenceDao;
import com.yuweix.kuafu.sequence.dao.SequenceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwei
 */
public class SequenceConf {
	@ConditionalOnMissingBean(SequenceDao.class)
	@Bean(name = "sequenceDao")
	public SequenceDao sequenceDao(@Autowired DataSource dataSource
			, @Value("${kuafu.sequence.setting.inner-step:100}") int innerStep
			, @Value("${kuafu.sequence.setting.retry-times:5}") int retryTimes
			, @Value("${kuafu.sequence.setting.segment-count:1}") int segmentCount
			, @Value("${kuafu.sequence.setting.max-skip-count:5}") int maxSkipCount
			, @Value("${kuafu.sequence.setting.max-wait-millis:5000}") long maxWaitMillis
			, @Value("${kuafu.sequence.setting.rule-class-name:}") String ruleClassName
			, @Value("${kuafu.sequence.setting.table-name:sequence}") String tableName) {
		SegmentSequenceDao sequenceDao = new SegmentSequenceDao();
		sequenceDao.setDataSource(dataSource);
		sequenceDao.setInnerStep(innerStep);
		sequenceDao.setRetryTimes(retryTimes);
		sequenceDao.setSegmentCount(segmentCount);
		sequenceDao.setMaxSkipCount(maxSkipCount);
		sequenceDao.setMaxWaitMillis(maxWaitMillis);
		sequenceDao.setRuleClassName(ruleClassName);
		sequenceDao.setTableName(tableName);
		return sequenceDao;
	}

	@ConditionalOnMissingBean(SequenceBean.class)
	@Bean
	@ConfigurationProperties(prefix = "kuafu.sequence", ignoreUnknownFields = true)
	public SequenceBean sequenceBean() {
		return new SequenceBean() {
			private Map<String, String> map = new HashMap<>();
			@Override
			public Map<String, String> getBeans() {
				return map;
			}
			@Override
			public Map<String, String> getBaseBeans() {
				return null;
			}
		};
	}

	@ConditionalOnMissingBean(SequenceBeanProcessor.class)
	@Bean
	public SequenceBeanProcessor sequenceBeanProcessor(Environment env) {
		String clzName = env.getProperty("kuafu.sequence.class-name");
		if (clzName != null && !"".equals(clzName)) {
			return new SequenceBeanProcessor(clzName);
		} else {
			return new SequenceBeanProcessor(DefaultSequence.class);
		}
	}
}
