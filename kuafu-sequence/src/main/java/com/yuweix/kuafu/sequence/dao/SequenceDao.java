package com.yuweix.kuafu.sequence.dao;


import com.yuweix.kuafu.sequence.bean.SequenceHolder;


/**
 * @author yuwei
 */
public interface SequenceDao {
	void ensure(String seqName, long minValue);
	SequenceHolder nextRange(String seqName);
}
