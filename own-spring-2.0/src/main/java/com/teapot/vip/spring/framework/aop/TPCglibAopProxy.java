package com.teapot.vip.spring.framework.aop;

import com.teapot.vip.spring.framework.aop.support.TPAdvisedSupport;

public class TPCglibAopProxy implements TPAopProxy{

    public TPCglibAopProxy(TPAdvisedSupport config) {

    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
