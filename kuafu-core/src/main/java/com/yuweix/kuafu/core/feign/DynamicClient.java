package com.yuweix.kuafu.core.feign;


import com.yuweix.kuafu.core.serialize.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;


public class DynamicClient {
    private static final Logger log = LoggerFactory.getLogger(DynamicClient.class);

    private final FeignClientBuilder feignClientBuilder;

    public DynamicClient(ApplicationContext appContext) {
        this.feignClientBuilder = new FeignClientBuilder(appContext);
    }

    private <T>T getFeignClient(final Class<T> type, String serviceId) {
        return this.feignClientBuilder.forType(type, serviceId).build();
    }

    public <T>T doGet(String feignName, String url, Object params) {
        try {
            DynamicService dynamicService = getFeignClient(DynamicService.class, feignName);
            return dynamicService.doGet(url, params);
        } catch (Exception ex) {
            log.error("【DynamicClient_GET请求】, feignName:{}, url:{}, params:{}, error:{}"
                    , feignName, url, JsonUtil.toJson(params), ex.getMessage(), ex);
            return null;
        }
    }

    public <T>T doPost(String feignName, String url, Object params) {
        try {
            DynamicService dynamicService = getFeignClient(DynamicService.class, feignName);
            return dynamicService.doPost(url, params);
        } catch (Exception ex) {
            log.error("【DynamicClient_POST请求】, feignName:{}, url:{}, params:{}, error:{}"
                    , feignName, url, JsonUtil.toJson(params), ex.getMessage(), ex);
            return null;
        }
    }
}
