package com.yuweix.kuafu.dao.mybatis;


import java.io.Serializable;

import com.yuweix.kuafu.dao.mybatis.provider.DeleteSqlProvider;
import com.yuweix.kuafu.dao.mybatis.where.Criteria;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;


/**
 * @author yuwei
 */
public interface DeleteMapper<T extends Serializable, PK extends Serializable> {
	@DeleteProvider(type = DeleteSqlProvider.class, method = "delete")
	int delete(T t);

	@DeleteProvider(type = DeleteSqlProvider.class, method = "deleteByKey")
	int deleteByKey(@Param("id") PK id, @Param("clz") Class<T> clz);

	@UpdateProvider(type = DeleteSqlProvider.class, method = "deleteByCriteria")
	int deleteByCriteria(@Param("criteria") Criteria criteria, @Param("clz") Class<T> clz);
}
