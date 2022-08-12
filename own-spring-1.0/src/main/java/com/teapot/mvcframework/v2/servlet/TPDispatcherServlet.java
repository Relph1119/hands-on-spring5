package com.teapot.mvcframework.v2.servlet;

import com.teapot.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class TPDispatcherServlet extends HttpServlet {
    // 保存application.properties文件中的内容
    private Properties contextConfig = new Properties();

    // 保存扫描的所有的类名
    private List<String> classNames = new ArrayList<String>();

    // 传说中的IoC容器
    // 为了简化程序，暂时不考虑ConcurrentHashMap
    // 主要还是关注设计思想和原理
    private Map<String, Object> ioc = new HashMap<String, Object>();

    // 保存url和Method的对应关系
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!!!");
            return;
        }

        Method method = this.handlerMapping.get(url);

        Map<String, String[]> params = req.getParameterMap();

        // 获取方法的形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 保存请求的url参数列表
        Map<String, String[]> parameterMap = req.getParameterMap();
        // 保存赋值参数的位置
        Object[] paramValues = new Object[parameterTypes.length];

        // 按照参数位置动态匹配参数
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                paramValues[i] = req;
                continue;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = resp;
                continue;
            } else if (parameterType == String.class) {
                // 提取方法中加入的注解参数
                Annotation[][] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for (Annotation a : pa[j]) {
                        if (a instanceof TPRequestParam) {
                            String paramName = ((TPRequestParam) a).value();
                            if (!"".equals(paramName.trim())) {
                                String value = Arrays.toString(parameterMap.get(paramName)).replaceAll("\\[|\\]]", "")
                                        .replaceAll("\\s", "");
                                paramValues[i] = value;
                            }
                        }
                    }
                }
            }
        }
        // 通过反射获取Method所在的Class，再获取Class的类名
        String beanName = toLowerfirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName),
                new Object[]{req, resp, params.get("name")[0]});
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        // (1)加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // (2)扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        // (3)初始化扫描到的类，并将它们放入IoC容器中
        doInstance();

        // (4)完成依赖注入
        doAutowired();

        // (5)初始化HandleMapping
        initHandlerMapping();

        System.out.println("TP MVC Framework is init");
    }

    /**
     * 初始化url和Method的映射关系
     */
    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(TPController.class)) {
                continue;
            }

            // 保存写在类上面的@GPRequestMapping("/demo")
            String baseUrl = "";
            if (clazz.isAnnotationPresent((TPRequestMapping.class))) {
                TPRequestMapping requestMapping = clazz.getAnnotation(TPRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            // 默认获取所有public类型的方法
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(TPRequestMapping.class)) {
                    continue;
                }
                TPRequestMapping requestMapping = method.getAnnotation(TPRequestMapping.class);
                // 优化
                String url = (baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");

                // 保存url和method的映射关系
                handlerMapping.put(url, method);
                System.out.println("Mapped " + url + "," + method);
            }
        }
    }

    /**
     * 自动进行依赖注入
     */
    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取所有的字段，包括private、protected、default类型
            // 普通OOP编程只能获得public类型的字段
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(TPAutowired.class)) {
                    continue;
                }

                TPAutowired autowired = field.getAnnotation(TPAutowired.class);
                // 如果用户没有自定义beanName，默认根据类型注入
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    // 获取接口的类型，作为key
                    beanName = field.getType().getName();
                }
                // 如果是public以外的类型，只要加了@Autowired注解都要强制赋值
                field.setAccessible(true);
                try {
                    // 根据key到IoC容器中取值
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 初始化扫描到的类，并将它们放入IoC容器中
     * 使用工厂模式实现
     */
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);

                // 需要思考
                // (1)什么样的类才需要初始化
                // (2)加了注解的类才初始化，那要如何判断呢？
                if (clazz.isAnnotationPresent(TPController.class)) {
                    Object instance = clazz.newInstance();
                    String beanName = toLowerfirstCase(className);
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(TPService.class)) {
                    // (1) 自定义的beanName
                    TPService service = clazz.getAnnotation(TPService.class);
                    String beanName = service.value();

                    // (2) 默认类名首字母小写
                    if ("".equals(beanName.trim())) {
                        beanName = toLowerfirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    // （3）根据类型自动赋值
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The " + i.getName() + " is exists!!!");
                        }
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将类名首字母改为小写
     * @param simpleName 类名
     * @return
     */
    private String toLowerfirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        // 大写字母的ASCII码要小于小写字母的ASCII
        // 在Java中，对char做算术运算实际上就是对ASCII码做算术运算
        if (Character.isLowerCase(chars[0])){
            return simpleName;
        }
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 加载配置文件
     * @param contextConfigLocation
     */
    private void doLoadConfig(String contextConfigLocation) {
        // 直接通过类路径找到Sping主配置文件所在的路径
        // 并且将其读取出来放到Properties对象中
        // 相当于将 scanPackage=com.teapot.demo 保存到了内存中
        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 扫描相关的类
     * @param scanPackage 包路径，com.teapot.demo
     */
    private void doScanner(String scanPackage) {
        // 转换为文件路径，实际上就是把.替换为/
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
                classNames.add(clazzName);
            }
        }
    }
}
