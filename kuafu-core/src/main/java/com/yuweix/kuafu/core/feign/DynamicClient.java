package com.yuweix.kuafu.core.feign;


import com.yuweix.kuafu.core.serialize.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DynamicClient {
    private static final Logger log = LoggerFactory.getLogger(DynamicClient.class);

    private final DynamicFeignClientFactory<DynamicService> dynamicFeignClientFactory;

    public DynamicClient(DynamicFeignClientFactory<DynamicService> dynamicFeignClientFactory) {
        this.dynamicFeignClientFactory = dynamicFeignClientFactory;
    }


    public <T>T doGet(String feignName, String url, Object params) {
        try {
            DynamicService dynamicService = dynamicFeignClientFactory.getFeignClient(DynamicService.class, feignName);
            return dynamicService.doGet(url, params);
        } catch (Exception ex) {
            log.error("【DynamicClient_GET请求】, feignName:{}, url:{}, params:{}, error:{}"
                    , feignName, url, JsonUtil.toJson(params), ex.getMessage(), ex);
            return null;
        }
    }

    public <T>T doPost(String feignName, String url, Object params) {
        try {
            DynamicService dynamicService = dynamicFeignClientFactory.getFeignClient(DynamicService.class, feignName);
            return dynamicService.doPost(url, params);
        } catch (Exception ex) {
            log.error("【DynamicClient_POST请求】, feignName:{}, url:{}, params:{}, error:{}"
                    , feignName, url, JsonUtil.toJson(params), ex.getMessage(), ex);
            return null;
        }
    }
}
