package com.yuweix.kuafu.core.feign;


import com.yuweix.kuafu.core.Constant;
import com.yuweix.kuafu.core.MdcUtil;
import com.yuweix.kuafu.core.serialize.JsonUtil;
import feign.Request;
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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class DynamicClient {
    private static final Logger log = LoggerFactory.getLogger(DynamicClient.class);

    private final FeignBuilderCustomizer customizer;
    private final FeignClientBuilder feignClientBuilder;
    private final Map<String, DynamicService> dynamicServiceCache = new ConcurrentHashMap<>();


    public DynamicClient(ApplicationContext appContext, long connectTimeout, long readTimeout) {
        this.feignClientBuilder = new FeignClientBuilder(appContext);
        this.customizer = builder -> builder.decoder(feignDecoder())
                .options(new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, true))
                .requestInterceptor(template -> template.header(Constant.HEADER_X_TRACE_ID, MdcUtil.getTraceId()));
    }


    private DynamicService getFeignClient(String feignName, String contextId, String url) {
        String cacheKey = feignName + "#" + (contextId == null || contextId.isEmpty() ? feignName : contextId);
        return dynamicServiceCache.computeIfAbsent(cacheKey, key -> buildClient(feignName, contextId, url, customizer));
    }

    private DynamicService buildClient(String feignName, String contextId, String url, FeignBuilderCustomizer customizer) {
        String effectiveContextId = (contextId == null || contextId.isEmpty()) ? feignName : contextId;
        return this.feignClientBuilder.forType(DynamicService.class, feignName)
                .contextId(effectiveContextId)
                .url(url)
                .customize(customizer)
                .build();
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

    public <T>T call(DynamicRequest request) {
        String feignName = request.getFeignName();
        String contextId = request.getContextId();
        DynamicRequest.Method method = request.getMethod();
        String url = request.getUrl();
        String path = request.getPath();
        Object params = request.getParams();
        Map<String, String> headers = request.getHeaders();
        if (headers == null) {
            headers = Collections.emptyMap();
        }
        Class<?> responseTypeClass = request.getResponseTypeClass();
        Type responseType = request.getResponseType();
        FeignBuilderCustomizer reqCustomizer = request.getCustomizer();
        boolean ignoreException = request.isIgnoreException();

        long startTimeMillis = System.currentTimeMillis();
        try {
            log.info("DynamicClient请求开始, feignName: {}, contextId: {}, url: {}, path: {}, method: {}, params: {}"
                    , feignName, contextId, url, path, method, JsonUtil.toJson(params));
            DynamicService dynamicService = reqCustomizer != null
                    ? buildClient(feignName, contextId, url, reqCustomizer)
                    : getFeignClient(feignName, contextId, url);
            String str = null;
            if (DynamicRequest.Method.GET.equals(method)) {
                str = dynamicService.doGet(path, params, headers);
            } else if (DynamicRequest.Method.POST.equals(method)) {
                str = dynamicService.doPost(path, params, headers);
            } else if (DynamicRequest.Method.PUT.equals(method)) {
                str = dynamicService.doPut(path, params, headers);
            } else if (DynamicRequest.Method.DELETE.equals(method)) {
                str = dynamicService.doDelete(path, params, headers);
            } else {
                log.error("Unsupported method: {}", method);
                throw new UnsupportedOperationException("Unsupported method: " + method);
            }
            if (str == null || str.trim().equals("")) {
                return null;
            }
            if (responseType != null) {
                return JsonUtil.toObject(str, responseType);
            }
            if (responseTypeClass != null) {
                return parse(str, responseTypeClass);
            }
            return (T) str;
        } catch (Exception ex) {
            log.error("DynamicClient请求失败, feignName: {}, contextId: {}, url: {}, path: {}, method: {}, error: {}"
                    , feignName, contextId, url, path, method, ex.getMessage(), ex);
            if (ignoreException) {
                return null;
            }
            throw new RuntimeException("DynamicClient请求失败", ex);
        } finally {
            log.info("DynamicClient请求结束, feignName: {}, contextId: {}, url: {}, path: {}, method: {}, cost: {}ms"
                    , feignName, contextId, url, path, method, System.currentTimeMillis() - startTimeMillis);
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
