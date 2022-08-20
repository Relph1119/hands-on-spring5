package com.teapot;

import com.teapot.vip.spring.demo.service.impl.QueryService;
import com.teapot.vip.spring.framework.context.TPApplicationContext;

public class Test {
    public static void main(String[] args) {
        TPApplicationContext context = new TPApplicationContext("classpath:application.properties");
        try {
            Object object = context.getBean(QueryService.class);
            System.out.println(object);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
