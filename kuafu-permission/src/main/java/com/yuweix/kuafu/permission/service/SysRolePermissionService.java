package com.yuweix.kuafu.permission.service;


import java.util.List;


/**
 * @author yuwei
 */
public interface SysRolePermissionService {
	List<Long> queryPermissionIdListByRoleId(long roleId);

	void saveRolePermission(long roleId, List<Long> permIdList, String modifier);
}
