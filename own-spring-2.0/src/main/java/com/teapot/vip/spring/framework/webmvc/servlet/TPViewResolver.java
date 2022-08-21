package com.teapot.vip.spring.framework.webmvc.servlet;

import java.io.File;
import java.util.Locale;

public class TPViewResolver {

    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File templateRootDir;

    public TPViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        this.templateRootDir = new File(templateRootPath);
    }

    public TPView resolveViewName(String viewName, Locale locale) throws Exception {
        if (null == viewName || "".equals(viewName.trim())) {
            return null;
        }

        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));

        return new TPView(templateFile);
    }

}
