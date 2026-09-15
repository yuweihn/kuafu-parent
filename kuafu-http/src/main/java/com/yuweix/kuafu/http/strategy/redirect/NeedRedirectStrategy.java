package com.yuweix.kuafu.http.strategy.redirect;


import org.apache.http.impl.client.DefaultRedirectStrategy;


/**
 * 重定向策略(重定向)
 * @author yuwei
 */
public class NeedRedirectStrategy extends DefaultRedirectStrategy {
	public NeedRedirectStrategy() {
		super();
	}
}
