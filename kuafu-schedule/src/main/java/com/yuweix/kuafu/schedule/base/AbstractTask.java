package com.yuweix.kuafu.schedule.base;


import com.yuweix.kuafu.data.elect.Elector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
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

	private static SoftReference<Map<Class<? extends AbstractTask>, ElectorWrapper>> ELECTOR_MAP_REF;
	private static final Object electorLock = new Object();
	private static final class ElectorWrapper {
		private Elector elector = null;
		ElectorWrapper(Elector elector) {
			this.elector = elector;
		}
	}
	private static final ElectorWrapper EMPTY_ELECTOR = new ElectorWrapper(null);

	public AbstractTask() {

	}

	@FunctionalInterface
	public interface ExecutionBody {
		Object run() throws Throwable;
	}
	/**
	 * 选主骨架：抢到主才执行 body，未抢到返回 null
	 */
	public final Object execute(Elector elector, ExecutionBody body) throws Throwable {
		if (!isEnabled()) {
			return null;
		}
		long startTime = System.currentTimeMillis();
		Elector localElector = getLeaderElector();
		if (localElector == null) {
			localElector = elector;
		}
		if (localElector == null) {
			localElector = DEFAULT_LEADER_ELECTOR;
		}

		String lockName = getLockName();
		boolean release = getRelease();
		Elector.R res = localElector.tryAcquire(lockName);
		if (!res.isSuccess()) {
			log.info("Not leader, job didn't execute! Job name: {}, Leader: {}", this.getClass().getName(), res.getLeader());
			return null;
		}

		Object result = null;
		try {
			before();
			result = body.run();
		} catch (Throwable ex) {
			handle(ex);
		} finally {
			if (release) {
				try {
					localElector.release(lockName);
				} catch (Exception ignored) {
				}
			}
			after();
			long timeCost = System.currentTimeMillis() - startTime;
			log.info("Job executed here, Job name: {}, Local node: {}, Time cost: {}"
					, this.getClass().getName(), res.getLocal(), timeCost >= 1000 ? (timeCost / 1000.0) + "s" : timeCost + "ms");
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

	/**
	 * 是否释放Leader选择器
	 */
	protected boolean getRelease() {
		return true;
	}

	private static Map<Class<? extends AbstractTask>, ElectorWrapper> getElectorWrapperMap() {
		Map<Class<? extends AbstractTask>, ElectorWrapper> map = null;
		if (ELECTOR_MAP_REF == null || (map = ELECTOR_MAP_REF.get()) == null) {
			synchronized (electorLock) {
				if (ELECTOR_MAP_REF == null || (map = ELECTOR_MAP_REF.get()) == null) {
					map = new ConcurrentHashMap<>();
					ELECTOR_MAP_REF = new SoftReference<>(map);
				}
			}
		}
		return map;
	}
	protected Elector getLeaderElector() {
		Class<? extends AbstractTask> clz = this.getClass();
		Map<Class<? extends AbstractTask>, ElectorWrapper> map = getElectorWrapperMap();
		ElectorWrapper wrapper = map.get(clz);
		if (wrapper == null) {
			synchronized (this) {
				wrapper = map.get(clz);
				if (wrapper == null) {
					wrapper = reflectElector(clz);
					map.put(clz, wrapper);
				}
			}
		}
		return wrapper.elector;
	}
	private ElectorWrapper reflectElector(Class<?> clazz) {
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field f: fields) {
				if (!Modifier.isStatic(f.getModifiers()) && f.getType() == Elector.class) {
					f.setAccessible(true);
					try {
						return new ElectorWrapper((Elector) f.get(this));
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		return EMPTY_ELECTOR;
	}
}
