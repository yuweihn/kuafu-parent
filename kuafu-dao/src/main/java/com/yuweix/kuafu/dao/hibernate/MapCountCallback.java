package com.yuweix.kuafu.dao.hibernate;


import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.SelectionQuery;

import java.util.Map;


/**
 * @author yuwei
 */
public class MapCountCallback extends AbstractIntegerCallback {
	protected String sql;
	protected Map<String, Object> params;

	public MapCountCallback(String sql, Map<String, Object> params) {
		this.sql = sql;
		this.params = params;
	}

	@Override
	public Integer doInHibernate(Session session) throws HibernateException {
		SelectionQuery<?> query = session.createSelectionQuery(sql);
		assembleParams(query, params);
		return Integer.parseInt(query.uniqueResult().toString());
	}
}
