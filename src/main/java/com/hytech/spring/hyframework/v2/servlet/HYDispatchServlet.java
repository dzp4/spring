package com.hytech.spring.hyframework.v2.servlet;

import com.hytech.spring.hyframework.annotation.*;
import com.hytech.spring.hyframework.util.StrUtil;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author dzp 2021/3/31
 */
public class HYDispatchServlet extends HttpServlet {

    private final Logger logger = Logger.getLogger(String.valueOf(getClass()));

    private static final Properties properties = new Properties();

    private static final String propertiesParmaName = "contextConfigLocation";

    // 享元模式 缓存
    private final List<String> classNames = new ArrayList<>();

    // ioc容器，key:默认类名首字母小写; value:对应实例对象
    private final Map<String, Object> ioc = new ConcurrentHashMap<>();

    // 开放访问的接口 key:url;value:method
    private final Map<String, Method> handlerMapping = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            // 6.委派,根据url去找到对应的Method，并且执行后后通过response返回
            doDispatch(req, res);
        } catch (IOException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            res.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void init(ServletConfig config) {
        // 1.加载配置文件
        loadConfiguration(config.getInitParameter(propertiesParmaName));

        // 2.扫描base-package下所有的类
        doScanner(properties.getProperty("base-package"));

        // 3.初始化ioc容器，将扫描到的相关类实例化，保存到ioc容器   (IoC部分)
        doInstance();

        // AOP代码应该在DI之前   AOP就是新生成的代理对象

        // 4.完成依赖注入 (DI部分)
        doAutowired();

        // 5.初始化HandlerMapping (MVC部分)
        initHandlerMapping();

        logger.info("HY Spring framework is init.");
    }

    private void loadConfiguration(String configuration) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configuration);
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String packageName) {
        String path = "/" + packageName.replaceAll("\\.", "/");
        URL resource = this.getClass().getClassLoader().getResource(path);
        File basePackage = new File(resource.getFile());

        for (File file : basePackage.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            }
            if (!file.getName().endsWith(".class")) {
                continue;
            }
            String className = packageName + "." + file.getName().replaceAll("\\.class", "");
            classNames.add(className);
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        try {

            for (String className : classNames) {

                Class<?> cla = Class.forName(className);
                if (!cla.isAnnotationPresent(HYController.class) && !cla.isAnnotationPresent(HYService.class)) {
                    continue;
                }

                String key = "";

                if (cla.isAnnotationPresent(HYController.class)) {
                    key = StrUtil.lowerCaseFist(cla.getSimpleName());

                } else if (cla.isAnnotationPresent(HYService.class)) {
                    // 1.默认首字母小写
                    // 2.在多个包出现相同类名，只能自定义全局唯一名字
                    // 3.如果接口多实现，只能抛异常
                    HYService service = cla.getAnnotation(HYService.class);
                    String value = service.value();
                    if (value.isEmpty()) {
                        key = StrUtil.lowerCaseFist(cla.getSimpleName());
                    } else {
                        key = StrUtil.lowerCaseFist(value);
                    }

                    for (Class<?> anInterface : cla.getInterfaces()) {
                        String name = anInterface.getName();
                        if (ioc.containsKey(name)) {
                            throw new Exception("The " + name + "is exist");
                        }
                    }
                }
                ioc.put(key, cla.newInstance());
            }

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {

                if (!field.isAnnotationPresent(HYAutoWired.class)) {
                    continue;
                }

                HYAutoWired r = field.getAnnotation(HYAutoWired.class);

                // 如果用户没有自自定义beanName，就默认根据类型注入
                String beanName = r.value().trim();
                if (beanName.isEmpty()) {
                    beanName = field.getType().getName();
                }

                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }


    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> cla = entry.getValue().getClass();
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
                url = url + "/" + requestMapping.value();
                handlerMapping.put(url.replaceAll("/+", "/"), method);
                logger.info("Method " + url + "," + method);
            }

        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse res) throws IOException, InvocationTargetException, IllegalAccessException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();

        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        if (!handlerMapping.containsKey(url)) {
            res.getWriter().write("404 Not Found");
            return;
        }

        Map<String, String[]> params = req.getParameterMap();

        Method method = handlerMapping.get(url);
        String beanName = method.getDeclaringClass().getSimpleName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] paramsValue = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                paramsValue[i] = req;
            } else if (parameterType == HttpServletResponse.class) {
                paramsValue[i] = res;
            } else if (parameterType == String.class) {
                // 通过运行时状态拿注解的值
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                for (Annotation[] parameterAnnotation : parameterAnnotations) {
                    for (Annotation annotation : parameterAnnotation) {
                        if (!(annotation instanceof HYRequestParam)) {
                            continue;
                        }
                        String paramName = ((HYRequestParam) annotation).value();
                        if (paramName.isEmpty()) {
                            continue;
                        }

                        paramsValue[i] = Arrays.toString(params.get(paramName))
                                .replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll("\\s+", "");
                    }
                }

            }
        }

        Object invoke = method.invoke(ioc.get(StrUtil.lowerCaseFist(beanName)), paramsValue);
        res.getWriter().write(String.valueOf(invoke));
    }

}
