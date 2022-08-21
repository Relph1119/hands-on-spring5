package com.teapot.vip.spring.framework.aop.aspect;

import com.teapot.vip.spring.framework.aop.intercept.TPMethodInterceptor;
import com.teapot.vip.spring.framework.aop.intercept.TPMethodInvocation;

import java.lang.reflect.Method;

public class TPMethodBeforeAdviceInterceptor extends TPAbstractAspectAdvice implements TPMethodInterceptor {

    private TPJoinPoint joinPoint;

    public TPMethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private void before(Method method, Object[] args, Object target) throws Throwable {
        // 传递到织入的参数
        // method.invoke(target);
        super.invokeAdviceMethod(this.joinPoint, null ,null);
    }

    @Override
    public Object invoke(TPMethodInvocation mi) throws Throwable {
        // 从被织入的代码中获取，JointPoint
        this.joinPoint = mi;
        before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
