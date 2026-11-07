package com.yuweix.kuafu.core.springboot;


import com.yuweix.kuafu.core.feign.DynamicClient;
import feign.codec.Decoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * @author yuwei
 */
public class DynamicFeignConf {
	@ConditionalOnMissingBean(Decoder.class)
	@Bean
	public Decoder feignDecoder() {
		List<HttpMessageConverter<?>> converters = new ArrayList<>();

		converters.add(new StringHttpMessageConverter(Charset.forName("GBK")));
		converters.add(new MappingJackson2HttpMessageConverter());

		return new SpringDecoder(() -> new HttpMessageConverters(converters));
	}

	@ConditionalOnMissingBean(FeignBuilderCustomizer.class)
	@Bean
	public FeignBuilderCustomizer feignBuilderCustomizer(Decoder feignDecoder) {
		return builder -> builder.decoder(feignDecoder);
	}

	@ConditionalOnMissingBean(DynamicClient.class)
	@Bean
	public DynamicClient dynamicClient(ApplicationContext context, FeignBuilderCustomizer customizer) {
		return new DynamicClient(context, customizer);
	}
}
