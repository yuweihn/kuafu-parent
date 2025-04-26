package com.yuweix.kuafu.web.filter;


import com.yuweix.kuafu.core.io.StreamUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author yuwei
 */
public class CacheBodyRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String[]> parameters = new LinkedHashMap<>();
    private byte[] requestBody;

    public CacheBodyRequestWrapper(HttpServletRequest request) {
        super(request);
        this.init(request);
    }

    private void init(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null && !parameterMap.isEmpty()) {
            this.parameters.putAll(parameterMap);
        }
        this.setRequestBody(request);
    }

    public byte[] getRequestBody() {
        return this.requestBody;
    }

    private void setRequestBody(ServletRequest request) {
        try {
            InputStream in = request.getInputStream();
            this.requestBody = StreamUtil.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getCharacterEncoding() {
        String encode = super.getCharacterEncoding();
        return encode == null ? StandardCharsets.UTF_8.name() : encode;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(this.parameters);
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.requestBody);
        return new ServletInputStream() {
            public int read() {
                return byteArrayInputStream.read();
            }

            public boolean isFinished() {
                return false;
            }

            public boolean isReady() {
                return false;
            }

            public void setReadListener(ReadListener readListener) {

            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public String toString() {
        return "CacheBodyRequestWrapper(parameters=" + this.parameters + ", requestBody=" + Arrays.toString(this.requestBody) + ")";
    }
}
