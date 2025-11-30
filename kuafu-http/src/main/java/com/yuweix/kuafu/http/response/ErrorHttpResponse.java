package com.yuweix.kuafu.http.response;


import com.yuweix.kuafu.core.JsonUtil;
import org.apache.http.Header;
import jakarta.servlet.http.Cookie;
import java.util.List;


/**
 * @author yuwei
 */
public class ErrorHttpResponse<B> implements HttpResponse<B> {
	private int status;
	private String errorMessage;

	public ErrorHttpResponse(int status, String errorMessage) {
		this.status = status;
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean isSuccess() {
		return false;
	}
	@Override
	public int getStatus() {
		return status;
	}
	@Override
	public B getBody() {
		return null;
	}
	@Override
	public List<Cookie> getCookieList() {
		return null;
	}
	@Override
	public List<Header> getHeaderList() {
		return null;
	}
	@Override
	public String getErrorMessage() {
		return errorMessage;
	}
	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public String toString() {
		return JsonUtil.toString(this);
	}
}
