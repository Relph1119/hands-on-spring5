package com.teapot.vip.spring.framework.aop;

public interface TPAopProxy {
    public Object getProxy();

    public Object getProxy(ClassLoader classLoader);

}
