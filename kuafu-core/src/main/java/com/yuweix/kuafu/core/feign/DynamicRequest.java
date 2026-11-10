package com.yuweix.kuafu.core.feign;


import org.springframework.cloud.openfeign.FeignBuilderCustomizer;

import java.lang.reflect.Type;
import java.util.Map;


public class DynamicRequest {
    private String feignName;
    private String contextId;
    private Method method;
    private String url;
    private String path;
    private Object params;
    private Map<String, String> headers;
    private Class<?> responseTypeClass;
    private Type responseType;
    private FeignBuilderCustomizer customizer;
    private boolean ignoreException;

    private DynamicRequest() {
        this.responseTypeClass = String.class;
        this.ignoreException = false;
    }
    public String getFeignName() {
        return feignName;
    }
    public String getContextId() {
        return contextId;
    }
    public Method getMethod() {
        return method;
    }
    public String getUrl() {
        return url;
    }
    public String getPath() {
        return path;
    }
    public Object getParams() {
        return params;
    }
    public Map<String, String> getHeaders() {
        return headers;
    }
    public Class<?> getResponseTypeClass() {
        return responseTypeClass;
    }
    public Type getResponseType() {
        return responseType;
    }
    public FeignBuilderCustomizer getCustomizer() {
        return customizer;
    }
    public boolean isIgnoreException() {
        return ignoreException;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum Method {
        GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;
    }

    public static final class Builder {
        private final DynamicRequest dynamicRequest;

        private Builder() {
            dynamicRequest = new DynamicRequest();
        }
        public Builder feignName(String feignName) {
            dynamicRequest.feignName = feignName;
            return this;
        }
        public Builder contextId(String contextId) {
            dynamicRequest.contextId = contextId;
            return this;
        }
        public Builder method(Method method) {
            dynamicRequest.method = method;
            return this;
        }
        public Builder url(String url) {
            dynamicRequest.url = url;
            return this;
        }
        public Builder path(String path) {
            dynamicRequest.path = path;
            return this;
        }
        public Builder params(Object params) {
            dynamicRequest.params = params;
            return this;
        }
        public Builder headers(Map<String, String> headers) {
            dynamicRequest.headers = headers;
            return this;
        }
        public Builder customizer(FeignBuilderCustomizer customizer) {
            dynamicRequest.customizer = customizer;
            return this;
        }
        public Builder responseType(Class<?> responseTypeClass) {
            dynamicRequest.responseTypeClass = responseTypeClass;
            return this;
        }
        public Builder responseType(Type responseType) {
            dynamicRequest.responseType = responseType;
            return this;
        }
        public Builder ignoreException(boolean ignoreException) {
            dynamicRequest.ignoreException = ignoreException;
            return this;
        }
        public DynamicRequest build() {
            return dynamicRequest;
        }
    }
}
