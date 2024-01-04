package com.vertx.common.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 数据库表名注解
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface TableName {
    // 表名
    String name() default "";
}
