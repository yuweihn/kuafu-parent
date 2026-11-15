package com.yuweix.kuafu.core.feign;


import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 动态 feign 客户端：接口、路径、参数、请求头全部由调用方决定。
 * <p>
 * 说明：
 * <ol>
 *     <li>{@code path} 必须以 "/" 开头（如 "/user/detail"），否则 feign 拼出来的 URL 会少一个分隔符。</li>
 *     <li>GET/DELETE 的参数走 query，params 传 Map 或 POJO，不能传 null。</li>
 *     <li>POST/PUT/PATCH 的参数走 body，格式由 headers 里的 Content-Type 决定：
 *      不传或传 application/json 时按 JSON 序列化；传 application/x-www-form-urlencoded 时按表单提交。</li>
 * </ol>
 *
 * @author yuwei
 */
public interface DynamicService {
    @GetMapping("{path}")
    <T> T doGet(@PathVariable("path") String path, @SpringQueryMap Object params
            , @RequestHeader Map<String, String> headers);

    @PostMapping("{path}")
    <T> T doPost(@PathVariable("path") String path, @RequestBody(required = false) Object params
            , @RequestHeader Map<String, String> headers);

    @PutMapping("{path}")
    <T> T doPut(@PathVariable("path") String path, @RequestBody(required = false) Object params
            , @RequestHeader Map<String, String> headers);

    @PatchMapping("{path}")
    <T> T doPatch(@PathVariable("path") String path, @RequestBody(required = false) Object params
            , @RequestHeader Map<String, String> headers);

    @DeleteMapping("{path}")
    <T> T doDelete(@PathVariable("path") String path, @SpringQueryMap Object params
            , @RequestHeader Map<String, String> headers);
}
