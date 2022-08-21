package com.teapot.vip.spring.framework.webmvc.servlet;

import com.teapot.vip.spring.framework.annotation.TPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TPHandlerAdapter {
    public boolean supports(Object handler) {
        return (handler instanceof TPHandlerMapping);
    }

    public TPModelAndView handler(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        TPHandlerMapping handlerMapping = (TPHandlerMapping) handler;

        // 将方法形参列表和Request的参数列表所在的顺序进行一一对应
        Map<String, Integer> paramIndexMapping = new HashMap<>();

        // 提取方法中加了注解的参数
        // 把方法上的注解拿到，得到的是一个二维数组
        // 因为一个参数可以有多个注解，而一个方法又有多个参数
        Annotation[] [] pa = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length ; i ++) {
            for(Annotation a : pa[i]){
                if(a instanceof TPRequestParam){
                    String paramName = ((TPRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        // 填充：提取方法中的request和response参数
        Class<?>[] paramsTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < paramsTypes.length; i++) {
            Class<?> type = paramsTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                paramIndexMapping.put(type.getName(), i);
            }
        }

        // 赋值：
        // 获取方法的形参列表
        Map<String, String[]> params = request.getParameterMap();
        // 保存实参列表
        Object[] paramValues = new Object[paramsTypes.length];

        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", "");

            if (!paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }

            int index = paramIndexMapping.get(param.getKey());
            paramValues[index] = caseStringValue(paramsTypes[index], value);
        }

        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = request;
        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = response;
        }

        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);
        if (result == null || result instanceof Void) {
            return null;
        }

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == TPModelAndView.class;
        if (isModelAndView) {
            return (TPModelAndView) result;
        }

        return null;
    }

    private Object caseStringValue(Class<?> type, String value) {
        if (String.class == type) {
            return value;
        }

        // 如果是int
        if (Integer.class == type) {
            return Integer.valueOf(value);
        } else if (Double.class == type) {
            return Double.valueOf(value);
        } else {
            if (value != null) {
                return value;
            }
            return null;
        }

        // 如果还有double或者其他的类型，继续添加if判断
        // 可以使用策略模式
    }
}
