package com.teapot.vip.spring.framework.aop.intercept;

public interface TPMethodInterceptor {

    Object invoke(TPMethodInvocation invocation) throws Throwable;
}
