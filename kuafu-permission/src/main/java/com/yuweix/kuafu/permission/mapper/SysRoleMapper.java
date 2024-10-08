package com.yuweix.kuafu.permission.mapper;


import com.yuweix.kuafu.dao.PersistUtil;
import com.yuweix.kuafu.permission.model.SysRole;
import com.yuweix.kuafu.dao.mybatis.BaseMapper;
import com.yuweix.kuafu.dao.mybatis.provider.AbstractProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.Map;


/**
 * @author yuwei
 */
public interface SysRoleMapper extends BaseMapper<SysRole, Long> {
	@SelectProvider(type = Provider.class, method = "queryRoleByNo")
	SysRole queryRoleByNo(@Param("roleNo")String roleNo);

	class Provider extends AbstractProvider {
		public String queryRoleByNo(Map<String, Object> param) {
			StringBuilder builder = new StringBuilder("");
			builder.append("  select ").append(PersistUtil.getAllColumnSql(SysRole.class))
					.append(" from ").append(PersistUtil.getTableName(SysRole.class))
					.append(" where role_no = #{roleNo} ");
			return builder.toString();
		}
	}
}
