package com.yuweix.kuafu.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;


/**
 * @author yuwei
 */
public class SpringContext implements ApplicationContextAware {
	private static final Logger log = LoggerFactory.getLogger(SpringContext.class);
	
	private static ConfigurableApplicationContext applicationContext;
	
	
	private SpringContext() {

	}


	public static void register(String beanName, Object obj) {
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		beanFactory.registerSingleton(beanName, obj);
	}
	public static void register(String beanName, Object obj, boolean check) {
		ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
		if (check && beanFactory.containsBean(beanName)) {
			return;
		}
		beanFactory.registerSingleton(beanName, obj);
	}
	public static<T> void registerBean(String beanName, Class<T> clz) {
		registerBean(beanName, clz, null);
	}
	public static<T> void registerBean(String beanName, Class<T> clz, List<Property> constructorArgList) {
		registerBean(beanName, clz, constructorArgList, null);
	}
	public static<T> void registerBean(String beanName, Class<T> clz, List<Property> constructorArgList, List<Property> propertyList) {
		registerBean(beanName, clz, constructorArgList, propertyList, null, null);
	}
	public static<T> void registerBean(String beanName, Class<T> clz, List<Property> constructorArgList, List<Property> propertyList
			, String initMethod, String destroyMethod) {
		BeanDefinitionRegistry beanDefRegistry = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
		
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clz);
		if (constructorArgList != null && constructorArgList.size() > 0) {
			for (Property constructorArg: constructorArgList) {
				if (Property.TYPE_VALUE == constructorArg.getType()) {
					builder.addConstructorArgValue(constructorArg.getValue());
				} else if (Property.TYPE_REFERENCE == constructorArg.getType()) {
					builder.addConstructorArgReference(constructorArg.getValue().toString());
				} else {
					throw new RuntimeException("Error parameter [type] in constructorArgList!");
				}
			}
		}
		
		if (propertyList != null && propertyList.size() > 0) {
			for (Property prop: propertyList) {
				if (Property.TYPE_VALUE == prop.getType()) {
					builder.addPropertyValue(prop.getPropertyName(), prop.getValue());
				} else if (Property.TYPE_REFERENCE == prop.getType()) {
					builder.addPropertyReference(prop.getPropertyName(), prop.getValue().toString());
				} else {
					throw new RuntimeException("Error parameter [type] in propertyList!");
				}
			}
		}

		if (initMethod != null && !"".equals(initMethod)) {
			builder.setInitMethodName(initMethod);
		}
		if (destroyMethod != null && !"".equals(destroyMethod)) {
			builder.setDestroyMethodName(destroyMethod);
		}
		beanDefRegistry.registerBeanDefinition(beanName, builder.getBeanDefinition());
	}

	@SuppressWarnings("unchecked")
	public static<T> T getBean(String beanName) {
		return (T) applicationContext.getBean(beanName);
	}
	@SuppressWarnings("unchecked")
	public static<T> T getBeanIgnoreNull(String beanName) {
		try {
			return (T) applicationContext.getBean(beanName);
		} catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}
	
	public static<T> T getBean(Class<T> clz) {
		return applicationContext.getBean(clz);
	}
	public static<T> T getBeanIgnoreNull(Class<T> clz) {
		try {
			return applicationContext.getBean(clz);
		} catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}
	
	public static<T> T getBean(String name, Class<T> clz) {
		return applicationContext.getBean(name, clz);
	}
	public static<T> T getBeanIgnoreNull(String name, Class<T> clz) {
		try {
			return applicationContext.getBean(name, clz);
		} catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}
	
	
	public static class Property {
		private String propertyName;
		private Object value;
		private byte type;
		
		public static final byte TYPE_VALUE = 0;
		public static final byte TYPE_REFERENCE = 1;
		
		public Property() {

		}
		public Property(String propertyName, Object value, byte type) {
			this.propertyName = propertyName;
			this.value = value;
			this.type = type;
		}
		public String getPropertyName() {
			return propertyName;
		}
		public void setPropertyName(String propertyName) {
			this.propertyName = propertyName;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		public byte getType() {
			return type;
		}
		public void setType(byte type) {
			this.type = type;
		}
	}
	
	@Override
	public void setApplicationContext(ApplicationContext ctx) {
		applicationContext = (ConfigurableApplicationContext) ctx;
	}
}
