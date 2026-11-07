package com.yuweix.kuafu.core.feign;


import com.yuweix.kuafu.core.serialize.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DynamicClient {
    private static final Logger log = LoggerFactory.getLogger(DynamicClient.class);

    private final FeignClientBuilder feignClientBuilder;
    private final Map<String, DynamicService> dynamicServiceCache = new ConcurrentHashMap<>();

    public DynamicClient(ApplicationContext appContext) {
        this.feignClientBuilder = new FeignClientBuilder(appContext);
    }

    private DynamicService getFeignClient(String feignName, String url) {
        String cacheKey = feignName + "#" + url;
        return dynamicServiceCache.computeIfAbsent(cacheKey,
                key -> this.feignClientBuilder.forType(DynamicService.class, feignName)
                        .url(url)
                        .build());
    }


    public Object doGet(String feignName, String path, Object params) {
        return doGet(feignName, "", path, params);
    }
    public Object doGet(String feignName, String url, String path, Object params) {
        try {
            DynamicService dynamicService = getFeignClient(feignName, url);
            return dynamicService.doGet(path, params);
        } catch (Exception ex) {
            log.error("【DynamicClient_GET请求】, feignName:{}, url:{}, path:{}, params:{}, error:{}"
                    , feignName, url, path, JsonUtil.toJson(params), ex.getMessage(), ex);
            return null;
        }
    }

    public Object doPost(String feignName, String path, Object params) {
        return doPost(feignName, "", path, params);
    }
    public Object doPost(String feignName, String url, String path, Object params) {
        try {
            DynamicService dynamicService = getFeignClient(feignName, url);
            return dynamicService.doPost(path, params);
        } catch (Exception ex) {
            log.error("【DynamicClient_POST请求】, feignName:{}, url:{}, path:{}, params:{}, error:{}"
                    , feignName, url, path, JsonUtil.toJson(params), ex.getMessage(), ex);
            return null;
        }
    }
}
