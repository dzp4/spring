package com.hytech.spring.framework.webmvc.servlet;

import com.hytech.spring.framework.context.HYApplicationContext;
import com.hytech.spring.hyframework.annotation.HYController;
import com.hytech.spring.hyframework.annotation.HYRequestMapping;
import com.hytech.spring.hyframework.annotation.HYRequestParam;
import com.hytech.spring.hyframework.util.StrUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 委派模式
 * 职责：负责任务调度，请求分发
 *
 * @author dzp 2021/4/2
 */
public class HYDispatchServlet extends HttpServlet {
    private final Logger logger = Logger.getLogger(String.valueOf(getClass()));

    private List<HYHandlerMapping> handlerMappings = new ArrayList<>();

    private Map<HYHandlerMapping, HYHandlerAdapter> handlerAdapters = new HashMap<>();

    private List<HYViewResolver> viewResolvers = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            doDispatch(req, res);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            processDispatchResult(req, res, new HYModelAndView("500"));
        }
    }

    @Override
    public void init(ServletConfig config) {
        // 初始化spring核心IoC容器
        HYApplicationContext context = new HYApplicationContext(config.getInitParameter("contextConfigLocation"));
        // 初始化九大组件
        initStrategies(context);
    }

    private void initStrategies(HYApplicationContext context) {
        // 多文件上传的组件
//        initMultipartResolver(context);
        // 初始化本地语言环境
//        initLocaleResolver(context);
        // 初始化模板处理器
//        initThemeResolver(context);
        // 初始化 Url映射关系
        initHandlerMappings(context);
        // 初始化参数适配器
        initHandlerAdapters(context);
        // 初始化异常拦截器
//        initHandlerExceptionResolvers(context);
        // 初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        // 初始化视图转换器
        initViewResolvers(context);
        // 初始化参数缓存器
//        initFlashMapManager(context);
    }

    private void initHandlerMappings(HYApplicationContext context){
        if (context.getBeanDefinitionCount() == 0) {
            return;
        }

        for (String beanDefinitionName : context.getBeanDefinitionNames()) {
            Object instance = context.getBean(beanDefinitionName);
            Class<?> cla = instance.getClass();
            if (!cla.isAnnotationPresent(HYController.class)) {
                continue;
            }
            String url = "/";
            if (cla.isAnnotationPresent(HYRequestMapping.class)) {
                HYRequestMapping requestMapping = cla.getAnnotation(HYRequestMapping.class);
                url = requestMapping.value();
            }

            // 只获取public方法
            for (Method method : cla.getMethods()) {
                if (!method.isAnnotationPresent(HYRequestMapping.class)) {
                    continue;
                }
                HYRequestMapping requestMapping = method.getAnnotation(HYRequestMapping.class);

                Pattern pattern = Pattern.compile(url.replaceAll("/+", "/").replaceAll("\\*", ".*"));
                handlerMappings.add(new HYHandlerMapping(pattern, instance, method));
                logger.info("Method " + url + "," + method);
            }
        }

    }

    private void initHandlerAdapters(HYApplicationContext context) {
        for (HYHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new HYHandlerAdapter());
        }
    }

    private void initViewResolvers(HYApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("template_root");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            viewResolvers.add(new HYViewResolver(templateRoot));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse res) throws IOException, InvocationTargetException, IllegalAccessException {
        // 完成对HandlerMapping的封装
        // 完成了对方法返回值的封装ModelAndView

        // 1.通过url得到一个handlerMapping
        HYHandlerMapping handler = getHandler(req);
        if (handler == null) {
            processDispatchResult(req, res, new HYModelAndView("404"));
            return;
        }

        // 2.根据一个HandlerMapping获得一个HandlerAdapter
        HYHandlerAdapter adapter = getHandlerAdapter(handler);
        // 3.解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        HYModelAndView modelAndView = adapter.handler(req, res, handler);

        // 4.把ModelAndView变成一个ViewResolver
        processDispatchResult(req, res, modelAndView);

    }

    private HYHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();

        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        for (HYHandlerMapping mapping : handlerMappings) {
            Matcher matcher = mapping.getUrlPattern().matcher(url);
            if (matcher.matches()) {
                return mapping;
            }
        }

        return null;
    }

    private HYHandlerAdapter getHandlerAdapter(HYHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        return this.handlerAdapters.get(handler);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse res, HYModelAndView modelAndView) throws IOException {
        if (modelAndView == null || this.viewResolvers.isEmpty()) {
            return;
        }

        for (HYViewResolver viewResolver : this.viewResolvers) {
            HYView view = viewResolver.resolverViewName(modelAndView.getViewName());
            // 直接往浏览器输出了
            view.render(modelAndView.getModel(), req, res);
        }

    }




}
