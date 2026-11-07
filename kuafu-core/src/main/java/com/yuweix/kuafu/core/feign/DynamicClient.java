package com.yuweix.kuafu.core.feign;


import com.yuweix.kuafu.core.serialize.JsonUtil;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeTypeUtils;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DynamicClient {
    private static final Logger log = LoggerFactory.getLogger(DynamicClient.class);

    private final FeignBuilderCustomizer customizer;
    private final FeignClientBuilder feignClientBuilder;
    private final Map<String, DynamicService> dynamicServiceCache = new ConcurrentHashMap<>();


    public DynamicClient(ApplicationContext appContext) {
        this.feignClientBuilder = new FeignClientBuilder(appContext);
        this.customizer = builder -> builder.decoder(feignDecoder());
    }


    private Decoder feignDecoder() {
        return (Response response, Type type) -> {
            Response.Body body = response.body();
            if (body == null) {
                return null;
            }
            byte[] bytes = Util.toByteArray(body.asInputStream());
            if (byte[].class.equals(type)) {
                return bytes;
            }
            String bodys = new String(bytes, charset(response));
            return bodys;
        };
    }
    private Charset charset(Response response) {
        for (Map.Entry<String, Collection<String>> entry : response.headers().entrySet()) {
            if (!"Content-Type".equalsIgnoreCase(entry.getKey()) || entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            try {
                Charset charset = MimeTypeUtils.parseMimeType(entry.getValue().iterator().next()).getCharset();
                return charset == null ? Util.UTF_8 : charset;
            } catch (InvalidMimeTypeException ignore) {

            }
        }
        return Util.UTF_8;
    }

    private DynamicService getFeignClient(String feignName, String url) {
        String cacheKey = feignName + "#" + url;
        return dynamicServiceCache.computeIfAbsent(cacheKey,
                key -> this.feignClientBuilder.forType(DynamicService.class, feignName)
                        .url(url)
                        .customize(customizer)
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
