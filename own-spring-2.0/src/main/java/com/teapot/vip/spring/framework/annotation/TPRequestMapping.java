package com.teapot.vip.spring.framework.annotation;

import java.lang.annotation.*;

// 请求URL
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TPRequestMapping {
    String value() default "";
}
