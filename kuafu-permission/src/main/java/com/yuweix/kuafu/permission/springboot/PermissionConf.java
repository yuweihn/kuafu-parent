package com.yuweix.kuafu.permission.springboot;


import com.yuweix.kuafu.core.serialize.Serializer;
import com.yuweix.kuafu.permission.web.interceptor.PermissionCheckInterceptor;
import com.yuweix.kuafu.sequence.base.SequenceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwei
 */
@ComponentScan(basePackages = "com.yuweix.kuafu.permission", useDefaultFilters = true)
public class PermissionConf {
	@Bean
	@ConfigurationProperties(prefix = "kuafu.sequence", ignoreUnknownFields = true)
	public SequenceBean sequenceBean() {
		return new SequenceBean() {
			private Map<String, String> map = new HashMap<>();
			@Override
			public Map<String, String> getBeans() {
				return map;
			}
			@Override
			public Map<String, String> getBaseBeans() {
				Map<String, String> baseBeans = new HashMap<>();
				baseBeans.put("seqSysAdmin", "seq_sys_admin,200");
				baseBeans.put("seqSysPermission", "seq_sys_permission,200");
				baseBeans.put("seqSysRole", "seq_sys_role,200");
				baseBeans.put("seqSysRolePermissionRel", "seq_sys_role_permission_rel,200");
				baseBeans.put("seqSysAdminRoleRel", "seq_sys_admin_role_rel,200");
				return baseBeans;
			}
		};
	}

	@Bean(name = "serializerAdvice")
	public Object serializerAdvice(@Autowired(required = false) Serializer serializer) {
		if (serializer == null) {
			return null;
		}
		serializer.addAccept("com.yuweix.kuafu.permission.dto");
		serializer.addAccept("com.yuweix.kuafu.permission.model");
		return null;
	}

	@Bean(name = "basePackage")
	public String basePackage(Environment env) {
		String str = env.getProperty("kuafu.mybatis.base-package");
		String str0 = "com.yuweix.kuafu.permission.mapper**";
		if (str == null) {
			return str0;
		} else {
			return str0 + "," + str;
		}
	}

	@ConditionalOnMissingBean(PermissionCheckInterceptor.class)
	@Bean
	public PermissionCheckInterceptor permissionCheckInterceptor() {
		return new PermissionCheckInterceptor();
	}
}
