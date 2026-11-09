package com.yuweix.kuafu.core.feign;


import feign.Param;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


public interface DynamicService {
    @GetMapping("{path}")
    <T>T doGet(@Param("path") @PathVariable("path") String path, @SpringQueryMap Object params, @RequestHeader Map<String, String> headers);

    @PostMapping("{path}")
    <T>T doPost(@Param("path") @PathVariable("path") String path, @RequestBody Object params, @RequestHeader Map<String, String> headers);

    @PutMapping("{path}")
    <T>T doPut(@Param("path") @PathVariable("path") String path, @RequestBody Object params, @RequestHeader Map<String, String> headers);

    @DeleteMapping("{path}")
    <T>T doDelete(@Param("path") @PathVariable("path") String path, @SpringQueryMap Object params, @RequestHeader Map<String, String> headers);
}
