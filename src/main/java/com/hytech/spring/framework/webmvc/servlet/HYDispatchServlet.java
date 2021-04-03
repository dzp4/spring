package com.hytech.spring.framework.webmvc.servlet;

import com.hytech.spring.framework.context.HYApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 委派模式
 * 职责：负责任务调度，请求分发
 *
 * @author dzp 2021/4/2
 */
public class HYDispatchServlet extends HttpServlet {

    private HYApplicationContext applicationContext;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        // 初始化spring核心IoC容器
        this.applicationContext = new HYApplicationContext(config.getInitParameter("contextConfigLocation"));


    }

}
