package com.yuweix.kuafu.permission.web.interceptor;


import com.yuweix.kuafu.core.ActionUtil;
import com.yuweix.kuafu.core.Response;
import com.yuweix.kuafu.permission.annotations.Permission;
import com.yuweix.kuafu.permission.common.PermissionUtil;
import com.yuweix.kuafu.permission.common.Properties;
import com.yuweix.kuafu.permission.dto.AdminDto;
import com.yuweix.kuafu.permission.dto.PermissionDto;
import com.yuweix.kuafu.permission.service.SysAdminService;
import com.yuweix.kuafu.permission.service.SysPermissionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * 权限检查拦截器
 * @author yuwei
 */
public class PermissionCheckInterceptor implements HandlerInterceptor {
	private static final Logger log = LoggerFactory.getLogger(PermissionCheckInterceptor.class);

	@Resource
	private SysPermissionService sysPermissionService;
	@Resource
	private SysAdminService sysAdminService;
	@Resource
	private Properties properties;


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}
		HandlerMethod hm = (HandlerMethod) handler;
		Permission perm = hm.getMethodAnnotation(Permission.class);
		if (perm == null) {
			return true;
		}
		String[] permNos = perm.value();
		if (permNos == null || permNos.length <= 0) {
			return true;
		}

		AdminDto adminDto = PermissionUtil.getLoginAccount();
		boolean ok = false;
		for (String permNo : permNos) {
			PermissionDto permissionDto = sysPermissionService.queryPermissionByNo(permNo);
			if (permissionDto == null) {
				continue;
			}
			boolean has = sysAdminService.hasPermission(adminDto.getId(), permissionDto.getId());
			if (has) {
				ok = true;
				break;
			}
		}
		if (ok) {
			return true;
		}
		outputErrorMsg(permNos);
		return false;
	}

	protected void outputErrorMsg(String[] permNos) {
		String errCode = properties.getNoAuthorityCode();
		String tips = String.format("[权限编码: %s]", Arrays.asList(permNos));
		log.error("没有权限[权限编码: {}]", Arrays.asList(permNos));
		String content = new Response<String, Void>(errCode, "没有权限" + tips).toString();
		ActionUtil.output(content.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON.getMimeType());
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler
			, ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}
}
