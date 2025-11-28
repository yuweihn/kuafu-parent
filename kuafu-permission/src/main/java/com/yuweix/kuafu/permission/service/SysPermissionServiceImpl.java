package com.yuweix.kuafu.permission.service;


import com.yuweix.kuafu.core.DateUtil;
import com.yuweix.kuafu.dao.mybatis.where.Criteria;
import com.yuweix.kuafu.dao.mybatis.where.Operator;
import com.yuweix.kuafu.permission.dao.SysAdminDao;
import com.yuweix.kuafu.permission.dao.SysPermissionDao;
import com.yuweix.kuafu.permission.dao.SysRolePermissionRelDao;
import com.yuweix.kuafu.permission.dto.*;
import com.yuweix.kuafu.permission.enums.PermType;
import com.yuweix.kuafu.permission.model.SysPermission;
import com.yuweix.kuafu.sequence.base.Sequence;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


/**
 * @author yuwei
 */
@Service("sysPermissionService")
public class SysPermissionServiceImpl implements SysPermissionService {
	@Resource
	private SysAdminDao sysAdminDao;
	@Resource
	private SysPermissionDao permissionDao;
	@Resource
	private SysRolePermissionRelDao sysRolePermissionRelDao;

	@Resource
	private Sequence seqSysPermission;


	@Override
	public List<PermissionDTO> queryPermissionListIncludeChildren(List<Long> idList, String keywords, List<String> permTypeList
			, Boolean visible) {
		List<SysPermission> permissionList = permissionDao.queryPermissionList(idList, null, keywords, permTypeList, visible
				, null, null);
		List<PermissionDTO> dtoList = permissionList == null || permissionList.size() <= 0
				? new ArrayList<>()
				: permissionList.stream().map(this::toPermissionDto).collect(Collectors.toList());
		return buildPermTree(dtoList);
	}
	private PermissionDTO toPermissionDto(SysPermission permission) {
		if (permission == null) {
			return null;
		}
		PermissionDTO dto = new PermissionDTO();
		dto.setId(permission.getId());
		dto.setPermNo(permission.getPermNo());
		dto.setTitle(permission.getTitle());
		dto.setParentId(permission.getParentId());
		dto.setOrderNum(permission.getOrderNum());
		dto.setPath(permission.getPath());
		dto.setComponent(permission.getComponent());
		dto.setIfExt(permission.isIfExt());
		dto.setPermType(permission.getPermType());
		dto.setPermTypeName(PermType.getNameByCode(permission.getPermType()));
		dto.setVisible(permission.isVisible());
		dto.setIcon(permission.getIcon());
		dto.setDescr(permission.getDescr());
		dto.setCreator(permission.getCreator());
		dto.setCreateTime(permission.getCreateTime() == null ? "" : DateUtil.formatDate(permission.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
		dto.setModifier(permission.getModifier());
		dto.setModifyTime(permission.getModifyTime() == null ? "" : DateUtil.formatDate(permission.getModifyTime(), "yyyy-MM-dd HH:mm:ss"));
		return dto;
	}
	/**
	 * 构建树状结构
	 */
	private <T extends AbstractTreeDTO<T>>List<T> buildPermTree(List<T> dtoList) {
		if (dtoList == null || dtoList.size() <= 0) {
			return dtoList;
		}
		CopyOnWriteArrayList<T> copyList = new CopyOnWriteArrayList<>(dtoList);
		List<Long> delIds = new ArrayList<>();
		for (T dto1 : copyList) {
			for (T dto2 : copyList) {
				if (dto1.getParentId() != null && dto1.getParentId() == dto2.getId()) {
					dto2.addChild(dto1);
					delIds.add(dto1.getId());
					break;
				}
			}
		}
		copyList.removeIf(dto1 -> delIds.contains(dto1.getId()));
		return copyList;
	}

	@Override
	public List<PermissionMenuTreeDTO> getMenuTreeListByAdminId(long adminId) {
		List<Long> permIdList = sysAdminDao.queryPermissionIdListByAdminId(adminId);
		if (permIdList == null) {
			permIdList = new ArrayList<>();
		}
		List<String> permTypeList = new ArrayList<>();
		permTypeList.add(PermType.DIR.getCode());
		permTypeList.add(PermType.MENU.getCode());

		List<SysPermission> permissionList = permissionDao.queryPermissionList(permIdList, null, null
				, permTypeList, null, null, null);
		List<PermissionMenuTreeDTO> dtoList = permissionList == null || permissionList.size() <= 0
				? new ArrayList<>()
				: permissionList.stream().map(this::toPermissionMenuTreeDto).collect(Collectors.toList());
		return buildPermTree(dtoList);
	}
	private PermissionMenuTreeDTO toPermissionMenuTreeDto(SysPermission permission) {
		if (permission == null) {
			return null;
		}
		PermissionMenuTreeDTO.Meta meta = new PermissionMenuTreeDTO.Meta();
		meta.setTitle(permission.getTitle());
		meta.setIcon(permission.getIcon());
		PermissionMenuTreeDTO dto = new PermissionMenuTreeDTO();
		dto.setId(permission.getId());
		dto.setPath(permission.getPath() == null ? "" : permission.getPath());
		dto.setName(permission.getPermNo() == null ? "" : permission.getPermNo());
		dto.setPermType(permission.getPermType());
		dto.setComponent(permission.getComponent());
		dto.setIfExt(permission.isIfExt());
		dto.setHidden(!permission.isVisible());
		dto.setMeta(meta);
		dto.setParentId(permission.getParentId());
		return dto;
	}

	@Override
	public List<String> getPermissionNoListByAdminId(long adminId) {
		List<Long> permIdList = sysAdminDao.queryPermissionIdListByAdminId(adminId);
		if (permIdList == null) {
			permIdList = new ArrayList<>();
		}
		List<String> permTypeList = new ArrayList<>();
		permTypeList.add(PermType.BUTTON.getCode());

		List<SysPermission> permissionList = permissionDao.queryPermissionList(permIdList, null, null
				, permTypeList, true, null, null);
		return permissionList == null || permissionList.size() <= 0
				? new ArrayList<>()
				: permissionList.stream().map(SysPermission::getPermNo)
				.filter(permNo -> permNo != null && !"".equals(permNo))
				.collect(Collectors.toList());
	}

	@Override
	public PermissionDTO queryPermissionById(long permissionId) {
		SysPermission permission = permissionDao.get(permissionId);
		return toPermissionDto(permission);
	}

	@Override
	public long addPermission(String permNo, String title, Long parentId, int orderNum, String path
			, String component, boolean ifExt, String permType, boolean visible
			, String icon, String descr, String creator) {
		SysPermission permission = permissionDao.queryPermissionByNo(permNo);
		if (permission != null) {
			throw new RuntimeException("权限编码已存在[" + permNo + "]");
		}

		long id = seqSysPermission.next();
		permission = new SysPermission();
		permission.setId(id);
		permission.setPermNo(permNo);
		permission.setTitle(title);
		permission.setParentId(parentId);
		permission.setOrderNum(orderNum);
		permission.setPath(path);
		permission.setComponent(component);
		permission.setIfExt(ifExt);
		permission.setPermType(permType);
		permission.setVisible(visible);
		permission.setIcon(icon);
		permission.setDescr(descr);
		permission.setCreator(creator);
		permission.setCreateTime(new Date());
		permissionDao.insertSelective(permission);
		return id;
	}

	@Override
	public void updatePermission(long id, String permNo, String title, Long parentId, int orderNum, String path
			, String component, boolean ifExt, String permType, boolean visible
			, String icon, String descr, String modifier) {
		SysPermission permission = permissionDao.get(id);
		if (permission == null) {
			throw new RuntimeException("该权限不存在[id=" + id + "]");
		}
		SysPermission permission2 = permissionDao.queryPermissionByNo(permNo);
		if (permission2 != null && permission2.getId() != id) {
			throw new RuntimeException("权限编码已存在[" + permNo + "]");
		}
		if (parentId != null && parentId == id) {
			throw new RuntimeException("上级权限不能选择自己");
		}

		permissionDao.deletePermissionByNoFromCache(permission.getPermNo());

		permission.setPermNo(permNo);
		permission.setTitle(title);
		permission.setParentId(parentId);
		permission.setOrderNum(orderNum);
		permission.setPath(path);
		permission.setComponent(component);
		permission.setIfExt(ifExt);
		permission.setPermType(permType);
		permission.setVisible(visible);
		permission.setIcon(icon);
		permission.setDescr(descr);
		permission.setModifier(modifier);
		permission.setModifyTime(new Date());
		permissionDao.updateByPrimaryKey(permission);
	}

	@Override
	public void deletePermission(long permissionId) {
		Criteria criteria = Criteria.of("perm_id", Operator.eq, permissionId);
		int cnt = sysRolePermissionRelDao.findCount(criteria);
		if (cnt > 0) {
			throw new RuntimeException("已绑定角色，不能删除");
		}

		int childCount = permissionDao.queryChildPermissionCount(permissionId);
		if (childCount > 0) {
			throw new RuntimeException("存在子权限，不能删除");
		}
		permissionDao.deleteByKey(permissionId);
	}

	@Override
	public void deletePermissions(List<Long> idList) {
		if (idList == null || idList.size() <= 0) {
			return;
		}
		List<PermissionIdDTO> idDtoList = new ArrayList<>();
		for (Long id: idList) {
			SysPermission perm = permissionDao.get(id);
			if (perm == null) {
				continue;
			}
			PermissionIdDTO idDto = new PermissionIdDTO();
			idDto.setId(perm.getId());
			idDto.setParentId(perm.getParentId());
			idDtoList.add(idDto);
		}
		deletePermissionTreeList(buildPermTree(idDtoList));
	}
	private void deletePermissionTreeList(List<PermissionIdDTO> list) {
		if (list == null || list.size() <= 0) {
			return;
		}
		for (PermissionIdDTO dto: list) {
			deletePermissionTreeList(dto.getChildren());
			deletePermission(dto.getId());
		}
	}

	@Override
	public PermissionDTO queryPermissionByNo(String permNo) {
		SysPermission permission = permissionDao.queryPermissionByNo(permNo);
		return toPermissionDto(permission);
	}

	@Override
	public PermissionExportDTO getPermissionExportDto() {
		List<SysPermission> list = permissionDao.queryPermissionList(null, null, null, null, null
				, null, null);
		List<PermissionDTO> dtoList = list == null || list.size() <= 0
				? new ArrayList<>()
				: list.stream().map(this::toPermissionDto).collect(Collectors.toList());
		return new PermissionExportDTO(buildPermTree(dtoList));
	}


	@Override
	public void doImport(Long parentId, List<PermissionDTO> list) {
		if (list == null || list.size() <= 0) {
			return;
		}
		for (PermissionDTO dto: list) {
			Long newParentId = doImport(dto.getPermNo(), dto.getTitle(), parentId, dto.getOrderNum(), dto.getPath()
					, dto.getComponent(), dto.getIfExt(), dto.getPermType(), dto.getVisible(), dto.getIcon(), dto.getDescr()
					, dto.getCreator(), dto.getCreateTime(), dto.getModifier(), dto.getModifyTime());
			doImport(newParentId, dto.getChildren());
		}
	}
	/**
	 * 按 permNo 覆盖数据
	 */
	private Long doImport(String permNo, String title, Long parentId, Integer orderNum, String path
			, String component, Boolean ifExt, String permType, Boolean visible, String icon, String descr
			, String creator, String createTime, String modifier, String modifyTime) {
		if (permNo == null || "".equals(permNo.trim())) {
			return null;
		}
		Date createTime0 = DateUtil.parseDateIgnoreE(createTime, "yyyy-MM-dd HH:mm:ss");
		Date modifyTime0 = DateUtil.parseDateIgnoreE(modifyTime, "yyyy-MM-dd HH:mm:ss");
		SysPermission perm = permissionDao.queryPermissionByNo(permNo.trim());
		if (perm == null) {
			perm = new SysPermission();
			perm.setId(seqSysPermission.next());
			perm.setPermNo(permNo.trim());
			perm.setTitle(title);
			perm.setParentId(parentId);
			perm.setOrderNum(orderNum == null ? 0 : orderNum);
			perm.setPath(path);
			perm.setComponent(component);
			perm.setIfExt(ifExt != null && ifExt);
			perm.setPermType(permType);
			perm.setVisible(visible != null && visible);
			perm.setIcon(icon);
			perm.setDescr(descr);
			perm.setCreator(creator);
			perm.setCreateTime(createTime0 == null ? new Date() : createTime0);
			perm.setModifier(modifier);
			perm.setModifyTime(modifyTime0);
			permissionDao.insert(perm);
			return perm.getId();
		} else {
			perm.setTitle(title);
			perm.setParentId(parentId);
			perm.setOrderNum(orderNum == null ? 0 : orderNum);
			perm.setPath(path);
			perm.setComponent(component);
			perm.setIfExt(ifExt != null && ifExt);
			perm.setPermType(permType);
			perm.setVisible(visible != null && visible);
			perm.setIcon(icon);
			perm.setDescr(descr);
			perm.setModifier(modifier);
			perm.setModifyTime(modifyTime0);
			permissionDao.updateByPrimaryKey(perm);
			return perm.getId();
		}
	}
}
