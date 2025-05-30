package com.yuweix.kuafu.boot.exception;


import com.yuweix.kuafu.core.JsonUtil;
import com.yuweix.kuafu.core.Response;
import com.yuweix.kuafu.core.exception.ExceptionHandler;
import com.yuweix.kuafu.core.exception.ExceptionViewResolver;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "kuafu.boot.exception.enabled")
public class ExceptionAutoConfiguration {
	@Configuration
	@ConditionalOnProperty(name = "kuafu.boot.exception.handler.enabled", matchIfMissing = true)
	protected static class ErrorControllerConfiguration {
		@Value("${kuafu.exception.error-code:}")
		private String errorCode;

		@Controller
		public class ErrorController {
			@RequestMapping(value = { "/error", "/error/**" })
			@ResponseBody
			public String toErrorPage(HttpServletResponse response) {
				int status = response.getStatus();
				HttpStatus httpStatus = HttpStatus.valueOf(status);

				Response<String, Void> resp = new Response<>(errorCode == null || "".equals(errorCode) ? "" + status : errorCode
						, httpStatus.getReasonPhrase() + "[" + status + "]");
				return JsonUtil.toJSONString(resp);
			}
		}

		@ConditionalOnMissingBean(ExceptionViewResolver.class)
		@Bean
		public ExceptionViewResolver exceptionViewResolver() {
			return new ExceptionViewResolver() {
				@SuppressWarnings("unchecked")
				@Override
				public ModelAndView createView(String content) {
					AbstractView view = new AbstractView() {
						@Override
						protected void renderMergedOutputModel(Map<String, Object> map, HttpServletRequest req, HttpServletResponse resp) throws Exception {
							resp.setContentType("application/json; charset=" + StandardCharsets.UTF_8);
							ServletOutputStream out = resp.getOutputStream();
							out.write(JsonUtil.toJSONString(map).getBytes(StandardCharsets.UTF_8));
							out.flush();
						}
					};
					String text = JsonUtil.toJSONString(new Response<String, Void>(errorCode == null || "".equals(errorCode) ? "500" : errorCode, content));
					Map<String, Object> attributes = JsonUtil.parseObject(text, Map.class);
					view.setAttributesMap(attributes);
					return new ModelAndView(view);
				}
			};
		}
	}

	@ConditionalOnMissingBean(ClassMessagePair.class)
	@ConfigurationProperties(prefix = "kuafu.exception", ignoreUnknownFields = true)
	@Bean
	public ClassMessagePair classMessagePair() {
		return new ClassMessagePair() {
			private Map<String, String> map = new HashMap<>();

			@Override
			public Map<String, String> getDefaultMessage() {
				return map;
			}
		};
	}

	@ConditionalOnMissingBean(ExceptionHandler.class)
	@Bean
	public ExceptionHandler exceptionHandler(ClassMessagePair classMessagePair, ExceptionViewResolver viewResolver
			, @Value("${kuafu.exception.show-name:false}") boolean showExceptionName) {
		Map<Class<?>, String> errorMsgMap = new HashMap<>();

		Map<String, String> classMessageMap = classMessagePair.getDefaultMessage();
		if (classMessageMap != null) {
			Set<Map.Entry<String, String>> entrySet = classMessageMap.entrySet();
			for (Map.Entry<String, String> entry : entrySet) {
				try {
					errorMsgMap.put(Class.forName(entry.getKey()), entry.getValue());
				} catch (ClassNotFoundException ignored) {
				}
			}
		}

		ExceptionHandler handler = new ExceptionHandler();
		handler.setViewResolver(viewResolver);
		handler.setErrorMsgMap(errorMsgMap);
		handler.setShowExceptionName(showExceptionName);
		return handler;
	}
}
