package com.yuweix.kuafu.core.exception;


import com.yuweix.kuafu.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@Value("${kuafu.exception.error-code:500}")
	protected String errorCode;


	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.OK)
	public Response<String, Void> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
		log.error("RuntimeException | URI: {}", request.getRequestURI(), ex);
		return Response.of(errorCode, ex.getMessage());
	}

	@ExceptionHandler({
			MethodArgumentNotValidException.class,
			BindException.class,
			MissingServletRequestParameterException.class,
			MethodArgumentTypeMismatchException.class,
			HttpMessageNotReadableException.class
	})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Response<String, Void> handleValidationException(Exception ex, HttpServletRequest request) {
		String msg;
		if (ex instanceof MethodArgumentNotValidException) {
			msg = ((MethodArgumentNotValidException) ex).getBindingResult().getFieldErrors().stream()
					.map(FieldError::getDefaultMessage)
					.collect(Collectors.joining("; "));
		} else if (ex instanceof BindException) {
			msg = ((BindException) ex).getFieldErrors().stream()
					.map(FieldError::getDefaultMessage)
					.collect(Collectors.joining("; "));
		} else if (ex instanceof MissingServletRequestParameterException) {
			msg = "缺少必要参数: " + ((MissingServletRequestParameterException) ex).getParameterName();
		} else if (ex instanceof MethodArgumentTypeMismatchException) {
			msg = "参数类型错误: " + ((MethodArgumentTypeMismatchException) ex).getName();
		} else {
			msg = "请求体格式错误";
		}
		log.error("ValidationException: {} | URI: {}", msg, request.getRequestURI());
		return Response.of(errorCode, msg);
	}

	@ExceptionHandler({
			HttpRequestMethodNotSupportedException.class,
			HttpMediaTypeNotSupportedException.class,
			NoHandlerFoundException.class
	})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Response<String, Void> handleHttpException(Exception ex, HttpServletRequest request) {
		String msg;
		if (ex instanceof HttpRequestMethodNotSupportedException) {
			msg = "不支持的请求方法: " + ((HttpRequestMethodNotSupportedException) ex).getMethod();
		} else if (ex instanceof HttpMediaTypeNotSupportedException) {
			msg = "不支持的媒体类型: " + ((HttpMediaTypeNotSupportedException) ex).getContentType();
		} else {
			msg = "资源不存在";
		}
		log.error("HttpException: {} | URI: {}", msg, request.getRequestURI());
		return Response.of(errorCode, msg);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Response<String, Void> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
		log.error("IllegalArgumentException: {} | URI: {}", ex.getMessage(), request.getRequestURI());
		return Response.of(errorCode, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Response<String, Void> handleException(Exception ex, HttpServletRequest request) {
		log.error("Unhandled Exception | URI: {}", request.getRequestURI(), ex);
		return Response.of(errorCode, "系统内部错误");
	}
}
