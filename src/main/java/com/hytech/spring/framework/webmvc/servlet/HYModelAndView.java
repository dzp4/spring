package com.hytech.spring.framework.webmvc.servlet;

import java.util.Map;

/**
 * @author dzp 2021/4/5
 */
public class HYModelAndView {

    private String viewName;

    private Map<String, ?> model;

    public HYModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public HYModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

}
