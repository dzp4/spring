package com.hytech.spring.framework.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @author dzp 2021/4/5
 */
public class HYHandlerMapping {

    private Pattern urlPattern;

    private Method method;

    private Object controller;

    public HYHandlerMapping(Pattern urlPattern, Object controller, Method method) {
        this.urlPattern = urlPattern;
        this.controller = controller;
        this.method = method;
    }

    public Pattern getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(Pattern urlPattern) {
        this.urlPattern = urlPattern;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

}
