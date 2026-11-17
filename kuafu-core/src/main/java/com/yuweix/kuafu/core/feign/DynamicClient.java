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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class DynamicClient {
    private static final Logger log = LoggerFactory.getLogger(DynamicClient.class);

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String FORM_CONTENT_TYPE_UTF8 = FORM_CONTENT_TYPE + ";charset=UTF-8";
    /**
     * 这些头由 feign / 底层连接自己维护，原样透传会出问题：
     * content-length 与实际 body 不一致、host 指向错误、connection 影响连接复用；
     * accept-encoding 透传 gzip 后，底层客户端不会自动解压（它只解压自己加的那个头），
     * 拿到的就是压缩字节，JSON 反序列化直接失败。
     */
    private static final List<String> UNFORWARDABLE_HEADERS = Arrays.asList("content-length"
            , "transfer-encoding", "host", "connection", "accept-encoding");

    private final FeignBuilderCustomizer defaultCustomizer;
    private final FeignClientBuilder feignClientBuilder;
    private final Map<String, DynamicService> dynamicServiceCache = new ConcurrentHashMap<>();


    public DynamicClient(ApplicationContext appContext, long connectTimeout, long readTimeout) {
        this.feignClientBuilder = new FeignClientBuilder(appContext);
        this.defaultCustomizer = builder -> builder.decoder(feignDecoder())
                .options(new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, true))
                .requestInterceptor(template -> template.header(Constant.HEADER_X_TRACE_ID, MdcUtil.getTraceId()));
    }


    /**
     * 缓存键必须带上 url：feign 的 url 是在 build 时就固化进 target 的，
     * 只按 feignName + contextId 缓存，同一个 feignName 换一个 host 调用时会静默复用第一次的地址，
     * 表现为“请求打到了别的实例/别的租户”上。
     */
    private DynamicService getFeignClient(String feignName, String contextId, String url, FeignBuilderCustomizer customizer) {
        String cid = (contextId == null || contextId.isEmpty()) ? feignName : contextId;
        String cacheKey = feignName + "#" + cid + "#" + url;
        if (customizer == null) {
            return dynamicServiceCache.computeIfAbsent(cacheKey, key -> buildClient(feignName, cid, url, defaultCustomizer));
        }
        // 自定义 customizer 也走缓存：否则每次调用都新建 Feign 客户端（丢默认超时/解码器），构成资源耗尽
        DynamicService cached = dynamicServiceCache.get(cacheKey + "@" + System.identityHashCode(customizer));
        if (cached != null) {
            return cached;
        }
        DynamicService created = buildClient(feignName, cid, url, customizer);
        dynamicServiceCache.put(cacheKey + "@" + System.identityHashCode(customizer), created);
        return created;
    }

    private DynamicService buildClient(String feignName, String contextId, String url, FeignBuilderCustomizer customizer) {
        return this.feignClientBuilder.forType(DynamicService.class, feignName)
                .contextId(contextId)
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
            if (!HEADER_CONTENT_TYPE.equalsIgnoreCase(entry.getKey()) || entry.getValue() == null || entry.getValue().isEmpty()) {
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
        String path = normalizePath(request.getPath());
        Object params = request.getParams();
        Map<String, String> headers = sanitizeHeaders(request.getHeaders());
        Class<?> responseTypeClass = request.getResponseTypeClass();
        Type responseType = request.getResponseType();
        FeignBuilderCustomizer reqCustomizer = request.getCustomizer();
        boolean ignoreException = request.isIgnoreException();
        if (method == null) {
            throw new IllegalArgumentException("DynamicRequest.method不能为空");
        }
        if (feignName == null || feignName.trim().isEmpty()) {
            throw new IllegalArgumentException("DynamicRequest.feignName不能为空");
        }

        if (hasBody(method) && isForm(headers)) {
            // 表单：把 Map/POJO 转成 MultiValueMap，feign 侧会用 FormHttpMessageConverter 编码
            headers = withFormContentType(headers);
            params = toFormParams(params);
        } else if (params == null) {
            // @SpringQueryMap 不接受 null；body 传 null 编码器也会抛 EncodeException
            params = Collections.emptyMap();
        }

        long startTimeMillis = System.currentTimeMillis();
        try {
            log.info("DynamicClient请求开始, feignName: {}, contextId: {}, url: {}, path: {}, method: {}, contentType: {}"
                    , feignName, contextId, url, path, method, contentType(headers));
            if (log.isDebugEnabled()) {
                log.debug("DynamicClient请求参数, path: {}, method: {}, params: {}"
                        , path, method, abbreviate(JsonUtil.toJson(params)));
            }
            DynamicService dynamicService = getFeignClient(feignName, contextId, url, reqCustomizer);
            String str;
            if (DynamicRequest.Method.GET.equals(method)) {
                str = dynamicService.doGet(path, params, headers);
            } else if (DynamicRequest.Method.POST.equals(method)) {
                str = dynamicService.doPost(path, params, headers);
            } else if (DynamicRequest.Method.PUT.equals(method)) {
                str = dynamicService.doPut(path, params, headers);
            } else if (DynamicRequest.Method.PATCH.equals(method)) {
                str = dynamicService.doPatch(path, params, headers);
            } else if (DynamicRequest.Method.DELETE.equals(method)) {
                str = dynamicService.doDelete(path, params, headers);
            } else {
                log.error("Unsupported method: {}", method);
                throw new UnsupportedOperationException("Unsupported method: " + method);
            }
            if (str == null || str.trim().isEmpty()) {
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
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        str = str.trim();
        if (type == null || String.class.isAssignableFrom(type)) {
            return (T) str;
        } else if (byte[].class.isAssignableFrom(type)) {
            return (T) str.getBytes(StandardCharsets.UTF_8);
        }
        return (T) JsonUtil.toObject(str, type);
    }


    /**
     * feign 拼 URL 时是 target + uri 直接相连，path 少了开头的 "/" 会拼出 http://host:8080user/detail 这种地址
     */
    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        return path.charAt(0) == '/' ? path : "/" + path;
    }

    private static Map<String, String> sanitizeHeaders(Map<String, String> headers) {
        Map<String, String> result = new LinkedHashMap<>();
        if (headers == null) {
            return result;
        }
        headers.forEach((name, value) -> {
            if (name == null || value == null) {
                return;
            }
            if (UNFORWARDABLE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                return;
            }
            result.put(name, value);
        });
        return result;
    }

    private static String contentType(Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (HEADER_CONTENT_TYPE.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return JSON_CONTENT_TYPE;
    }

    private static boolean isForm(Map<String, String> headers) {
        return contentType(headers).toLowerCase(Locale.ROOT).startsWith(FORM_CONTENT_TYPE);
    }

    private static Map<String, String> withFormContentType(Map<String, String> headers) {
        Map<String, String> result = new LinkedHashMap<>(headers);
        result.keySet().removeIf(name -> HEADER_CONTENT_TYPE.equalsIgnoreCase(name));
        result.put(HEADER_CONTENT_TYPE, FORM_CONTENT_TYPE_UTF8);
        return result;
    }

    private static boolean hasBody(DynamicRequest.Method method) {
        return DynamicRequest.Method.POST == method
                || DynamicRequest.Method.PUT == method
                || DynamicRequest.Method.PATCH == method;
    }

    /**
     * 表单提交要求 body 是 MultiValueMap&lt;String, String&gt;：
     * feign-form 的 SpringFormEncoder 只对 MultiValueMap 走表单编码，普通 Map 会被当成 JSON 发出去；
     * 而值里一旦出现非字符串（如 Integer），它又会自动改写成 multipart，所以这里统一转成字符串。
     * String/byte[] 视为调用方已经拼好的报文，原样发送。
     */
    private static Object toFormParams(Object params) {
        if (params == null) {
            return new LinkedMultiValueMap<>();
        }
        if (params instanceof byte[] || params instanceof CharSequence) {
            return params;
        }
        Map<?, ?> map;
        if (params instanceof Map) {
            map = (Map<?, ?>) params;
        } else {
            map = JsonUtil.toObject(JsonUtil.toJson(params), Map.class);
            if (map == null) {
                return new LinkedMultiValueMap<>();
            }
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        map.forEach((k, v) -> {
            if (k != null && v != null) {
                addFormValue(form, String.valueOf(k), v);
            }
        });
        return form;
    }

    private static void addFormValue(MultiValueMap<String, String> form, String name, Object value) {
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                if (item != null) {
                    addFormValue(form, name, item);
                }
            }
            return;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(value, i);
                if (item != null) {
                    addFormValue(form, name, item);
                }
            }
            return;
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean
                || value instanceof Character || value instanceof Enum) {
            form.add(name, value.toString());
        } else {
            // 表单只支持平铺键值，对象类型的值按 JSON 字符串提交
            form.add(name, JsonUtil.toJson(value));
        }
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= 512 ? text : text.substring(0, 512) + "...";
    }
}
