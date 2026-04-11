package com.yuweix.kuafu.core.feign.aspect;


import com.yuweix.kuafu.core.JsonUtil;
import com.yuweix.kuafu.core.Response;
import com.yuweix.kuafu.core.SpringContext;
import com.yuweix.kuafu.core.feign.annotations.FallbackPlus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;

import java.lang.reflect.Method;


/**
 * 拦截带{@link FeignClient}注解的类中的方法，在异常发生时调用补偿方法
 * @author yuwei
 */
@Aspect
public class FeignFallbackAspect {
    private static final Logger log = LoggerFactory.getLogger(FeignFallbackAspect.class);


    /**
     * 切入点
     */
    @Pointcut("@within(org.springframework.cloud.openfeign.FeignClient)")
    public void pointcut() {

    }

    @Around("pointcut()")
    public Object advice(ProceedingJoinPoint point) throws Throwable {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        try {
            log.info("Method: {}, Params: {}", method.getDeclaringClass().getSimpleName() + "." + method.getName(), point.getArgs());
            Object result = point.proceed();
            log.info("Result: {}", JsonUtil.toString(result));
            return result;
        } catch (Exception ex) {
            log.error("Feign调用异常，Error: {}", ex.getMessage());
            return doFallback(method, point.getArgs(), ex);
        }
    }
    private Object doFallback(Method method, Object[] args, Exception ex) throws Throwable {
        Class<?> clz = method.getDeclaringClass();
        if (!clz.isAnnotationPresent(FeignClient.class)) {
            throw ex;
        }
        FeignClient fc = clz.getAnnotation(FeignClient.class);
        Class<?> fallbackClz = fc.fallback();
        Object targetObj = null;
        Method targetMethod = null;
        FallbackPlus fallbackPlus = null;
        if (fallbackClz != null && clz.isAssignableFrom(fallbackClz)) {
            targetObj = SpringContext.getBeanIgnoreNull(fallbackClz);
            if (targetObj == null) {
                try {
                    targetObj = fallbackClz.getDeclaredConstructor().newInstance();
                } catch (Exception ignored) {}
            }
            targetMethod = fallbackClz.getMethod(method.getName(), method.getParameterTypes());
        }
        /**
         * 在没有fallback配置的情况下，启用{@link FallbackPlus}注解
         */
        if (method.isAnnotationPresent(FallbackPlus.class)) {
            fallbackPlus = method.getAnnotation(FallbackPlus.class);
        }
        if (fallbackPlus == null && clz.isAnnotationPresent(FallbackPlus.class)) {
            fallbackPlus = clz.getAnnotation(FallbackPlus.class);
        }
        if (targetObj != null && targetMethod != null) {
            return targetMethod.invoke(targetObj, args);
        } else if (fallbackPlus != null) {
            Class<?> returnType = method.getReturnType();
            if (Response.class.isAssignableFrom(returnType)) {
                String err = getErrorMessage(fc);
                return new Response<>(fallbackPlus.errorCode(), (err == null || err.isEmpty()) ? ex.getMessage() : err);
            } else if (byte.class.isAssignableFrom(returnType) || short.class.isAssignableFrom(returnType)
                    || int.class.isAssignableFrom(returnType) || long.class.isAssignableFrom(returnType)
                    || float.class.isAssignableFrom(returnType) || double.class.isAssignableFrom(returnType)) {
                return 0;
            } else if (boolean.class.isAssignableFrom(returnType)) {
                return false;
            } else if (char.class.isAssignableFrom(returnType)) {
                return (char) 0;
            } else {
                return null;
            }
        }
        throw ex;
    }
    private String getErrorMessage(FeignClient fc) {
        if (fc == null) {
            return null;
        }
        String serviceName = fc.name();
        return serviceName == null || serviceName.isEmpty() ? "服务请求失败！" : "[" + serviceName + "]服务请求失败！";
    }
}
