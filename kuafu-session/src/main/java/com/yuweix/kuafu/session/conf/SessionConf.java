package com.yuweix.kuafu.session.conf;


import com.yuweix.kuafu.core.serialize.Serializer;
import com.yuweix.kuafu.session.SessionConstant;
import com.yuweix.kuafu.session.cache.SessionCache;
import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author yuwei
 */
public class SessionConf {
	private PathPattern exclusivePattern;
	/**
	 * session失效时间(分钟)
	 */
	private int maxInactiveInterval;
	private String applicationName;
	private SessionCache cache;
	private Serializer serializer;

	private SessionConf() {

	}

	private static class Holder {
		private static final SessionConf instance = new SessionConf();
	}

	public static SessionConf getInstance() {
		return Holder.instance;
	}

	public void setExclusivePattern(String[] exclusiveURLs) {
		exclusivePattern = new PathPattern(exclusiveURLs);
	}

	public PathPattern getExclusivePattern() {
		return exclusivePattern;
	}

	public void setCache(SessionCache cache) {
		this.cache = cache;
	}

	public SessionCache getCache() {
		Assert.notNull(cache, "[cache] is required.");
		return cache;
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	public Serializer getSerializer() {
		Assert.notNull(serializer, "[serializer] is required.");
		return serializer;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public int getMaxInactiveInterval() {
		if (maxInactiveInterval <= 0) {
			return SessionConstant.DEFAULT_MAX_INACTIVE_INTERVAL;
		} else {
			return this.maxInactiveInterval;
		}
	}

	public void setApplicationName(String applicationName) {
		Assert.notNull(applicationName, "[applicationName] is required.");
		String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Matcher m = Pattern.compile(regEx).matcher(applicationName);
		this.applicationName = m.replaceAll("").trim();
	}

	public String getApplicationName() {
		Assert.notNull(applicationName, "[applicationName] is required.");
		return applicationName;
	}

	public void check() {
		Assert.notNull(cache, "[cache] is required.");
		Assert.notNull(serializer, "[serializer] is required.");
		Assert.notNull(applicationName, "[applicationName] is required.");
	}
}
