package com.teapot.vip.spring.framework.annotation;

import java.lang.annotation.*;

// 业务逻辑，注入接口
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TPService {
    String value() default "";
}
