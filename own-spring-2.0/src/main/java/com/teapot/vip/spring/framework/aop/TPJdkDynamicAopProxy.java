package com.teapot.vip.spring.framework.aop;

import com.teapot.vip.spring.framework.aop.intercept.TPMethodInvocation;
import com.teapot.vip.spring.framework.aop.support.TPAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class TPJdkDynamicAopProxy implements TPAopProxy, InvocationHandler {

    private TPAdvisedSupport advised;

    public TPJdkDynamicAopProxy(TPAdvisedSupport config) {
        this.advised = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(this.advised.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader,
                this.advised.getTargetClass().getInterfaces(),
                this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 拦截器链
        List<Object> interceptorsAndDynamicMethodMatchers = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, this.advised.getTargetClass());

        TPMethodInvocation invocation = new TPMethodInvocation(proxy, this.advised.getTarget(),
                method, args, this.advised.getTargetClass(), interceptorsAndDynamicMethodMatchers);
        return invocation.proceed();
    }
}
