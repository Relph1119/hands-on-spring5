package com.teapot.vip.spring.framework.beans.support;

import com.teapot.vip.spring.framework.beans.config.TPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public class TPBeanDefinitionReader {

    private List<String> registryBeanClasses = new ArrayList<>();

    private Properties config = new Properties();



    // 固定配置文件中的key，相当于xml规范
    private final String SCAN_PACKAGE = "scanPackage";

    public TPBeanDefinitionReader(String... locations){
        // 通过URL定位找到其所对应的文件，然后转换为文件流
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    private void doScanner(String scanPackage) {
        // 转换为文件路径，实际上就是把.替换为/
        // classpath
        // TODO: this.getClass().getClassLoader().
        URL url = this.getClass().getResource(
                "/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String clazzName = (scanPackage + "." + file.getName().replace(".class", ""));
                registryBeanClasses.add(clazzName);
            }
        }
    }

    public Properties getConfig() {
        return this.config;
    }

    // 把配置文件中扫描到的所有配置信息转换为TPBeanDefinition对象，
    // 以便于之后IoC操作方面
    public List<TPBeanDefinition> loadBeanDefinitions() {
        List<TPBeanDefinition> result = new ArrayList<>();

        try {
            for (String className : registryBeanClasses) {
                Class<?> beanClass = Class.forName(className);
                // 如果是一个接口，是不能实例化的
                // 用它实现类来实例化
                if (beanClass.isInterface()) {
                    continue;
                }

                // beanName有三种情况：
                // 1. 默认是类名首字母小写
                // 2. 自定义名字
                // 3. 接口注入
                result.add(doCreateBeanDefinition(toLowerfirstCase(beanClass.getSimpleName()), beanClass.getName()));
//                result.add(doCreateBeanDefinition(beanClass.getName(), beanClass.getName()));

                Class<?> [] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    // 如果是多个实现类，只能覆盖
                    // 为什么？因为Spring没有那么智能
                    // 这个时候，可以自定义名字
                    result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // 把每一个配置信息解析成BeanDefinition
    private TPBeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        TPBeanDefinition beanDefinition = new TPBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(factoryBeanName);
        return beanDefinition;
    }

    /**
     * 将类名首字母改为小写
     */
    private String toLowerfirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        // 大写字母的ASCII码要小于小写字母的ASCII
        // 在Java中，对char做算术运算实际上就是对ASCII码做算术运算
        if (Character.isLowerCase(chars[0])) {
            return simpleName;
        }
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
