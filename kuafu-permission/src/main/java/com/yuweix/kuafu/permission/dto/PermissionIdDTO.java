package com.yuweix.kuafu.permission.dto;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author yuwei
 */
public class PermissionIdDTO extends AbstractTreeDTO<PermissionIdDTO> implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	private Long parentId;
	private List<PermissionIdDTO> children = new ArrayList<>();


	public PermissionIdDTO addChild(PermissionIdDTO child) {
		children.add(child);
		return this;
	}

	//////////////////////////////////////////////////////////////////////////////
	@Override
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public List<PermissionIdDTO> getChildren() {
		return children;
	}

	public void setChildren(List<PermissionIdDTO> children) {
		this.children = children;
	}
}
