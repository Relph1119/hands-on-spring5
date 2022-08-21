package com.teapot.vip.spring.framework.aop.aspect;

import com.teapot.vip.spring.framework.aop.intercept.TPMethodInterceptor;
import com.teapot.vip.spring.framework.aop.intercept.TPMethodInvocation;

import java.lang.reflect.Method;

public class TPAfterReturningAdviceInterceptor extends TPAbstractAspectAdvice implements TPMethodInterceptor {

    private TPJoinPoint joinPoint;

    public TPAfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(TPMethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }

    private void afterReturning(Object retVal, Method method, Object[] arguments, Object aThis) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint, retVal, null);
    }
}
