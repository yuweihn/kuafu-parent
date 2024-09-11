package com.yuweix.kuafu.web;



public class DefaultXssEncoder implements XssEncoder {
    @Override
    public String filter(String str) {
        return XssUtil.filter(str);
    }
}
