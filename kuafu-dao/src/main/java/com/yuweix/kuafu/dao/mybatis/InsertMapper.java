package com.yuweix.kuafu.dao.mybatis;


import com.yuweix.kuafu.dao.mybatis.provider.InsertSqlProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;

import java.io.Serializable;


/**
 * @author yuwei
 */
public interface InsertMapper<T extends Serializable, PK extends Serializable> {
	@Options(useGeneratedKeys = true)
	@InsertProvider(type = InsertSqlProvider.class, method = "insert")
	int insert(T t);

	@Options(useGeneratedKeys = true)
	@InsertProvider(type = InsertSqlProvider.class, method = "insertSelective")
	int insertSelective(T t);
}
