package com.teapot.vip.spring.framework.context;

import com.teapot.vip.spring.framework.annotation.TPAutowired;
import com.teapot.vip.spring.framework.annotation.TPController;
import com.teapot.vip.spring.framework.annotation.TPService;
import com.teapot.vip.spring.framework.beans.config.TPBeanPostProcessor;
import com.teapot.vip.spring.framework.core.TPBeanFactory;
import com.teapot.vip.spring.framework.beans.TPBeanWrapper;
import com.teapot.vip.spring.framework.beans.config.TPBeanDefinition;
import com.teapot.vip.spring.framework.beans.support.TPBeanDefinitionReader;
import com.teapot.vip.spring.framework.beans.support.TPDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按照源码分析的套路，IoC、DI、MVC、AOP
 */
public class TPApplicationContext extends TPDefaultListableBeanFactory implements TPBeanFactory {

    private String[] configLocations;
    private TPBeanDefinitionReader reader;

    // 单例的IoC容器缓存
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    // 通用的IoC容器
    private Map<String, TPBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

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
                try {
                    getBean(beanName);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private void doRegisterBeanDefinition(List<TPBeanDefinition> beanDefinitions) {
        for (TPBeanDefinition beanDefinition : beanDefinitions) {
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(beanClass.getName());
    }

    // 依赖注入，从这里开始，通过读取BeanDefinition中的信息
    // 然后，通过反射机制创建一个实例并返回
    // Spring做法是，不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
    // 装饰器模式：
    //1、保留原来的OOP关系
    //2、需要对它进行扩展，增强（为了以后AOP打基础）
    @Override
    public Object getBean(String beanName) throws Exception {
        TPBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        Object instance = null;

        // 工厂模式、策略模式
        TPBeanPostProcessor postProcessor = new TPBeanPostProcessor();

        postProcessor.postProcessBeforeInitialization(instance, beanName);

        // 1. 初始化
        instance = instantiateBean(beanName, beanDefinition);

        // 3. 把这个对象封装到BeanWrapper中
        TPBeanWrapper beanWrapper = new TPBeanWrapper(instance);

        // class A{B b;}
        // class B{A a;}
        // 一个方法是搞不定的，需要分两次，避免循环依赖

        // TODO: 2. 获取到beanWrapper之后，将其保存到IoC容器中
//        if (this.factoryBeanInstanceCache.containsKey(beanName)) {
//            throw new Exception("The " + beanName + " is exists!! ");
//        }
        // 4. 把beanWrapper存到IoC容器里面
        this.factoryBeanInstanceCache.put(beanName, beanWrapper);

        postProcessor.postProcessAfterInitialization(instance, beanName);

        // 3. 注入
        populateBean(beanName, new TPBeanDefinition(), beanWrapper);

        return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
    }

    private void populateBean(String beanName, TPBeanDefinition beanDefinition, TPBeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrappedInstance();

        Class<?> clazz = beanWrapper.getWrappedClass();
        // 判断：只有加了注解的类，才执行依赖注入
        if (!clazz.isAnnotationPresent(TPController.class) || clazz.isAnnotationPresent(TPService.class)) {
            return;
        }

        // 获取所有的fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(TPAutowired.class)) {
                continue;
            }

            TPAutowired autowired = field.getAnnotation(TPAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            // 强制访问
            field.setAccessible(true);

            try {
                // TODO：为什么会为NULL
                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null) {
                    continue;
                }
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Object instantiateBean(String beanName, TPBeanDefinition beanDefinition) {
        // 1. 获取要实例化的对象的类名
        String className = beanDefinition.getBeanClassName();

        // 2. 反射实例化，得到一个对象
        Object instance = null;
        try {
            // 假设默认就是单例， 细节暂且不考虑，先把主线拉通
            if (this.singletonObjects.containsKey(className)) {
                instance = this.singletonObjects.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                // 根据ClassName
                this.singletonObjects.put(className, instance);
                // 根据FactoryBeanName
                this.singletonObjects.put(beanDefinition.getFactoryBeanName(), instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }
}
