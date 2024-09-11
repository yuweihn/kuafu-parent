package com.yuweix.kuafu.session.filter;


import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.yuweix.kuafu.session.CookiesUtil;
import com.yuweix.kuafu.session.SessionConstant;
import com.yuweix.kuafu.session.cache.SessionCache;
import com.yuweix.kuafu.session.conf.SessionConf;


/**
 * @author yuwei
 */
public class CookieSessionFilter extends SessionFilter {
	private String cookieName;


	public CookieSessionFilter(SessionCache cache) {
		super(cache);
	}
	public CookieSessionFilter() {

	}

	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}


	@Override
	protected String getSessionId(HttpServletRequest request, HttpServletResponse response) {
		String ckName = cookieName;
		if (ckName == null || "".equals(ckName)) {
			ckName = SessionConf.getInstance().getApplicationName() + SessionConstant.COOKIE_SESSION_ID_SUFFIX;
		}

		String sid = CookiesUtil.findValueByKey(request, ckName);
		if (sid == null || "".equals(sid)) {
			sid = UUID.randomUUID().toString().replace("-", "");
		}
		CookiesUtil.addCookie(request, response, ckName, sid, null, null
				, SessionConstant.COOKIE_MAX_AGE_DEFAULT);
		return sid;
	}
}
