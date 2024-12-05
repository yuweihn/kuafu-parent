package com.yuweix.kuafu.dao.springboot;


import com.alibaba.druid.pool.DruidDataSource;
import com.yuweix.kuafu.dao.datasource.DynamicDataSource;
import com.yuweix.kuafu.dao.datasource.DynamicDataSourceAspect;
import com.yuweix.kuafu.dao.mybatis.SQLInterceptor;
import com.yuweix.kuafu.sharding.aspect.DataSourceAspect;
import com.yuweix.kuafu.sharding.context.ShardingContext;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwei
 */
@EnableTransactionManagement(proxyTargetClass = true)
public class MybatisConf {
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

	@ConditionalOnMissingBean(name = "targetDataSources")
	@Bean(name = "targetDataSources")
	public Map<String, DataSource> targetDataSources() {
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
			, @Qualifier("targetDataSources") Map<String, DataSource> targetDataSources) {
		if (targetDataSources == null) {
			targetDataSources = new HashMap<>();
		}

		DynamicDataSource dds = new DynamicDataSource();
		dds.setLenientFallback(lenient);
		dds.setDefaultTargetDataSource(defaultDataSource);
		dds.setTargetDataSources(new HashMap<>(targetDataSources));
		return dds;
	}

	@ConditionalOnMissingBean(name = "mapperLocations")
	@Bean(name = "mapperLocations")
	public Resource[] mapperLocations(@Value("${kuafu.mybatis.mapper.location-pattern:}") String locationPattern) throws IOException {
		if (locationPattern == null || "".equals(locationPattern)) {
			return new Resource[0];
		}
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		return resolver.getResources(locationPattern);
	}

	@ConditionalOnMissingBean(name = "basePackage")
	@Bean(name = "basePackage")
	public String basePackage(Environment env) {
		return env.getProperty("kuafu.mybatis.base-package");
	}

	@ConditionalOnMissingBean(name = "sqlInterceptor")
	@Bean(name = "sqlInterceptor")
	public Interceptor sqlInterceptor() {
		return new SQLInterceptor();
	}

	@ConditionalOnMissingBean(name = "pluginInterceptors")
	@Bean(name = "pluginInterceptors")
	public Interceptor[] pluginInterceptors(@Qualifier("sqlInterceptor") Interceptor sqlInterceptor) {
		return new Interceptor[]{sqlInterceptor};
	}

	@ConditionalOnMissingBean(SqlSessionFactory.class)
	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactoryBean sqlSessionFactoryBean(@Autowired DataSource dataSource
			, @Autowired(required = false) ShardingContext shardingContext
			, @Qualifier("mapperLocations") Resource[] mapperLocations
			, @Qualifier("pluginInterceptors") Interceptor[] pluginInterceptors) {
		SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
		sessionFactoryBean.setDataSource(dataSource);
		sessionFactoryBean.setPlugins(pluginInterceptors);
		if (mapperLocations != null && mapperLocations.length > 0) {
			sessionFactoryBean.setMapperLocations(mapperLocations);
		}
		return sessionFactoryBean;
	}

	@ConditionalOnMissingBean(SqlSessionTemplate.class)
	@Bean(name = "sqlSessionTemplate")
	public SqlSessionTemplate SqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@ConditionalOnMissingBean(MapperScannerConfigurer.class)
	@Bean(name = "mapperScannerConf")
	public MapperScannerConfigurer mapperScannerConf(@Qualifier("basePackage") String basePackage) {
		MapperScannerConfigurer conf = new MapperScannerConfigurer();
//		conf.setSqlSessionFactoryBeanName("sqlSessionFactory");
		conf.setSqlSessionTemplateBeanName("sqlSessionTemplate");
		conf.setBasePackage(basePackage);
		return conf;
	}

	@ConditionalOnMissingBean(TransactionManager.class)
	@Bean(name = "transactionManager")
	public DataSourceTransactionManager transactionManager(@Autowired DataSource dataSource) {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
		transactionManager.setDataSource(dataSource);
		return transactionManager;
	}

	@ConditionalOnMissingBean(TransactionTemplate.class)
	@Bean(name = "transactionTemplate")
	public TransactionTemplate transactionTemplate(@Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
		return new TransactionTemplate(transactionManager);
	}
}
