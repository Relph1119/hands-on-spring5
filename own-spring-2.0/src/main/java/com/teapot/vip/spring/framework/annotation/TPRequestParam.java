package com.teapot.vip.spring.framework.annotation;

import java.lang.annotation.*;

// 请求参数映射
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TPRequestParam {
    String value() default "";

    boolean required() default true;
}
