package com.yuweix.kuafu.session;


import com.yuweix.kuafu.core.serialize.Serializer;
import com.yuweix.kuafu.session.conf.SessionConf;

import java.util.Date;


/**
 * @author yuwei
 */
public abstract class SessionUtil {
	/**
	 * 根据指定sessionId获得登录时间
	 * eg.  sessionId:  cache.kuafu.session.9ee627c1a0d14d17a5c794ad2dd8421d
	 * @return
	 */
	public static Date getCreateTimeBySessionId(String sessionId) {
		if (sessionId == null) {
			return null;
		}
		Serializer serializer = SessionConf.getInstance().getSerializer();
		SessionAttribute attribute = serializer.deserialize(SessionConf.getInstance().getCache().get(sessionId));
		return attribute == null ? null : attribute.getCreateTime();
	}

	/**
	 * 查询指定session中指定属性的值
	 */
	@SuppressWarnings("unchecked")
	public static<T> T getAttributeBySessionId(String sessionId, String key) {
		if (sessionId == null) {
			return null;
		}
		Serializer serializer = SessionConf.getInstance().getSerializer();
		SessionAttribute attribute = serializer.deserialize(SessionConf.getInstance().getCache().get(sessionId));
		if (attribute == null) {
			return null;
		}
		return (T) attribute.getAttribute(key);
	}

	public static<T> String getSessionIdByRepeatKey(T repeatKey) {
		SessionConf conf = SessionConf.getInstance();
		String sessionIdKey = SessionConstant.SESSION_ID_PRE + conf.getApplicationName() + "." + SessionConstant.SESSION_ID_KEY_CURRENT + "." + repeatKey.toString();
		return conf.getCache().get(sessionIdKey);
	}
}
