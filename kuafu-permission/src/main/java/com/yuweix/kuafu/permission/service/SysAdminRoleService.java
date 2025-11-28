package com.yuweix.kuafu.permission.service;


import com.yuweix.kuafu.permission.dto.AdminRoleDTO;

import java.util.List;


/**
 * @author yuwei
 */
public interface SysAdminRoleService {
	int queryAdminRoleCountByAdminId(long adminId, String keywords);
	List<AdminRoleDTO> queryAdminRoleListByAdminId(long adminId, String keywords, int pageNo, int pageSize);
	
	/**
	 * 给指定人员分配角色；
	 */
	void addAdminRoleList(long adminId, List<Long> roleIdList, String modifier);
	
	long addAdminRole(long adminId, long roleId, String creator);
	AdminRoleDTO queryAdminRoleById(long id);
	void updateAdminRole(long id, long adminId, long roleId, String modifier);
	void deleteAdminRole(long id);

	boolean hasRole(long adminId, long roleId);
}
