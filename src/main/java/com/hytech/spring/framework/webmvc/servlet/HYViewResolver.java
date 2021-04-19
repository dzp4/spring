package com.hytech.spring.framework.webmvc.servlet;

import com.hytech.spring.hyframework.util.StrUtil;

import java.io.File;

/**
 * @author dzp 2021/4/5
 */
public class HYViewResolver {

    private static final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File templateRootDir;

    public HYViewResolver(String templateRoot) {
        String path = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        templateRootDir = new File(path);
    }

    public HYView resolverViewName(String viewName) {
        if (StrUtil.isEmpty(viewName) || "".equals(viewName.trim())) {
            return null;
        }
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
        File file = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+", ""));
        return new HYView(file);
    }

}
