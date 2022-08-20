package com.teapot.vip.spring.framework.webmvc.servlet;

import java.util.HashMap;
import java.util.Map;

public class TPModelAndView {

    private String code;
    Map<String,Object> model = new HashMap<String,Object>();

    public TPModelAndView(String code, Map<String, Object> model) {
        this.code = code;
        this.model = model;
    }
}
