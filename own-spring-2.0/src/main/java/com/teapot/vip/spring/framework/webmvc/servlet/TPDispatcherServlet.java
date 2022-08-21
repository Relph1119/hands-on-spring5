package com.teapot.vip.spring.framework.webmvc.servlet;

import com.teapot.vip.spring.framework.annotation.TPController;
import com.teapot.vip.spring.framework.annotation.TPRequestMapping;
import com.teapot.vip.spring.framework.context.TPApplicationContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Servlet只是作为一个MVC的启动入口
@Slf4j
public class TPDispatcherServlet extends HttpServlet {

    private final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    private TPApplicationContext context;

    private List<TPHandlerMapping> handlerMappings = new ArrayList<>();

    private Map<TPHandlerMapping, TPHandlerAdapter> handlerAdapters = new HashMap<>();

    private List<TPViewResolver> viewResolvers = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.doDispatch(req, resp);
        } catch (Exception e) {
//            try {
//                prcessDispatchResult(req, resp, new TPModelAndView("500"));
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 1. 通过从request获取URL，匹配一个handlerMapping
        TPHandlerMapping handler = getHandler(req);

        if (handler == null) {
            prcessDispatchResult(req, resp, new TPModelAndView("404"));
            return;
        }

        // 2. 准备调用前的参数
        TPHandlerAdapter ha = getHandlerAdapter(handler);

        // 3. 真正的调用方法，返回ModelAndView存储的页面和页面模板的名称
        TPModelAndView mav = ha.handler(req, resp, handler);

        // 这一步才是真正的输出
        prcessDispatchResult(req, resp, mav);
    }

    private void prcessDispatchResult(HttpServletRequest req, HttpServletResponse resp, TPModelAndView mav) throws Exception {
        // 将ModelAndView变成一个HTML/OutputStream/json/freemark/veolcity
        // ContextType

        if (null == mav) {
            return;
        }

        // 如果ModelAndView不为null，需要进行渲染
        if (this.viewResolvers.isEmpty()) {
            return;
        }

        for (TPViewResolver viewResolver : this.viewResolvers) {
            TPView view  = viewResolver.resolveViewName(mav.getViewName(), null);
            view.render(mav.getModel(), req, resp);
            return;
        }
    }

    private TPHandlerAdapter getHandlerAdapter(TPHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        TPHandlerAdapter ha = this.handlerAdapters.get(handler);
        if (ha.supports(handler)) {
            return ha;
        }
        return null;
    }

    private TPHandlerMapping getHandler(HttpServletRequest req) throws Exception {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        for (TPHandlerMapping handler : this.handlerMappings) {
            try {
                Matcher matcher = handler.getPattern().matcher(url);
                if (!matcher.matches()) {
                    continue;
                }
                return handler;
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1. 初始化ApplicationContext
        context = new TPApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));

        // 2. 初始化Spring MVC九大组件
        initStrategies(context);
    }

    // 初始化策略
    protected void initStrategies(TPApplicationContext context) {
        // 多文件上传的组件
        initMultipartResolver(context);
        // 初始化本地语言环境
        initLocaleResolver(context);
        // 初始化模板处理器
        initThemeResolver(context);
        // handlerMapping，必须实现
        initHandlerMappings(context);
        // 初始化参数适配器，必须实现
        initHandlerAdapters(context);
        // 初始化异常拦截器
        initHandlerExceptionResolvers(context);
        // 初始化视图预处理器
        initRequestToViewNameTranslator(context);
        // 初始化视图转换器，必须实现
        initViewResolvers(context);
        // 参数缓存器
        initFlashMapManager(context);
    }

    private void initFlashMapManager(TPApplicationContext context) {

    }

    private void initViewResolvers(TPApplicationContext context) {
        // 获取模板的存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);

        for (File template : templateRootDir.listFiles()) {
            this.viewResolvers.add(new TPViewResolver(templateRoot));
        }

    }

    private void initRequestToViewNameTranslator(TPApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(TPApplicationContext context) {
    }

    private void initHandlerAdapters(TPApplicationContext context) {
        // 把一个request请求变成一个handler，参数都是字符串，自动匹配到handler中的形参

        // 需要拿到HandlerMapping，才能完成转换
        // 意味着，有几个HandlerMapping，就有几个HandlerAdapter
        for (TPHandlerMapping handlerMapping : this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new TPHandlerAdapter());
        }
    }

    private void initHandlerMappings(TPApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        try {
            for (String beanName : beanNames) {
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();

                if (!clazz.isAnnotationPresent(TPController.class)) {
                    continue;
                }

                String baseUrl = "";
                if (clazz.isAnnotationPresent((TPRequestMapping.class))) {
                    TPRequestMapping requestMapping = clazz.getAnnotation(TPRequestMapping.class);
                    baseUrl = requestMapping.value();
                }

                // 默认获取所有public类型的方法
                for (Method method : clazz.getMethods()) {
                    // 没有加RequestMapping注解的直接忽略
                    if (!method.isAnnotationPresent(TPRequestMapping.class)) {
                        continue;
                    }
                    TPRequestMapping requestMapping = method.getAnnotation(TPRequestMapping.class);
                    String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*", ".*"))
                            .replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);

                    // 保存url和method的映射关系
                    handlerMappings.add(new TPHandlerMapping(controller, method, pattern));
                    log.info("Mapped " + regex + "," + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initThemeResolver(TPApplicationContext context) {
    }

    private void initLocaleResolver(TPApplicationContext context) {
    }

    private void initMultipartResolver(TPApplicationContext context) {

    }


}

