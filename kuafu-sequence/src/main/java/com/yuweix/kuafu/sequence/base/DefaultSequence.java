package com.yuweix.kuafu.sequence.base;



/**
 * @author yuwei
 */
public class DefaultSequence extends AbstractSequence {
	@Override
	protected long nextVal() {
		long value = sequenceHolder.getAndIncrement();
		if (value <= 0L) {
			lock.lock();
			try {
				do {
					sequenceHolder = sequenceDao.nextRange(name);
					value = sequenceHolder.getAndIncrement();
				} while (value <= 0L);
			} finally {
				lock.unlock();
			}
		}
		return value;
	}
}
