package com.teapot.vip.spring.framework.beans.support;

import com.teapot.vip.spring.framework.beans.config.TPBeanDefinition;
import com.teapot.vip.spring.framework.context.support.TPAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TPDefaultListableBeanFactory extends TPAbstractApplicationContext {

    // 存储注册信息的BeanDefinition，伪IoC容器
    protected final Map<String, TPBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
}
