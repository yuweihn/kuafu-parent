package com.yuweix.kuafu.permission.service;


import com.yuweix.kuafu.permission.dto.PermissionDto;
import com.yuweix.kuafu.permission.dto.PermissionExportDto;
import com.yuweix.kuafu.permission.dto.PermissionMenuTreeDto;

import java.util.List;


/**
 * @author yuwei
 */
public interface SysPermissionService {
	List<PermissionDto> queryPermissionListIncludeChildren(List<Long> idList, String keywords, List<String> permTypeList
			, Boolean visible);

	PermissionDto queryPermissionById(long permissionId);

	/**
	 * 查询指定人员的权限菜单
	 */
	List<PermissionMenuTreeDto> getMenuTreeListByAdminId(long adminId);

	/**
	 * 查询指定人员的权限按钮
	 */
	List<String> getPermissionNoListByAdminId(long adminId);

	/**
	 * 添加权限
	 * 返回新加的权限id
	 */
	long addPermission(String permNo, String title, Long parentId, int orderNum, String path
			, String component, boolean ifExt, String permType, boolean visible
			, String icon, String descr, String creator);
	void updatePermission(long id, String permNo, String title, Long parentId, int orderNum, String path
			, String component, boolean ifExt, String permType, boolean visible
			, String icon, String descr, String modifier);
	void deletePermission(long permissionId);
	void deletePermissions(List<Long> idList);

	PermissionDto queryPermissionByNo(String permNo);

	PermissionExportDto getPermissionExportDto();
	void doImport(Long parentId, List<PermissionDto> list);
}
