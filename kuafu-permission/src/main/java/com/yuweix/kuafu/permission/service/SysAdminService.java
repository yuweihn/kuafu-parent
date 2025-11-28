package com.yuweix.kuafu.permission.service;


import com.yuweix.kuafu.core.Response;
import com.yuweix.kuafu.permission.dto.AdminDTO;

import java.util.List;


/**
 * @author yuwei
 */
public interface SysAdminService {
	long createAccount(String accountNo, String password, String realName, Byte gender, String creator);
	void updateAccount(long id, String realName, Byte gender, String modifier);
	AdminDTO findAdminById(long id);
	Response<Boolean, AdminDTO> login(String accountNo, String password);
	void changePassword(long id, String password, String modifier);
	void changePassword(long id, String oldPassword, String password, String modifier);
	void changeAvatar(long id, String avatar, String modifier);
	void deleteAccount(long id);
	int queryAdminCount(String keywords);
	List<AdminDTO> queryAdminList(String keywords, int pageNo, int pageSize);
	
	/**
	 * 查询某人是否有某权限
	 * @param adminId
	 * @param permissionId
	 * @return
	 */
	boolean hasPermission(long adminId, long permissionId);

	List<Long> queryPermissionIdListByAdminId(long adminId);
}
