package com.yuweix.kuafu.core;


import org.slf4j.MDC;


/**
 * @author yuwei
 */
public abstract class MdcUtil {
	public static String getTraceId() {
		return MDC.get(Constant.TRACE_ID_KEY);
	}

	public static void setTraceId(String traceId) {
		MDC.put(Constant.TRACE_ID_KEY, traceId);
	}

	public static void removeTraceId() {
		MDC.remove(Constant.TRACE_ID_KEY);
	}

	public static String getSpanId() {
		return MDC.get(Constant.SPAN_ID_KEY);
	}

	public static void setSpanId(String traceId) {
		MDC.put(Constant.SPAN_ID_KEY, traceId);
	}

	public static void removeSpanId() {
		MDC.remove(Constant.SPAN_ID_KEY);
	}

	public static String getRequestId() {
		return MDC.get(Constant.REQUEST_ID_KEY);
	}

	public static void setRequestId(String requestId) {
		MDC.put(Constant.REQUEST_ID_KEY, requestId);
	}

	public static void removeRequestId() {
		MDC.remove(Constant.REQUEST_ID_KEY);
	}
}
