package com.yuweix.kuafu.permission.dto;




/**
 * @author yuwei
 */
public abstract class AbstractTreeDTO<T extends AbstractTreeDTO<T>> {
	public abstract long getId();

	public abstract Long getParentId();

	public abstract T addChild(T child);
}
