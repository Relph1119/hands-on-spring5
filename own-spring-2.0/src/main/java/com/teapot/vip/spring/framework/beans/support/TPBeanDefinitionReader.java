package com.teapot.vip.spring.framework.beans.support;

import com.teapot.vip.spring.framework.beans.config.TPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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
        URL url = this.getClass().getClassLoader().getResource(
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

        for (String className : registryBeanClasses) {
            TPBeanDefinition beanDefinition = doCreateBeanDefinition(className);
            if (null == beanDefinition) {
                continue;
            }
            result.add(beanDefinition);
        }
        return result;
    }

    // 把每一个配置信息解析成BeanDefinition
    private TPBeanDefinition doCreateBeanDefinition(String className) {
        try {
            Class<?> beanClass = Class.forName(className);
            // 有可能是一个接口，用它的实现类作为beanClassName
            if (beanClass.isInterface()) {
                return null;
            }
            TPBeanDefinition beanDefinition = new TPBeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(toLowerfirstCase(beanClass.getSimpleName()));
            return beanDefinition;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
