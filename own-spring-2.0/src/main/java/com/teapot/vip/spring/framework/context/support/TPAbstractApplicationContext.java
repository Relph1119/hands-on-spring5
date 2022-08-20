package com.teapot.vip.spring.framework.context.support;

/**
 * IOC容器实现的顶层设计
 */
public abstract class TPAbstractApplicationContext {

    // 受保护，只提供给子类重写
    // 模板模式，最少知道原则
    protected void refresh() {

    }
}
