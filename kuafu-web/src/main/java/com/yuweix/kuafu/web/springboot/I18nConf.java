package com.yuweix.kuafu.web.springboot;


import com.yuweix.kuafu.web.TextUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.lang.reflect.Constructor;


/**
 * @author yuwei
 */
public class I18nConf {
	@ConditionalOnMissingBean(TextUtil.class)
	@Bean
	public TextUtil textUtil(MessageSource messageSource) {
		try {
			Class<?> clz = Class.forName(TextUtil.class.getName());
			Constructor<?> constructor = clz.getDeclaredConstructor();
			constructor.setAccessible(true);
			TextUtil textUtil = (TextUtil) constructor.newInstance();
			textUtil.setMessageSource(messageSource);
			return textUtil;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@ConditionalOnMissingBean(LocaleResolver.class)
	@Bean(name = "localeResolver")
	public LocaleResolver localeResolver() {
		return new SessionLocaleResolver();
	}

	@ConditionalOnMissingBean(MessageSource.class)
	@Bean(name = "messageSource")
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("bundles.message");
		return messageSource;
	}
}
