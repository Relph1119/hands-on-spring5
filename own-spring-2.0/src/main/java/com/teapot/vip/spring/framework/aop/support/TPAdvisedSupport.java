package com.teapot.vip.spring.framework.aop.support;

import com.teapot.vip.spring.framework.aop.aspect.TPAfterReturningAdviceInterceptor;
import com.teapot.vip.spring.framework.aop.aspect.TPAfterThrowingAdviceInterceptor;
import com.teapot.vip.spring.framework.aop.aspect.TPMethodBeforeAdviceInterceptor;
import com.teapot.vip.spring.framework.aop.config.TPAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TPAdvisedSupport {

    private Class<?> targetClass;
    private Object target;
    private TPAopConfig config;
    // 正则
    private Pattern pointCutClassPattern;

    private Map<Method, List<Object>> methodCache;

    // 解析Aop配置
    public TPAdvisedSupport(TPAopConfig config) {
        this.config = config;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    public Object getTarget() {
        return this.target;
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception {
        List<Object> cached = methodCache.get(method);
        if (null == cached) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cached = methodCache.get(m);
            this.methodCache.put(m, cached);
        }
        return cached;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    private void parse() {
        // pointCut=public .* com.teapot.vip.spring.demo.service..*Service..*(.*)
        // 解析配置文件
        // 将pointCut转成标准正则
        String pointCut = config.getPointCut().replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");
        // 编译成正则
        String pointCutForClassRegx = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegx.substring(
                pointCutForClassRegx.lastIndexOf(" ") + 1));

        try {
            methodCache = new HashMap<>();

            // 匹配方法
            Pattern pattern = Pattern.compile(pointCut);

            // 将方法初始化
            Class<?> aspectClass = Class.forName(this.config.getAspectClass());
            // 保存所有的方法
            Map<String, Method> aspectMethods = new HashMap<>();
            for (Method m : aspectClass.getMethods()) {
                aspectMethods.put(m.getName(), m);
            }

            for (Method m : this.targetClass.getMethods()) {
                String methodString = m.toString();
                if (methodString.contains("throws")) {
                    // 获取方法名
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }

                // 包装成拦截器
                Matcher matcher = pattern.matcher(methodString);
                if (matcher.matches()) {
                    // 执行器链
                    List<Object> advices = new LinkedList<>();
                    // 把每一个方法包装成MethodIntercept
                    // before
                    if (!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))) {
                        // 创建一个advised
                        advices.add(new TPMethodBeforeAdviceInterceptor(aspectMethods.get(config.getAspectBefore()), aspectClass.newInstance()));
                    }

                    // after
                    if (!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))) {
                        // 创建一个advised
                        advices.add(new TPAfterReturningAdviceInterceptor(aspectMethods.get(config.getAspectAfter()), aspectClass.newInstance()));
                    }

                    // afterThrowing
                    if (!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))) {
                        // 创建一个advised
                        TPAfterThrowingAdviceInterceptor throwingAdvice =
                                new TPAfterThrowingAdviceInterceptor(aspectMethods.get(config.getAspectAfterThrow()), aspectClass.newInstance());
                        throwingAdvice.setThrowName(config.getAspectAfterThrowingName());
                        advices.add(throwingAdvice);
                    }

                    methodCache.put(m, advices);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    // 用类名匹配切面表达式规则
    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }
}
