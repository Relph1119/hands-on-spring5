package com.teapot.vip.spring.framework.aop.aspect;

import com.teapot.vip.spring.framework.aop.intercept.TPMethodInterceptor;
import com.teapot.vip.spring.framework.aop.intercept.TPMethodInvocation;

import java.lang.reflect.Method;

public class TPAfterThrowingAdviceInterceptor extends TPAbstractAspectAdvice  implements TPMethodInterceptor {

    private String throwingName;

    public TPAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(TPMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        } catch (Throwable e) {
            invokeAdviceMethod(mi, null, e.getCause());
            throw e;
        }
    }

    public void setThrowName(String throwName) {
        this.throwingName = throwName;
    }
}
