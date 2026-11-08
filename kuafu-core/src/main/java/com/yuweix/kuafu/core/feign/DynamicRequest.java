package com.yuweix.kuafu.core.feign;


import java.lang.reflect.Type;


public class DynamicRequest {
    private String feignName;
    private FeignMethod method;
    private String url;
    private String path;
    private Object params;
    private Class<?> responseTypeClass;
    private Type responseType;


    private DynamicRequest() {
        this.responseTypeClass = String.class;
    }
    public String getFeignName() {
        return feignName;
    }
    public FeignMethod getMethod() {
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
    public Class<?> getResponseTypeClass() {
        return responseTypeClass;
    }
    public Type getResponseType() {
        return responseType;
    }

    public static Builder builder() {
        return new Builder();
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
        public Builder method(FeignMethod method) {
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
        public Builder responseType(Class<?> responseTypeClass) {
            dynamicRequest.responseTypeClass = responseTypeClass;
            return this;
        }
        public Builder responseType(Type responseType) {
            dynamicRequest.responseType = responseType;
            return this;
        }
        public DynamicRequest build() {
            return dynamicRequest;
        }
    }
}
