package com.yuweix.kuafu.core.feign;


import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


public interface DynamicService {
    @GetMapping("{path}")
    <T>T doGet(@PathVariable("path") String path, @SpringQueryMap Object params);

    @PostMapping("{path}")
    <T>T doPost(@PathVariable("path") String path, @RequestBody Object params);
}
