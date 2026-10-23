package com.yuweix.kuafu.schedule.base;


import com.yuweix.kuafu.data.elect.Elector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yuwei
 */
public abstract class AbstractTask {
	private static final Logger log = LoggerFactory.getLogger(AbstractTask.class);
	private static final Elector DEFAULT_LEADER_ELECTOR = new Elector() {
		@Override
		public String acquire(String lock) {
			return getLocalNode();
		}
		@Override
		public void release(String lock) {
			//NO-OP
		}
		@Override
		public String getLocalNode() {
			return "local";
		}
	};

	private static final Map<Class<?>, Optional<Elector>> ELECTOR_CACHE = new ConcurrentHashMap<>();

	public AbstractTask() {

	}

	@FunctionalInterface
	public interface ExecutionBody {
		Object run() throws Throwable;
	}
	/**
	 * 选主骨架：抢到主才执行body，未抢到返回null
	 */
	public final Object execute(Elector injected, ExecutionBody body) throws Throwable {
		return execute(injected, null, body);
	}

	public final Object execute(Elector injected, String methodName, ExecutionBody body) throws Throwable {
		if (!isEnabled()) {
			return null;
		}

		Elector elector = resolveElector(injected);
		String lockName = getLockName(methodName);
		Elector.R res = elector.tryAcquire(lockName);
		if (!res.isSuccess()) {
			log.info("Not leader, job didn't execute! Job name: {}, Leader: {}", getClass().getName(), res.getLeader());
			return null;
		}

		long startTime = System.currentTimeMillis();
		boolean release = getRelease();
		Object result = null;
		try {
			before();
			result = body.run();
		} catch (Throwable ex) {
			handle(ex);
		} finally {
			if (release) {
				try {
					elector.release(lockName);
				} catch (Exception ignored) {
				}
			}
			after();
			long timeCost = System.currentTimeMillis() - startTime;
			log.info("Job executed here, Job name: {}, Local node: {}, Time cost: {}"
					, getClass().getName(), res.getLocal(), timeCost >= 1000 ? (timeCost / 1000.0) + "s" : timeCost + "ms");
		}
		return result;
	}

	protected boolean isEnabled() {
		return true;
	}

	protected void before() {

	}
	protected void handle(Throwable t) {
		log.error("任务执行失败, Error: {}", t.getMessage(), t);
	}
	protected void after() {

	}
	protected String getLockName() {
		return this.getClass().getName();
	}
	protected String getLockName(String methodName) {
		String base = getLockName();
		return methodName == null || methodName.isEmpty() ? base : base + "#" + methodName;
	}

	/**
	 * 是否释放Leader选择器
	 */
	protected boolean getRelease() {
		return true;
	}

	private Elector resolveElector(Elector injected) {
		Elector elector = getLeaderElector();
		if (elector != null) {
			return elector;
		}
		if (injected != null) {
			return injected;
		}
		return DEFAULT_LEADER_ELECTOR;
	}

	protected Elector getLeaderElector() {
		return ELECTOR_CACHE.computeIfAbsent(this.getClass(), k -> Optional.ofNullable(reflectElector(k))).orElse(null);
	}

	private Elector reflectElector(Class<?> clazz) {
		while (clazz != null) {
			for (Field f : clazz.getDeclaredFields()) {
				if (!Modifier.isStatic(f.getModifiers()) && f.getType() == Elector.class) {
					f.setAccessible(true);
					try {
						return (Elector) f.get(this);
					} catch (IllegalAccessException ex) {
						log.error("Reflect elector error, {}", ex.getMessage(), ex);
						throw new RuntimeException(ex);
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}
}
