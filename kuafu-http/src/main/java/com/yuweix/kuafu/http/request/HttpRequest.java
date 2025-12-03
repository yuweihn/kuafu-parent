package com.yuweix.kuafu.http.request;


import com.yuweix.kuafu.http.response.HttpResponse;


/**
 * @author yuwei
 */
public interface HttpRequest<B> {
	HttpResponse<B> execute();
}
