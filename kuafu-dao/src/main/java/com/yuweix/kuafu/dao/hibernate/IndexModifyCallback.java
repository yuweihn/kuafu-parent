package com.yuweix.kuafu.dao.hibernate;


import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.MutationQuery;


/**
 * @author yuwei
 */
public class IndexModifyCallback extends AbstractIntegerCallback {
	protected String sql;
	protected Object[] params;

	public IndexModifyCallback(String sql, Object[] params) {
		this.sql = sql;
		this.params = params;
	}

	@Override
	public Integer doInHibernate(Session session) throws HibernateException {
		MutationQuery query = session.createNativeMutationQuery(sql);
		assembleParams(query, params);
		return query.executeUpdate();
	}
}
