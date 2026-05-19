package com.yuweix.kuafu.core.exception;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;


/**
 * 通用异常处理方式
 * @author yuwei
 */
public class ExceptionHandler implements HandlerExceptionResolver {
	private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);


	private ExceptionViewResolver viewResolver;
	private Map<Class<?>, String> errorMsgMap;
	private boolean showExceptionName;
	/** 当配置中存在异常对应的提示消息时，是否优先使用配置消息而非异常原始消息 */
	private boolean preferConfiguredMessage = false;


	public void setViewResolver(ExceptionViewResolver viewResolver) {
		this.viewResolver = viewResolver;
	}

	public void setErrorMsgMap(Map<Class<?>, String> errorMsgMap) {
		this.errorMsgMap = errorMsgMap;
	}

	public void setShowExceptionName(boolean showExceptionName) {
		this.showExceptionName = showExceptionName;
	}

	public void setPreferConfiguredMessage(boolean preferConfiguredMessage) {
		this.preferConfiguredMessage = preferConfiguredMessage;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response
			, Object handler, Exception ex) {
		log.error("ExceptionHandler===>>>resolveException, Error: {}", ex.getMessage(), ex);
		return viewResolver.createView(showMessage(ex));
	}

	private String showMessage(Exception ex) {
		Class<? extends Exception> aClz = ex.getClass();
		String msg = ex.getMessage();
		String configuredMsg = errorMsgMap == null ? null : errorMsgMap.get(aClz);

		if (preferConfiguredMessage && configuredMsg != null && !"".equals(configuredMsg)) {
			return showExceptionName ? aClz.getName() + ": " + configuredMsg : configuredMsg;
		}
		if (msg != null && !"".equals(msg)) {
			return showExceptionName ? aClz.getName() + ": " + msg : msg;
		}
		if (configuredMsg != null && !"".equals(configuredMsg)) {
			return showExceptionName ? aClz.getName() + ": " + configuredMsg : configuredMsg;
		}
		return aClz.getName();
	}
}
