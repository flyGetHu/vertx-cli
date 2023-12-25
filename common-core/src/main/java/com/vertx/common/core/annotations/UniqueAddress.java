package com.vertx.common.core.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface UniqueAddress {
    String value() default "";
}
