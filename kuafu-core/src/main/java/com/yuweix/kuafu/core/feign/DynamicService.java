package com.yuweix.kuafu.core.feign;


import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


public interface DynamicService {
    @GetMapping("{url}")
    <T>T doGet(@PathVariable("url") String url, @SpringQueryMap Object params);

    @PostMapping("{url}")
    <T>T doPost(@PathVariable("url") String url, @RequestBody Object params);
}
