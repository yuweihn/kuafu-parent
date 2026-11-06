package com.yuweix.kuafu.core.feign;


import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;


public class DynamicFeignClientFactory<T> {
    private final FeignClientBuilder feignClientBuilder;


    public DynamicFeignClientFactory(ApplicationContext appContext) {
        this.feignClientBuilder = new FeignClientBuilder(appContext);
    }

    public T getFeignClient(final Class<T> type, String serviceId) {
        return this.feignClientBuilder.forType(type, serviceId).build();
    }
}

