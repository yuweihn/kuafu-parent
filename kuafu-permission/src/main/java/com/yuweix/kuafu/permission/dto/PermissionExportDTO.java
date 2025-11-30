package com.yuweix.kuafu.permission.dto;


import com.yuweix.kuafu.core.encrypt.SecurityUtil;
import com.yuweix.kuafu.core.JsonUtil;

import java.io.Serializable;
import java.util.List;


/**
 * @author yuwei
 */
public class PermissionExportDTO implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final String SALT = "Rewfe5..;;43po6baasfpcxr034;;',";

	private List<PermissionDTO> list;
	private String sign;

	public PermissionExportDTO() {

	}
	public PermissionExportDTO(List<PermissionDTO> list) {
		this.setList(list);
	}


	public boolean verify() {
		return this.toSgin().equals(this.sign);
	}

	private String toSgin() {
		return SecurityUtil.getMd5(JsonUtil.toString(list) + SALT);
	}

	public List<PermissionDTO> getList() {
		return list;
	}

	public void setList(List<PermissionDTO> list) {
		this.list = list;
		this.sign = this.toSgin();
	}

	public String getSign() {
		return sign;
	}
}
