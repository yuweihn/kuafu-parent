package com.yuweix.kuafu.data.es.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.util.ObjectBuilder;
import com.yuweix.kuafu.data.es.common.ESConnector;
import com.yuweix.kuafu.data.es.common.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;


/**
 * @author yuwei
 */
public class EsServiceImpl implements EsService {
	private static final Logger log = LoggerFactory.getLogger(EsServiceImpl.class);

	private ESConnector esConnector;
	private Lock lock;

	private static final String DEFAULT_FILTER_FIELD_NAME = "createTime";
	private static final TypeMapping DEFAULT_MAPPING;
	static {
		try {
			Property dateProperty = Property.of(pBuilder -> pBuilder.date(
					tBuilder -> tBuilder.format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")));
			DEFAULT_MAPPING = new TypeMapping.Builder().properties(DEFAULT_FILTER_FIELD_NAME, dateProperty).build();
		} catch (Exception e) {
			log.error("创建TypeMapping失败, Error: {}", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public EsServiceImpl(ESConnector esConnector) {
		this.esConnector = esConnector;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	/**
	 * 检查集群状态
	 */
	@Override
	public boolean ping() {
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			boolean health = client.ping().value();
			if (!health) {
				throw new RuntimeException("elasticsearch cluster health status is red.");
			}
			return true;
		} catch (Exception e) {
			log.error("ping elasticsearch error: {}.", e.getMessage(), e);
			return false;
		} finally {
			esConnector.release(client);
		}
	}

	@Override
	public boolean isIndexExists(String index) {
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			ExistsRequest req = ExistsRequest.of(f -> f.index(index));
			return client.indices().exists(req).value();
		} catch (Exception e) {
			log.error("isIndexExists(indexName: {}), error: {} ", index, e.toString());
			return false;
		} finally {
			esConnector.release(client);
		}
	}

	@Override
	public void createIndex(String index, int shards, int replicas) {
		createIndex(index, DEFAULT_MAPPING, shards, replicas);
	}

	/**
	 * 创建索引
	 * @param index     索引名称
	 * @param shards    分片数目
	 * @param replicas  分片的副本数
	 */
	@Override
	public void createIndex(String index, TypeMapping mapping, int shards, int replicas) {
		if (lock == null) {
			createIndex0(index, mapping, shards, replicas);
		} else {
			String lockKey = "cache.lock.create.es.index." + index;
			String owner = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
			boolean locked = lock.lock(lockKey, owner, 10);
			if (!locked) {
				return;
			}

			createIndex0(index, mapping, shards, replicas);
			lock.unlock(lockKey, owner);
		}
	}
	private boolean createIndex0(String indexName, TypeMapping mapping, int shards, int replicas) {
		if (isIndexExists(indexName)) {
			return true;
		}
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			CreateIndexRequest request = CreateIndexRequest.of(f -> f.index(indexName)
					.settings(s -> s.index(is -> is.mapping(mls -> mls.totalFields(tf -> tf.limit(5000))))
							.numberOfShards("" + shards)
							.numberOfReplicas("" + replicas)
							.maxResultWindow(200000000))
					.mappings(mapping));
			CreateIndexResponse resp = client.indices().create(request);
			return resp.acknowledged();
		} catch (Exception e) {
			log.error("创建索引失败, Error: {}", e.getMessage(), e);
			return false;
		} finally {
			esConnector.release(client);
		}
	}

	/**
	 * 删除索引
	 */
	@Override
	public void deleteIndex(String index) {
		if (!isIndexExists(index)) {
			throw new RuntimeException("索引不存在，无法删除！");
		}
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			DeleteIndexRequest request = DeleteIndexRequest.of(f -> f.index(index));
			client.indices().delete(request);
		} catch (Exception e) {
			log.error("删除索引失败, Error: {}", e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			esConnector.release(client);
		}
	}

	/**
	 * 删除文档
	 */
	@Override
	public void delete(String index, String id) {
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			DeleteRequest request = DeleteRequest.of(f -> f.index(index).id(id));
			client.delete(request);
		} catch (Exception e) {
			log.error("delete by id({}) error: {}.", id, e.getMessage(), e);
		} finally {
			esConnector.release(client);
		}
	}

	@Override
	public void delete(String index, List<String> ids) {
		log.debug("[batchDelete] delete data begin!");
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			List<BulkOperation> list = new ArrayList<>();
			for (String id: ids) {
				list.add(new BulkOperation.Builder().delete(f -> f.index(index).id(id)).build());
			}
			client.bulk(builder -> builder.index(index).operations(list));
		} catch (Exception e) {
			log.error("删除失败, Error: {}", e.getMessage(), e);
		} finally {
			log.debug("[batchDelete] delete data end!");
			esConnector.release(client);
		}
	}

	@Override
	public void deleteByQuery(String index, Query query) {
		log.debug("[deleteByQuery] delete data begin!");
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			DeleteByQueryRequest request = DeleteByQueryRequest.of(f -> f.index(index).query(query));
			client.deleteByQuery(request);
		} catch (Exception e) {
			log.error("deleteByQuery失败, Error: {}", e.getMessage(), e);
		} finally {
			log.debug("[deleteByQuery] delete data end!");
			esConnector.release(client);
		}
	}

	@Override
	public Map<String, Object> findById(String index, String id) {
		log.debug("[findById] begin!");
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			GetRequest getRequest = GetRequest.of(f -> f.index(index).id(id));
			GetResponse<Map<String, Object>> resp = client.get(getRequest, (Type) Map.class);
			Map<String, Object> map = resp.source();
			if (map != null) {
				map.put("documentId", resp.id());
			}
			return map;
		} catch (Exception e) {
			log.error("findById失败, Error: {}", e.getMessage(), e);
			return null;
		} finally {
			log.debug("[findById] end!");
			esConnector.release(client);
		}
	}

	/**
	 * 存入数据
	 */
	@Override
	public boolean insertList(String index, List<Map<String, Object>> list) {
		if (list == null || list.size() <= 0) {
			return true;
		}
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			List<BulkOperation> opList = new ArrayList<>();
			for (Map<String, Object> map : list) {
				String id = (String) map.get("id");
				if (id == null || "".equals(id)) {
					id = UUID.randomUUID().toString().replace("-", "");
				}
				String finalId = id;
				opList.add(BulkOperation.of(d -> d.index(c -> c.id(finalId).document(map))));
			}
			BulkResponse bulkResp = client.bulk(builder -> builder.index(index).operations(opList));
			boolean ok = !bulkResp.errors();
			if (!ok) {
				log.error("Response of Insert: {}", bulkResp);
			}
			return ok;
		} catch (Exception e) {
			log.error("insertList Error: {}.", e.getMessage(), e);
		} finally {
			esConnector.release(client);
		}
		return false;
	}

	@Override
	public long queryTotal(List<String> indices, Query query) {
		long total = 0L;
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			CountRequest.Builder sourceBuilder = new CountRequest.Builder();
			sourceBuilder.index(indices);
//			sourceBuilder.trackTotalHits(true);
			if (query != null) {
				sourceBuilder.query(query);
			}
			CountRequest request = sourceBuilder.build();
			CountResponse count = client.count(request);
			total = count.count();
			log.info("数据总条数：{}", total);
		} catch (Exception e) {
			log.error("queryTotal失败, Error: {}", e.getMessage(), e);
		} finally {
			esConnector.release(client);
		}
		return total;
	}

	@Override
	public List<Map<String, Object>> getDataList(List<String> indices, Integer pageNo, Integer pageSize) {
		return getDataList(indices, null, pageNo, pageSize);
	}

	@Override
	public List<Map<String, Object>> getDataList(List<String> indices, Query query
			, Integer pageNo, Integer pageSize) {
		return getDataList(indices, DEFAULT_FILTER_FIELD_NAME, null, query, pageNo, pageSize);
	}

	@Override
	public List<Map<String, Object>> getDataList(List<String> indices, String filterFieldName, SortOrder sortOrder
			, Query query, Integer pageNo, Integer pageSize) {
		log.info("Start to get data from ES({})......", indices);
		long startTime = System.currentTimeMillis();
		List<Map<String, Object>> results = new ArrayList<>();
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			SearchRequest.Builder sourceBuilder = new SearchRequest.Builder().ignoreUnavailable(true);
			sourceBuilder.index(indices);
			if (query != null) {
				sourceBuilder.query(query);
			}
			if (filterFieldName != null) {
				sourceBuilder.sort(sortBuilder -> sortBuilder
						.field(fieldBuilder -> fieldBuilder.field(filterFieldName).order(sortOrder != null ? sortOrder : SortOrder.Asc)));
			}
//			sourceBuilder.trackTotalHits(true);
			if (pageNo != null && pageSize != null) {
				sourceBuilder.from((pageNo - 1) * pageSize);
				sourceBuilder.size(pageSize);
			}

			SearchRequest request = sourceBuilder.build();
			SearchResponse<Map<String, Object>> response = client.search(request, (Type) Map.class);
			final List<Hit<Map<String, Object>>> hits1 = response.hits().hits();
			for (Hit<Map<String, Object>> hit: hits1) {
				results.add(hit.source());
			}
		} catch (Exception e) {
			log.error("getDataList查询报错, Error: {}", e.getMessage(), e);
		} finally {
			esConnector.release(client);
			long timeCost = System.currentTimeMillis() - startTime;
			log.info("Getting data from ES cost {}."
					, timeCost >= 1000 ? (timeCost / 1000.0) + "s" : timeCost + "ms");
		}
		return results;
	}

	@Override
	public Map<String, Aggregate> getAggDataList(List<String> indices, Query query
			, String aggKey, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> aggFn) {
		return getAggDataList(indices,  query, aggKey, aggFn, DEFAULT_FILTER_FIELD_NAME, null);
	}
	@Override
	public Map<String, Aggregate> getAggDataList(List<String> indices, Query query
			, String aggKey, Function<Aggregation.Builder, ObjectBuilder<Aggregation>> aggFn
			, String filterFieldName, SortOrder sortOrder) {
		log.info("Start to get Aggregation data from ES({})......", indices);
		long startTime = System.currentTimeMillis();
		ElasticsearchClient client = null;
		try {
			client = esConnector.acquire();
			SearchRequest.Builder sourceBuilder = new SearchRequest.Builder().ignoreUnavailable(true);
			sourceBuilder.index(indices);
			if (query != null) {
				sourceBuilder.query(query);
			}
			sourceBuilder.aggregations(aggKey, aggFn);
			if (filterFieldName != null) {
				sourceBuilder.sort(sortBuilder -> sortBuilder
						.field(fieldBuilder -> fieldBuilder.field(filterFieldName).order(sortOrder != null ? sortOrder : SortOrder.Asc)));
			}

			SearchRequest request = sourceBuilder.build();
			SearchResponse<Map<String, Object>> response = client.search(request, (Type) Map.class);
			return response.aggregations();
		} catch (Exception e) {
			log.error("getAggDataList查询报错, Error: {}", e.getMessage(), e);
			return new HashMap<>();
		} finally {
			esConnector.release(client);
			long timeCost = System.currentTimeMillis() - startTime;
			log.info("Getting Aggregation data from ES cost {}."
					, timeCost >= 1000 ? (timeCost / 1000.0) + "s" : timeCost + "ms");
		}
	}
}
