package com.yuweix.kuafu.sharding.annotation;


import java.lang.annotation.*;


/**
 * @author yuwei
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ShardingDataSource {
    String value();
}
