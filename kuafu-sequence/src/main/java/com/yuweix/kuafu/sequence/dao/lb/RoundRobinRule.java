package com.yuweix.kuafu.sequence.dao.lb;


import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author yuwei
 */
public class RoundRobinRule implements IRule {
	private volatile int segmentCount;
	private Map<String, AtomicInteger> curSegment = new ConcurrentHashMap<>();
	
	
	public RoundRobinRule() {
		this(1);
	}
	public RoundRobinRule(int segmentCount) {
		setSegmentCount(segmentCount);
	}
	
	
	@Override
	public int chooseSegment(String seqName) {
		Assert.isTrue(segmentCount > 0, "Field [segmentCount] cannot be less than 1, segmentCount = " + segmentCount);

		/**
		 * 查询下一个segment值，并保证范围在[0 ~ segmentCount-1]之间
		 */
		int idx = curSegment.computeIfAbsent(seqName, k -> new AtomicInteger(0)).incrementAndGet();
		return Math.floorMod(idx, segmentCount);
	}
	
	@Override
	public void setSegmentCount(int segmentCount) {
		Assert.isTrue(segmentCount > 0, "Parameter [segmentCount] cannot be less than 1, segmentCount = " + segmentCount);
		this.segmentCount = segmentCount;
	}
}
