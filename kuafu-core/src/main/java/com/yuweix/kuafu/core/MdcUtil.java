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
}
