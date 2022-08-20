package com.teapot.vip.spring.framework.beans;

/**
 * 单例工厂的顶层设计
 */
public interface TPBeanFactory {

    /**
     * 根据beanName从IoC容器中获得一个实例Bean
     * @param beanName
     * @return
     */
    Object getBean(String beanName);
}
