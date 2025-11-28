package com.yuweix.kuafu.permission.dto;


import java.io.Serializable;
import java.util.List;


/**
 * @author yuwei
 */
public class RolePermissionDTO implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private List<PermissionDTO> permList;
	private List<Long> checkedPermIdList;

	public List<PermissionDTO> getPermList() {
		return permList;
	}

	public void setPermList(List<PermissionDTO> permList) {
		this.permList = permList;
	}

	public List<Long> getCheckedPermIdList() {
		return checkedPermIdList;
	}

	public void setCheckedPermIdList(List<Long> checkedPermIdList) {
		this.checkedPermIdList = checkedPermIdList;
	}
}

