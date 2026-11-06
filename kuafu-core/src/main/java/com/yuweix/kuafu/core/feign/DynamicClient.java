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

    private DynamicService getFeignClient(String feignName, String contextId, String url) {
        String cid = (contextId == null || contextId.isEmpty()) ? feignName : contextId;
        return dynamicServiceCache.computeIfAbsent(feignName + "#" + cid,
                key -> this.feignClientBuilder.forType(DynamicService.class, feignName)
                        .contextId(cid)
                        .url(url)
                        .build());
    }

    public <T>T doGet(String feignName, String contextId, String url, String path, Object params) {
        try {
            DynamicService dynamicService = getFeignClient(feignName, contextId, url);
            return dynamicService.doGet(path, params);
        } catch (Exception ex) {
            log.error("【DynamicClient_GET请求】, feignName:{}, contextId:{}, path:{}, params:{}, error:{}"
                    , feignName, contextId, path, JsonUtil.toJson(params), ex.getMessage(), ex);
            return null;
        }
    }

    public <T>T doPost(String feignName, String contextId, String url, String path, Object params) {
        try {
            DynamicService dynamicService = getFeignClient(feignName, contextId, url);
            return dynamicService.doPost(path, params);
        } catch (Exception ex) {
            log.error("【DynamicClient_POST请求】, feignName:{}, contextId:{}, path:{}, params:{}, error:{}"
                    , feignName, contextId, path, JsonUtil.toJson(params), ex.getMessage(), ex);
            return null;
        }
    }
}
