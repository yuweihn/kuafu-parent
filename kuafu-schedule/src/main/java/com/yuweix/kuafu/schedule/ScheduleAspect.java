package com.yuweix.kuafu.schedule;


import com.yuweix.kuafu.schedule.base.AbstractTask;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;


/**
 * @author yuwei
 */
@Aspect
public class ScheduleAspect {
    @Pointcut("@annotation(com.yuweix.kuafu.schedule.Executable)")
    public void executable() {

    }

    @Around("executable()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object target = point.getTarget();
        if (!(target instanceof AbstractTask)) {
            return point.proceed();
        } else {
            ((AbstractTask) target).start();
            return null;
        }
    }
}
