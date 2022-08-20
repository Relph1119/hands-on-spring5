package com.teapot.vip.spring.framework.context;

/**
 * 通过解耦方式获得IoC容器的顶层设计
 * 后面将通过一个监听器，去扫描所有的类，
 * 只要实现此接口，将自动调用setApplicationContext()方法，
 * 从而将IoC容器注入目标类中
 */
public interface TPApplicationContextAware {
    void setApplicationContext(TPApplicationContext applicationContext);
}
