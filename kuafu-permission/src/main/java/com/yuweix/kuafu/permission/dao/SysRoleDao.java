package com.yuweix.kuafu.permission.dao;


import com.yuweix.kuafu.dao.mybatis.Dao;
import com.yuweix.kuafu.permission.model.SysRole;


/**
 * @author yuwei
 */
public interface SysRoleDao extends Dao<SysRole, Long> {
    SysRole queryRoleByNo(String roleNo);
    void deleteRoleByNoFromCache(String roleNo);
}
