package com.teapot.vip.spring.framework.context;

import com.teapot.vip.spring.framework.beans.TPBeanFactory;
import com.teapot.vip.spring.framework.beans.TPBeanWrapper;
import com.teapot.vip.spring.framework.beans.config.TPBeanDefinition;
import com.teapot.vip.spring.framework.beans.support.TPBeanDefinitionReader;
import com.teapot.vip.spring.framework.beans.support.TPDefaultListableBeanFactory;

import java.util.List;
import java.util.Map;

/**
 * 按照源码分析的套路，IoC、DI、MVC、AOP
 *
 */
public class TPApplicationContext extends TPDefaultListableBeanFactory implements TPBeanFactory {

    private String [] configLocations;
    private TPBeanDefinitionReader reader;

    // ClassPathXmlA
    public TPApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        refresh();
    }

    @Override
    protected void refresh() {
        // 1. 定位：定位配置文件
        reader = new TPBeanDefinitionReader(this.configLocations);

        // 2. 加载配置文件，扫描相关的类，把它们封装成BeanDefinition
        List<TPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3. 注册：把配置信息放到容器里面（伪IoC容器）
        doRegisterBeanDefinition(beanDefinitions);

        // 4. 把不是延时加载的类进行提前初始化
        doAutowired();
    }

    // 只处理非延时加载的情况
    private void doAutowired() {
        for (Map.Entry<String, TPBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyinit()) {
                getBean(beanName);
            }
        }
    }

    private void doRegisterBeanDefinition(List<TPBeanDefinition> beanDefinitions) {
        for (TPBeanDefinition beanDefinition : beanDefinitions) {
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    @Override
    public Object getBean(String beanName) {
        // 1. 初始化
        instantiateBean(beanName, new TPBeanDefinition());
        // class A{B b;}
        // class B{A a;}
        // 一个方法是搞不定的，需要分两次，避免循环依赖


        // 2. 注入
        populateBean(beanName, new TPBeanDefinition(), new TPBeanWrapper());

        return null;
    }

    private void populateBean(String beanName, TPBeanDefinition tpBeanDefinition, TPBeanWrapper tpBeanWrapper) {

    }

    private void instantiateBean(String beanName, TPBeanDefinition tpBeanDefinition) {

    }
}
