package com.yuweix.kuafu.data.es.service;


import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.util.ObjectBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * @author yuwei
 */
public interface EsService {
	/**
	 * 检查集群状态
	 */
	boolean ping();

	boolean isIndexExists(String index);

	void createIndex(String index, int shards, int replicas);
	/**
	 * 创建索引
	 * @param index     索引名称
	 * @param shards    分片数目
	 * @param replicas  分片的副本数
	 */
	void createIndex(String index, TypeMapping mapping, int shards, int replicas);

	/**
	 * 删除索引
	 */
	void deleteIndex(String index);

	/**
	 * 删除文档
	 */
	void delete(String index, String id);
	void delete(String index, List<String> ids);
	void deleteByQuery(String index, Query query);

	Map<String, Object> findById(String index, String id);

	/**
	 * 存入数据
	 */
	boolean insertList(String index, List<Map<String, Object>> list);

	long queryTotal(List<String> indices, Query query);

	List<Map<String, Object>> getDataList(List<String> indices, Integer pageNo, Integer pageSize);
	List<Map<String, Object>> getDataList(List<String> indices
			, Query query, Integer pageNo, Integer pageSize);
	List<Map<String, Object>> getDataList(List<String> indices, String filterFieldName, SortOrder sortOrder
			, Query query, Integer pageNo, Integer pageSize);

	Map<String, Aggregate> getAggDataList(List<String> indices, Query query
			, String aggKey, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> aggFn);
	Map<String, Aggregate> getAggDataList(List<String> indices, Query query
			, String aggKey, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> aggFn
			, String filterFieldName, SortOrder sortOrder);
}
