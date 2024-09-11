package com.yuweix.kuafu.sharding.aspect;


import com.yuweix.kuafu.sharding.Shardable;
import com.yuweix.kuafu.sharding.annotation.Shard;
import com.yuweix.kuafu.sharding.context.TableHolder;
import com.yuweix.kuafu.sharding.utils.ShardAopUtil;
import com.yuweix.kuafu.sharding.utils.ShardingUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;


/**
 * 逻辑表替换为物理表的切面。
 * 针对{@link Shardable}子类中的public方法，凡是参数中带有{@link Shard}注解的都会被执行。
 * @author yuwei
 */
@Aspect
public class TableAspect {
    @Pointcut("execution(public * com.yuweix.kuafu.sharding.Shardable+.*(..))")
    public void shard() {

    }

    @Around("shard()")
    public Object onShard(ProceedingJoinPoint point) throws Throwable {
        Object target = point.getTarget();
        if (!(target instanceof Shardable)) {
            return point.proceed();
        }

        Object shardingVal = ShardAopUtil.getAnnotationArgShardVal(point);
        if (shardingVal == null) {
            return point.proceed();
        }
        Shardable shardable = (Shardable) target;
        try {
            this.beforeSharding(shardable, shardingVal);
            return point.proceed();
        } finally {
            this.afterSharding();
        }
    }

    protected void beforeSharding(Shardable shardable, Object shardingVal) {
        if (shardingVal == null) {
            return;
        }
        Class<?> clz = shardable.getPersistClz();
        String srcTableName = ShardingUtil.getTableName(clz);
        String targetTableName = ShardingUtil.getPhysicalTableName(clz, shardingVal);
        TableHolder.set(srcTableName, targetTableName);
    }

    protected void afterSharding() {
        TableHolder.remove();
    }
}
