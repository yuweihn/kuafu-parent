package com.yuweix.kuafu.permission.dao;


import com.yuweix.kuafu.dao.mybatis.Dao;
import com.yuweix.kuafu.permission.dto.SysAdminRoleDTO;
import com.yuweix.kuafu.permission.model.SysAdminRoleRel;

import java.util.List;


/**
 * @author yuwei
 */
public interface SysAdminRoleRelDao extends Dao<SysAdminRoleRel, Long> {
	/**
	 * 数据来源于下面几个表，当这几个表的数据有变化，则清掉缓存中的当前数据
	 * SysAdminRoleRel
	 */
	boolean hasRole(long adminId, long roleId);
	void deleteHasRoleFromCache(long adminId, long roleId);
	
	int queryAdminRoleCountByAdminId(Long adminId, Long roleId, String keywords);
	List<SysAdminRoleDTO> queryAdminRoleListByAdminId(Long adminId, Long roleId, String keywords, int pageNo, int pageSize);
	
	/**
	 * 数据来源于下面几个表，当这几个表的数据有变化，则清掉缓存中的当前数据
	 * SysAdminRoleRel
	 */
	SysAdminRoleRel queryByAdminIdAndRoleId(long adminId, long roleId);
	void deleteByAdminIdAndRoleIdFromCache(long adminId, long roleId);
}
