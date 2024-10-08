package com.yuweix.kuafu.dao.mybatis.where;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yuwei
 */
public class Criteria implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private StringBuilder sql;
	private Map<String, Object> params;
	private int pindex;

	private Criteria() {
		sql = new StringBuilder("");
		params = new HashMap<String, Object>();
		pindex = 0;
	}

	public static Criteria of(String key, Operator operator) {
		return of(key, operator, null);
	}
	public static Criteria of(String key, Operator operator, Object value) {
		Criteria criteria = new Criteria();
		String criterionSql = createCriterionSql(key, operator, value, criteria.params, criteria.hashCode(), ++criteria.pindex);
		criteria.sql.append(criterionSql);
		return criteria;
	}
	private static String createCriterionSql(String key, Operator operator, Object value, Map<String, Object> params
			, int hashCode, int pindex) throws IllegalArgumentException {
		if (key == null || "".equals(key.trim()) || operator == null) {
			throw new IllegalArgumentException("Invalid argument.");
		}

		String paramKey = "p" + hashCode +  "" + pindex;
		if (operator.getCode().equals(Operator.in.getCode()) || operator.getCode().equals(Operator.nin.getCode())) {
			if ((value == null) || !(value instanceof Collection)) {
				return "";
			} else {
				Collection<?> col = (Collection<?>) value;
				if (col.size() <= 0) {
					if (operator.getCode().equals(Operator.in.getCode())) {
						return " 1 = 0 ";
					} else {
						return " 1 = 1 ";
					}
				} else {
					params.put(paramKey, value);
					return key + " " + operator.getCode() + " <foreach item='item' collection='criteria.params." + paramKey + "' open='(' separator=',' close=')'>#{item}</foreach> ";
				}
			}
		} else if (value == null) {
			return key + " " + operator.getCode() + " ";
		} else {
			params.put(paramKey, value);
			return key + " " + operator.getCode() + " #{criteria.params." + paramKey + "} ";
		}
	}

	private Criteria add(Connector connector, String criterionSql) {
		if (criterionSql != null && !"".equals(criterionSql.trim())) {
			sql.append(" ")
				.append(connector.getCode())
				.append(" ")
				.append(criterionSql)
				.append(" ");
		}
		return this;
	}
	public Criteria and(String key, Operator operator) {
		return and(key, operator, null);
	}
	public Criteria and(String key, Operator operator, Object value) {
		String criterionSql = createCriterionSql(key, operator, value, this.params, this.hashCode(), ++this.pindex);
		return add(Connector.and, criterionSql);
	}
	public Criteria and(String sql) {
		return add(Connector.and, sql);
	}
	public Criteria or(String key, Operator operator) {
		return or(key, operator, null);
	}
	public Criteria or(String key, Operator operator, Object value) {
		String criterionSql = createCriterionSql(key, operator, value, this.params, this.hashCode(), ++this.pindex);
		return add(Connector.or, criterionSql);
	}
	public Criteria or(String sql) {
		return add(Connector.or, sql);
	}

	private Criteria add(Connector connector, Criteria criteria) {
		params.putAll(criteria.getParams());
		String criteriaSql = criteria.toSql();
		if (criteriaSql != null && !"".equals(criteriaSql.trim())) {
			sql.insert(0, "(")
				.append(") ")
				.append(connector.getCode())
				.append(" (")
				.append(criteriaSql)
				.append(") ");
		}
		return this;
	}
	public Criteria and(Criteria criteria) {
		return add(Connector.and, criteria);
	}
	public Criteria or(Criteria criteria) {
		return add(Connector.or, criteria);
	}

	public String toSql() {
		return sql.toString();
	}

	public Map<String, Object> getParams() {
		return params;
	}
}
