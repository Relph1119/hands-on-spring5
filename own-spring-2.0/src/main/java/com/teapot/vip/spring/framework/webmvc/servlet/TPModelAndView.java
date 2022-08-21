package com.teapot.vip.spring.framework.webmvc.servlet;

import java.util.HashMap;
import java.util.Map;

public class TPModelAndView {

    private String viewName;
    private Map<String,?> model;

    public TPModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public TPModelAndView(String viewName, Map<String, Object> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }


    public Map<String, ?> getModel() {
        return model;
    }
}
