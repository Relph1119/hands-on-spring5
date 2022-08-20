package com.teapot.vip.spring.framework.beans.config;

import lombok.Data;

@Data
public class TPBeanDefinition {
    private String beanClassName;
    private boolean lazyinit = false;
    private String factoryBeanName;


}
