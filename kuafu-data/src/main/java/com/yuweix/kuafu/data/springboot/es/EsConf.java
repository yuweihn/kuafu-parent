package com.yuweix.kuafu.data.springboot.es;


import com.yuweix.kuafu.data.cache.Cache;
import com.yuweix.kuafu.data.es.common.ESConnectPool;
import com.yuweix.kuafu.data.es.common.ESConnector;
import com.yuweix.kuafu.data.es.common.Lock;
import com.yuweix.kuafu.data.es.service.EsService;
import com.yuweix.kuafu.data.es.service.EsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


/**
 * @author yuwei
 */
public class EsConf {
	@ConditionalOnMissingBean(ESConnector.class)
	@Bean
	public ESConnector esConnector(@Value("${kuafu.es.ip}")String serverIp, @Value("${kuafu.es.conn.poolSize:2}")int connPoolSize
			, @Value("${kuafu.es.conn.timeout:5000}")int connTimeout, @Value("${kuafu.es.socket.timeout:10000}")int socketTimeout
			, @Value("${kuafu.es.conn.request.timeout:5000}")int connRequestTimeout, @Value("${kuafu.es.max.acquire.times:5}")int maxAcquireTimes) {
		return new ESConnectPool(serverIp, connPoolSize, connTimeout, socketTimeout
				, connRequestTimeout, maxAcquireTimes);
	}

	@ConditionalOnMissingBean(EsService.class)
	@Bean
	public EsService esService(ESConnector esConnector, @Autowired(required = false) Cache cache) {
		EsServiceImpl esService = new EsServiceImpl(esConnector);
		if (cache != null) {
			esService.setLock(new Lock() {
				@Override
				public boolean lock(String key, String owner, long timeout) {
					return cache.lock(key, owner, timeout);
				}

				@Override
				public boolean unlock(String key, String owner) {
					return cache.unlock(key, owner);
				}
			});
		}
		return esService;
	}
}
