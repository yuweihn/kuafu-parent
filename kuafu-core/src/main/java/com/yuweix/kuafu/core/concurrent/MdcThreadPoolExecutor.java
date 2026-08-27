package com.yuweix.kuafu.core.concurrent;


import com.yuweix.kuafu.core.MapUtil;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.*;


/**
 * @author yuwei
 **/
public class MdcThreadPoolExecutor extends ThreadPoolExecutor {
    public MdcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public MdcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit
            , BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public MdcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit
            , BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public MdcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit
            , BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(this.wrap(task));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(this.wrap(task));
    }

    @Override
    public void execute(Runnable task) {
        super.execute(this.wrap(task));
    }

    protected <T> Callable<T> wrap(final Callable<T> callable) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        if (context == null || context.isEmpty()) {
            return callable;
        }
        return () -> {
            runWithContext(context, callable);
            return null;
        };
    }

    protected Runnable wrap(final Runnable runnable) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        if (context == null || context.isEmpty()) {
            return runnable;
        }
        return () -> runWithContext(context, runnable);
    }

    private static void runWithContext(Map<String, String> context, Runnable task) {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        if (previous == null || previous.isEmpty()) {
            MDC.setContextMap(context);
        } else {
            MDC.setContextMap(MapUtil.mergeIgnoreNull(previous, context));
        }
        try {
            task.run();
        } finally {
            if (previous != null) {
                MDC.setContextMap(previous);
            } else {
                MDC.clear();
            }
        }
    }

    private static <T> void runWithContext(Map<String, String> context, Callable<T> task) throws Exception {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        if (previous == null || previous.isEmpty()) {
            MDC.setContextMap(context);
        } else {
            MDC.setContextMap(MapUtil.mergeIgnoreNull(previous, context));
        }
        try {
            task.call();
        } finally {
            if (previous != null) {
                MDC.setContextMap(previous);
            } else {
                MDC.clear();
            }
        }
    }
}
