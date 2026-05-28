package com.yuweix.kuafu.dao.hibernate.generator;


import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 强制要求实体在持久化（save）之前必须手动设置好主键 ID，否则抛出异常。
 * Hibernate6.0以后有效
 */
@Target(FIELD)
@Retention(RUNTIME)
@IdGeneratorType(AssignGenerator.class)
public @interface Assigned {

}
