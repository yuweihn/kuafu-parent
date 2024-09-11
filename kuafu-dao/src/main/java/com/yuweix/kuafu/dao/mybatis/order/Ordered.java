package com.yuweix.kuafu.dao.mybatis.order;




/**
 * @author yuwei
 */
public enum Ordered {
	ASC("asc"),
	DESC("desc");

	private String code;

	Ordered(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
