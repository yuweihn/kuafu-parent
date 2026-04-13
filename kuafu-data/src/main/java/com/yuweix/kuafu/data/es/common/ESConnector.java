package com.yuweix.kuafu.data.es.common;


import co.elastic.clients.elasticsearch.ElasticsearchClient;


public interface ESConnector {
	ElasticsearchClient acquire();
	void release(ElasticsearchClient client);
}
