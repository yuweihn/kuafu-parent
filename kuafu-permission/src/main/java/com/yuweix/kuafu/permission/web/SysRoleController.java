package com.yuweix.kuafu.permission.web;


import com.yuweix.kuafu.core.Response;
import com.yuweix.kuafu.permission.annotations.Permission;
import com.yuweix.kuafu.permission.common.PermissionUtil;
import com.yuweix.kuafu.permission.common.Properties;
import com.yuweix.kuafu.permission.dto.AdminDTO;
import com.yuweix.kuafu.permission.dto.PageResponseDTO;
import com.yuweix.kuafu.permission.dto.RoleDTO;
import com.yuweix.kuafu.permission.service.SysRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;


/**
 * Sys角色管理
 * @author yuwei
 */
@Controller
public class SysRoleController {
	@Resource
	private SysRoleService sysRoleService;
	@Resource
	private Properties properties;


	/**
	 * 角色列表
	 */
	@Permission(value = "sys.role.list")
	@RequestMapping(value = "/sys/role/list", method = GET)
	@ResponseBody
	public Response<String, PageResponseDTO<RoleDTO>> queryRoleList(@RequestParam(value = "keywords", required = false) String keywords
			, @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
			, @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		int count = sysRoleService.queryRoleCount(keywords);
		List<RoleDTO> roleList = sysRoleService.queryRoleList(keywords, pageNo, pageSize);
		PageResponseDTO<RoleDTO> dto = new PageResponseDTO<>();
		dto.setSize(count);
		dto.setList(roleList);
		return new Response<>(properties.getSuccessCode(), "ok", dto);
	}

	/**
	 * 查询角色详情
	 */
	@Permission(value = "sys.role.info")
	@RequestMapping(value = "/sys/role/info", method = GET)
	@ResponseBody
	public Response<String, RoleDTO> queryRoleInfo(@RequestParam(value = "id", required = true) long id) {
		RoleDTO role = sysRoleService.queryRoleById(id);
		if (role == null) {
			return new Response<>(properties.getFailureCode(), "无数据");
		} else {
			return new Response<>(properties.getSuccessCode(), "ok", role);
		}
	}

	/**
	 * 添加角色
	 */
	@Permission(value = "sys.role.create")
	@RequestMapping(value = "/sys/role/create", method = POST)
	@ResponseBody
	public Response<String, Void> createRole(@RequestParam(value = "roleNo", required = true)String roleNo
			, @RequestParam(value = "roleName", required = true) String roleName) {
		AdminDTO adminDto = PermissionUtil.getLoginAccount();
		sysRoleService.addRole(roleNo.trim(), roleName, adminDto.getAccountNo());
		return new Response<>(properties.getSuccessCode(), "ok");
	}

	/**
	 * 修改角色
	 */
	@Permission(value = "sys.role.update")
	@RequestMapping(value = "/sys/role/update", method = POST)
	@ResponseBody
	public Response<String, Void> updateRole(@RequestParam(value = "id", required = true) long id
			, @RequestParam(value = "roleNo", required = true)String roleNo
			, @RequestParam(value = "roleName", required = true) String roleName) {
		AdminDTO adminDto = PermissionUtil.getLoginAccount();
		sysRoleService.updateRole(id, roleNo.trim(), roleName, adminDto.getAccountNo());
		return new Response<>(properties.getSuccessCode(), "ok");
	}

	/**
	 * 删除角色
	 */
	@Permission(value = "sys.role.delete")
	@RequestMapping(value = "/sys/role/delete", method = DELETE)
	@ResponseBody
	public Response<String, Void> deleteRole(@RequestParam(value = "ids", required = true)long[] ids) {
		for (long id: ids) {
			sysRoleService.deleteRole(id);
		}
		return new Response<>(properties.getSuccessCode(), "ok");
	}
}
