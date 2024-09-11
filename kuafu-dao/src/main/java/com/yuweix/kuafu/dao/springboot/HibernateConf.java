package com.yuweix.kuafu.dao.springboot;


import com.alibaba.druid.pool.DruidDataSource;
import com.yuweix.kuafu.dao.datasource.DynamicDataSource;
import com.yuweix.kuafu.dao.datasource.DynamicDataSourceAspect;
import com.yuweix.kuafu.dao.hibernate.DynamicTableInspector;
import com.yuweix.kuafu.sharding.aspect.DataSourceAspect;
import com.yuweix.kuafu.sharding.context.ShardingContext;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;


/**
 * @author yuwei
 */
@EnableTransactionManagement(proxyTargetClass = true)
public class HibernateConf {
	@ConditionalOnProperty(name = "kuafu.datasource.default.enabled", matchIfMissing = true)
	@ConditionalOnMissingBean(name = "dataSource")
	@Bean(name = "dataSource", initMethod = "init", destroyMethod = "close")
	public DataSource defaultDataSource(@Value("${kuafu.datasource.default.driver-class}") String driverClassName
			, @Value("${kuafu.datasource.default.url}") String url
			, @Value("${kuafu.datasource.default.user-name}") String userName
			, @Value("${kuafu.datasource.default.password}") String password
			, @Value("${kuafu.datasource.default.default-read-only:false}") boolean defaultReadOnly
			, @Value("${kuafu.datasource.default.filters:stat}") String filters
			, @Value("${kuafu.datasource.default.max-active:2}") int maxActive
			, @Value("${kuafu.datasource.default.initial-size:1}") int initialSize
			, @Value("${kuafu.datasource.default.max-wait-mills:60000}") long maxWaitMillis
			, @Value("${kuafu.datasource.default.remove-abandoned:false}") boolean removeAbandoned
			, @Value("${kuafu.datasource.default.remove-abandoned-timeout:1800}") int removeAbandonedTimeout
			, @Value("${kuafu.datasource.default.min-idle:1}") int minIdle
			, @Value("${kuafu.datasource.default.time-between-eviction-runs-millis:60000}") long timeBetweenEvictionRunsMillis
			, @Value("${kuafu.datasource.default.min-evictable-idle-time-millis:300000}") long minEvictableIdleTimeMillis
			, @Value("${kuafu.datasource.default.validation-query:select 'x'}") String validationQuery
			, @Value("${kuafu.datasource.default.test-while-idle:true}") boolean testWhileIdle
			, @Value("${kuafu.datasource.default.test-on-borrow:false}") boolean testOnBorrow
			, @Value("${kuafu.datasource.default.test-on-return:false}") boolean testOnReturn
			, @Value("${kuafu.datasource.default.pool-prepared-statements:true}") boolean poolPreparedStatements
			, @Value("${kuafu.datasource.default.max-pool-prepared-statement-per-connection-size:50}") int maxPoolPreparedStatementPerConnectionSize
			, @Value("${kuafu.datasource.default.max-open-prepared-statements:100}") int maxOpenPreparedStatements) throws SQLException {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(userName);
		dataSource.setPassword(password);
		dataSource.setDefaultReadOnly(defaultReadOnly);
		dataSource.setFilters(filters);
		dataSource.setMaxActive(maxActive);
		dataSource.setInitialSize(initialSize);
		dataSource.setMaxWait(maxWaitMillis);
		dataSource.setRemoveAbandoned(removeAbandoned);
		dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
		dataSource.setMinIdle(minIdle);
		dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		dataSource.setValidationQuery(validationQuery);
		dataSource.setTestWhileIdle(testWhileIdle);
		dataSource.setTestOnBorrow(testOnBorrow);
		dataSource.setTestOnReturn(testOnReturn);
		dataSource.setPoolPreparedStatements(poolPreparedStatements);
		dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
		dataSource.setMaxOpenPreparedStatements(maxOpenPreparedStatements);
		return dataSource;
	}

	@ConditionalOnMissingBean(name = "dataSources")
	@Bean(name = "dataSources")
	public Map<String, DataSource> dataSources() {
		return null;
	}

	@ConditionalOnMissingBean(DataSourceAspect.class)
	@Bean(name = "dynamicDataSourceAspect")
	public DynamicDataSourceAspect dynamicDataSourceAspect() {
		return new DynamicDataSourceAspect();
	}

	@Primary
	@ConditionalOnMissingBean(name = "dynamicDataSource")
	@Bean(name = "dynamicDataSource")
	public DataSource dynamicDataSource(@Autowired(required = false) @Qualifier("dataSource") DataSource defaultDataSource
			, @Value("${kuafu.datasource.default.lenient:false}") boolean lenient
			, @Qualifier("dataSources") Map<String, DataSource> dataSources) {
		if (dataSources == null) {
			dataSources = new HashMap<>();
		}

		DynamicDataSource dds = new DynamicDataSource();
		dds.setLenientFallback(lenient);
		dds.setDefaultTargetDataSource(defaultDataSource);
		dds.setTargetDataSources(new HashMap<>(dataSources));
		return dds;
	}


	@ConditionalOnMissingBean(name = "mappingLocations")
	@Bean(name = "mappingLocations")
	public Resource[] mappingLocations() {
		return new Resource[] {};
	}

	@ConditionalOnMissingBean(name = "packagesToScan")
	@Bean(name = "packagesToScan")
	public String[] packagesToScan(@Value("${kuafu.hibernate.scan.packages:}") String packages) {
		if (packages == null || "".equals(packages)) {
			return new String[0];
		}
		return packages.split(",");
	}

	@ConditionalOnMissingBean(SessionFactory.class)
	@Bean(name = "sessionFactory")
	public LocalSessionFactoryBean localSessionFactoryBean(@Autowired DataSource dataSource
			, @Qualifier("mappingLocations") Resource[] mappingLocations
			, @Qualifier("packagesToScan") String[] packagesToScan
			, @Autowired(required = false) ShardingContext shardingContext
			, @Value("${kuafu.hibernate.dialect:}") String dialect
			, @Value("${kuafu.hibernate.current-session-context-class:}") String sessionContext
			, @Value("${kuafu.hibernate.cache.region.factory-class:}") String cacheRegionFactory
			, @Value("${kuafu.hibernate.cache.provider-class:}") String cacheProviderClass
			, @Value("${kuafu.hibernate.cache.provider-configuration-file-resource-path:ehcache-default.xml}") String ehcachePath
			, @Value("${kuafu.hibernate.cache.use-query-cache:true}") String useQueryCache
			, @Value("${kuafu.hibernate.cache.use-second-level-cache:false}") String useSecondLevelCache
			, @Value("${kuafu.hibernate.cache.missing-cache-strategy:create}") String missingCacheStrategy
			, @Value("${kuafu.hibernate.show-sql:false}") String showSql
			, @Value("${kuafu.hibernate.jdbc.batch-size:20}") String batchSize
			, @Value("${kuafu.hibernate.connection.release-mode:auto}") String releaseMode
			, @Value("${kuafu.hibernate.session.factory.statement-inspector:}") String statementInspector) {
		if (dialect == null || "".equals(dialect.trim())) {
			dialect = org.hibernate.dialect.MySQLDialect.class.getName();
		}
		if (sessionContext == null || "".equals(sessionContext.trim())) {
			sessionContext = org.springframework.orm.hibernate5.SpringSessionContext.class.getName();
		}
		if (cacheRegionFactory == null || "".equals(cacheRegionFactory.trim())) {
			cacheRegionFactory = org.hibernate.cache.jcache.internal.JCacheRegionFactory.class.getName();
		}
		if (cacheProviderClass == null || "".equals(cacheProviderClass.trim())) {
			cacheProviderClass = org.ehcache.jsr107.EhcacheCachingProvider.class.getName();
		}
		if (statementInspector == null || "".equals(statementInspector.trim())) {
			statementInspector = DynamicTableInspector.class.getName();
		}

		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", dialect);
		properties.setProperty("hibernate.current_session_context_class", sessionContext);
		properties.setProperty("hibernate.cache.region.factory_class", cacheRegionFactory);
		properties.setProperty("hibernate.cache.provider_class", cacheProviderClass);
		properties.setProperty("hibernate.cache.provider_configuration_file_resource_path", ehcachePath);
		properties.setProperty("hibernate.cache.use_query_cache", useQueryCache);
		properties.setProperty("hibernate.cache.use_second_level_cache", useSecondLevelCache);
		properties.setProperty("hibernate.javax.cache.missing_cache_strategy", missingCacheStrategy);
		properties.setProperty("hibernate.show_sql", showSql);
		properties.setProperty("hibernate.jdbc.batch_size", batchSize);
		properties.setProperty("hibernate.connection.release_mode", releaseMode);
		properties.setProperty("hibernate.session_factory.statement_inspector", statementInspector);

		LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
		bean.setDataSource(dataSource);
		bean.setHibernateProperties(properties);
		bean.setMappingLocations(mappingLocations);
		bean.setPackagesToScan(packagesToScan);
		return bean;
	}

	@ConditionalOnMissingBean(TransactionManager.class)
	@Bean(name = "transactionManager")
	public HibernateTransactionManager transactionManager(@Qualifier("sessionFactory") SessionFactory sessionFactory) {
		HibernateTransactionManager transactionManager = new HibernateTransactionManager();
		transactionManager.setSessionFactory(sessionFactory);
		return transactionManager;
	}
}
