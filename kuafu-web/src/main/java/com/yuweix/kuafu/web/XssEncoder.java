package com.yuweix.kuafu.web;



/**
 * XSS漏洞过滤
 */
public interface XssEncoder {
    String filter(String str);
}
