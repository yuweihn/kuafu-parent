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
import java.nio.charset.StandardCharsets;
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
            if (bytes == null || bytes.length == 0) {
                return null;
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
                return charset == null ? StandardCharsets.UTF_8 : charset;
            } catch (InvalidMimeTypeException ignore) {

            }
        }
        return StandardCharsets.UTF_8;
    }

    private DynamicService getFeignClient(String feignName, String url) {
        String cacheKey = feignName + "#" + url;
        return dynamicServiceCache.computeIfAbsent(cacheKey,
                key -> this.feignClientBuilder.forType(DynamicService.class, feignName)
                        .url(url)
                        .customize(customizer)
                        .build());
    }

    public <T>T call(DynamicRequest request) {
        String feignName = request.getFeignName();
        DynamicRequest.Method method = request.getMethod();
        String url = request.getUrl();
        String path = request.getPath();
        Object params = request.getParams();
        Class<?> responseTypeClass = request.getResponseTypeClass();
        Type type = request.getResponseType();

        long startTimeMillis = System.currentTimeMillis();
        try {
            log.info("DynamicClient请求开始, feignName:{}, url:{}, path:{}, params:{}"
                    , feignName, url, path, JsonUtil.toJson(params));
            DynamicService dynamicService = getFeignClient(feignName, url);
            String str = null;
            if (DynamicRequest.Method.GET.equals(method)) {
                str = dynamicService.doGet(path, params);
            } else if (DynamicRequest.Method.POST.equals(method)) {
                str = dynamicService.doPost(path, params);
            } else {
                log.error("Unsupported method: {}", method);
                throw new UnsupportedOperationException("Unsupported method: " + method);
            }
            if (str == null || str.trim().equals("")) {
                return null;
            }
            if (type != null) {
                return JsonUtil.toObject(str, type);
            }
            if (responseTypeClass != null) {
                return parse(str, responseTypeClass);
            }
            return (T) str;
        } catch (Exception ex) {
            log.error("DynamicClient请求失败, feignName:{}, url:{}, path:{}, error:{}"
                    , feignName, url, path, ex.getMessage(), ex);
            return null;
        } finally {
            log.info("DynamicClient请求结束, feignName:{}, url:{}, path:{}, cost: {}ms"
                    , feignName, url, path, System.currentTimeMillis() - startTimeMillis);
        }
    }

    private <T>T parse(String str, Class<?> type) {
        if (str == null || str.trim().equals("")) {
            return null;
        }
        str = str.trim();
        if (type == null || String.class.isAssignableFrom(type)) {
            return (T) str;
        } else if (byte[].class.isAssignableFrom(type)) {
            return (T) str.getBytes();
        }
        return (T) JsonUtil.toObject(str, type);
    }
}
