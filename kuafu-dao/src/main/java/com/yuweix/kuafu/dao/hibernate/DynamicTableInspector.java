package com.yuweix.kuafu.dao.hibernate;


import com.yuweix.kuafu.sharding.context.TableHolder;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 动态修改表名拦截器
 * @author yuwei
 */
public class DynamicTableInspector implements StatementInspector {
	private static final Logger log = LoggerFactory.getLogger(DynamicTableInspector.class);

	public DynamicTableInspector() {

	}

	@Override
	public String inspect(String sql) {
		final String srcName = TableHolder.getSrcName();
		final String targetName = TableHolder.getTargetName();
		if (srcName == null || targetName == null || srcName.equals(targetName)) {
			return sql;
		}
		log.info("Original SQL: {}", sql);
		String actualSql = sql.replaceAll("\\b" + Pattern.quote(srcName) + "\\b", Matcher.quoteReplacement(targetName));
		log.info("Actual SQL: {}", actualSql);
		return actualSql;
	}
}
