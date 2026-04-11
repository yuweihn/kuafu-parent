package com.yuweix.kuafu.core.feign.annotations;


import org.springframework.cloud.openfeign.FeignClient;
import java.lang.annotation.*;


/**
 * 在{@link FeignClient}不设置fallback属性的情况下，如果仍然希望做熔断降级，可使用{@link FallbackPlus}注解。优先级低于{@link FeignClient}的fallback属性。
 * 需搭配{@link FeignClient}注解一起使用。
 * @author yuwei
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface FallbackPlus {
    String errorCode() default "9999";
}
