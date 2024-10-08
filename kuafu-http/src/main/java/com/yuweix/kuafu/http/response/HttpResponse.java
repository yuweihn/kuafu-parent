package com.yuweix.kuafu.http.response;


import org.apache.http.Header;
import jakarta.servlet.http.Cookie;
import java.util.List;


/**
 * @author yuwei
 */
public interface HttpResponse<B> {
	boolean isSuccess();
	int getStatus();
	String getErrorMessage();
	B getBody();
	List<Cookie> getCookieList();
	List<Header> getHeaderList();
	String getContentType();
}
