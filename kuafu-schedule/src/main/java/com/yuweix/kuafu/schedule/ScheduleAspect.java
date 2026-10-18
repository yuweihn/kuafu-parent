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

    @Pointcut("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void scheduled() {

    }

    @Around("executable() || scheduled()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object target = point.getTarget();
        if (!(target instanceof AbstractTask)) {
            return point.proceed();
        }

        return ((AbstractTask) target).execute(point::proceed);
    }
}
