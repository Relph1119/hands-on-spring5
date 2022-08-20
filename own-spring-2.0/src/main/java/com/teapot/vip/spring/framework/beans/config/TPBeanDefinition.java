package com.teapot.vip.spring.framework.beans.config;

public class TPBeanDefinition {
    private String beanClassName;
    private boolean lazyinit = false;
    private String factoryBeanName;
    private boolean isSingleton = true;

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public boolean isLazyinit() {
        return lazyinit;
    }

    public void setLazyinit(boolean lazyinit) {
        this.lazyinit = lazyinit;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }
}
