package com.teapot.demo.service.impl;

import com.teapot.demo.service.IDemoService;
import com.teapot.mvcframework.annotation.TPService;

@TPService
public class DemoService implements IDemoService {
    public String get(String name) {
        return "My name is " + name;
    }
}
