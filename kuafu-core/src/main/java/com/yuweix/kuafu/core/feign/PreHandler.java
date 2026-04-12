package com.yuweix.kuafu.core.feign;


import feign.RequestTemplate;


/**
 * @author yuwei
 */
public interface PreHandler {
	void apply(RequestTemplate template);
}