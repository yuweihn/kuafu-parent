package com.yuweix.kuafu.web.multipart;


import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * 基于内容的 MultipartFile 实现
 */
public class BytesMultipartFile implements MultipartFile {
    public static final String HTML_CONTENT_TYPE = "text/html";


    /**
     * 表单字段名（Multipart 的 name）
     */
    private final String name;
    /**
     * 原始文件名（必须带后缀）
     */
    private final String originalFilename;
    /**
     * Content-Type，如 text/html
     */
    private final String contentType;
    /**
     * 文件内容字节（按 charset 编码）
     */
    private final byte[] contentBytes;


    public BytesMultipartFile(String originalFilename, byte[] contentBytes, String contentType) {
        this("file", originalFilename, contentBytes, contentType, StandardCharsets.UTF_8);
    }

    public BytesMultipartFile(String name, String originalFilename, byte[] contentBytes, String contentType, Charset charset) {
        this.name = name == null || "".equals(name.trim()) ? "file" : name.trim();
        this.originalFilename = originalFilename;
        Charset cs = (charset == null ? StandardCharsets.UTF_8 : charset);
        this.contentType = contentType + "; charset=" + cs.name();
        this.contentBytes = contentBytes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return contentBytes.length == 0;
    }

    @Override
    public long getSize() {
        return contentBytes.length;
    }

    @Override
    public byte[] getBytes() {
        return contentBytes.clone();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(contentBytes);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        FileOutputStream fileOutputStream = new FileOutputStream(dest);
        fileOutputStream.write(contentBytes);
        fileOutputStream.close();
    }
}