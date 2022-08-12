package com.teapot.demo.mvc.action;

import com.teapot.demo.service.IDemoService;
import com.teapot.mvcframework.annotation.TPAutowired;
import com.teapot.mvcframework.annotation.TPController;
import com.teapot.mvcframework.annotation.TPRequestMapping;
import com.teapot.mvcframework.annotation.TPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@TPController
@TPRequestMapping("/demo")
public class DemoAction {
    @TPAutowired private IDemoService demoService;
    @TPRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @TPRequestParam("name") String name){
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    @TPRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @TPRequestParam("a") Integer a, @TPRequestParam("b") Integer b) {
        try {
            resp.getWriter().write(a + " + " + b + " = " + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @TPRequestMapping("/remove")
    public void remove(HttpServletRequest req, HttpServletResponse resp,
                       @TPRequestParam("id") Integer id) {

    }
}
