package com.yuweix.kuafu.permission.web;


import com.yuweix.kuafu.core.Response;
import com.yuweix.kuafu.permission.annotations.Permission;
import com.yuweix.kuafu.permission.common.PermissionUtil;
import com.yuweix.kuafu.permission.common.Properties;
import com.yuweix.kuafu.permission.dto.AdminDto;
import com.yuweix.kuafu.permission.dto.PageResponseDto;
import com.yuweix.kuafu.permission.dto.PermissionMenuTreeDto;
import com.yuweix.kuafu.permission.enums.EnumGender;
import com.yuweix.kuafu.permission.service.SysAdminService;
import com.yuweix.kuafu.permission.service.SysPermissionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;


/**
 * Sys管理员账户
 * @author yuwei
 */
@Controller
public class SysAdminController {
	@Resource
	private SysAdminService sysAdminService;
	@Resource
	private SysPermissionService sysPermissionService;
	@Resource
	private Properties properties;


	/**
	 * 管理员列表
	 */
	@Permission(value = "sys.admin.list")
	@RequestMapping(value = "/sys/admin/list", method = GET)
	@ResponseBody
	public Response<String, PageResponseDto<AdminDto>> queryAdminList(
			@RequestParam(value = "keywords", required = false) String keywords
			, @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo
			, @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		int size = sysAdminService.queryAdminCount(keywords);
		List<AdminDto> adminList = sysAdminService.queryAdminList(keywords, pageNo, pageSize);
		
		PageResponseDto<AdminDto> dto = new PageResponseDto<>();
		dto.setSize(size);
		dto.setList(adminList);
		return new Response<>(properties.getSuccessCode(), "ok", dto);
	}

	/**
	 * 查询管理员
	 */
	@Permission(value = "sys.admin.info")
	@RequestMapping(value = "/sys/admin/info", method = GET)
	@ResponseBody
	public Response<String, AdminDto> findAdminById(@RequestParam(value = "id", required = true) long accountId) {
		AdminDto adminDto = sysAdminService.findAdminById(accountId);
		if (adminDto != null) {
			adminDto.setPassword(null);
		}
		return new Response<>(properties.getSuccessCode(), "ok", adminDto);
	}

	/**
	 * 添加管理员
	 */
	@Permission(value = "sys.admin.create")
	@RequestMapping(value = "/sys/admin/create", method = POST)
	@ResponseBody
	public Response<String, Void> createAdmin(@RequestParam(value = "accountNo", required = true) String accountNo
			, @RequestParam(value = "password", required = true) String password
			, @RequestParam(value = "realName", required = false) String realName
			, @RequestParam(value = "gender", required = false) Byte gender) {
		AdminDto adminDto = PermissionUtil.getLoginAccount();
		sysAdminService.createAccount(accountNo, password, realName, gender, adminDto.getAccountNo());
		return new Response<>(properties.getSuccessCode(), "ok");
	}

	/**
	 * 更新管理员资料
	 */
	@Permission(value = "sys.admin.update")
	@RequestMapping(value = "/sys/admin/update", method = POST)
	@ResponseBody
	public Response<String, Void> updateAdmin(@RequestParam(value = "accountId", required = true) long accountId
			, @RequestParam(value = "realName", required = false) String realName
			, @RequestParam(value = "gender", required = false) Byte gender) {
		AdminDto adminDto = PermissionUtil.getLoginAccount();
		sysAdminService.updateAccount(accountId, realName, gender, adminDto.getAccountNo());
		return new Response<>(properties.getSuccessCode(), "ok");
	}

	/**
	 * 设置密码
	 */
	@Permission(value = "sys.admin.change.password")
	@RequestMapping(value = "/sys/admin/change-password", method = POST)
	@ResponseBody
	public Response<String, Void> changePassword(@RequestParam(value = "accountId", required = true) long accountId
			, @RequestParam(value = "password", required = true) String password) {
		AdminDto adminDto = PermissionUtil.getLoginAccount();
		sysAdminService.changePassword(accountId, password, adminDto.getAccountNo());
		return new Response<>(properties.getSuccessCode(), "ok");
	}

	/**
	 * 性别列表(下拉选择)
	 */
	@Permission(value = "sys.admin.gender.drop.down.list")
	@RequestMapping(value = "/sys/admin/gender/drop-down-list", method = GET)
	@ResponseBody
	public Response<String, List<Map<String, Object>>> queryGenderDropDownList() {
		EnumGender[] genders = EnumGender.values();
		List<Map<String, Object>> list = new ArrayList<>();
		if (genders != null && genders.length > 0) {
			for (EnumGender gender: genders) {
				Map<String, Object> map = new HashMap<>();
				map.put("id", gender.getCode());
				map.put("name", gender.getName());
				list.add(map);
			}
		}
		return new Response<>(properties.getSuccessCode(), "ok", list);
	}

	/**
	 * 删除管理员
	 */
	@Permission(value = "sys.admin.delete")
	@RequestMapping(value = "/sys/admin/delete", method = DELETE)
	@ResponseBody
	public Response<String, Void> deleteAdmin(@RequestParam(value = "ids", required = true) long[] ids) {
		for (long accountId: ids) {
			sysAdminService.deleteAccount(accountId);
		}
		return new Response<>(properties.getSuccessCode(), "ok");
	}

	/**
	 * 查询权限菜单列表
	 */
	@RequestMapping(value = "/sys/admin/permission/menu/list", method = GET)
	@ResponseBody
	public Response<String, List<PermissionMenuTreeDto>> getMenuTreeListByAdminId() {
		AdminDto adminDto = PermissionUtil.getLoginAccount();
		List<PermissionMenuTreeDto> menuList = sysPermissionService.getMenuTreeListByAdminId(adminDto.getId());
		return Response.of(properties.getSuccessCode(), "ok", menuList);
	}

	/**
	 * 查询权限按钮列表
	 */
	@RequestMapping(value = "/sys/admin/permission/button/list", method = GET)
	@ResponseBody
	public Response<String, List<String>> getPermissionNoListByAdminId() {
		AdminDto adminDto = PermissionUtil.getLoginAccount();
		List<String> permList = sysPermissionService.getPermissionNoListByAdminId(adminDto.getId());
		return Response.of(properties.getSuccessCode(), "ok", permList);
	}
}




