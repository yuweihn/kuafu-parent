package com.yuweix.kuafu.dao.hibernate;


import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.util.Map;


/**
 * @author yuwei
 */
public class MapModifyCallback extends AbstractIntegerCallback {
	protected String sql;
	protected Map<String, Object> params;

	public MapModifyCallback(String sql, Map<String, Object> params) {
		this.sql = sql;
		this.params = params;
	}

	@Override
	public Integer doInHibernate(Session session) throws HibernateException {
		NativeQuery<Integer> query = session.createNativeQuery(sql, Integer.class);
		assembleParams(query, params);
		return query.executeUpdate();
	}
}
