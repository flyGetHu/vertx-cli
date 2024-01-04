package com.vertx.common.core.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 唯一地址
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface UniqueAddress {
    // 唯一地址
    String value() default "";
}
