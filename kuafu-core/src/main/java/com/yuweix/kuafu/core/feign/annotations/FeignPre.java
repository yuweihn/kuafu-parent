package com.yuweix.kuafu.core.feign.annotations;


import com.yuweix.kuafu.core.feign.PreHandler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * @author yuwei
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface FeignPre {
	Class<? extends PreHandler> value();
}