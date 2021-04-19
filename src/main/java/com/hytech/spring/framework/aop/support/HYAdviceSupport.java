package com.hytech.spring.framework.aop.support;

import com.hytech.spring.framework.aop.aspect.HYAdvice;
import com.hytech.spring.framework.aop.conifg.HYAopConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dzp 2021/4/7
 */
public class HYAdviceSupport  {

    private HYAopConfig aopConfig;
    private Object target;
    private Class<?> targetClass;
    private Pattern pointCutPattern;

    private Map<Method, Map<String, HYAdvice>> methodCache;


    public HYAdviceSupport(HYAopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

    private void parse() {
        String pointCut = aopConfig.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");

        // 三段
        // 第一段：方法的修饰符和返回值
        // 第二段：类名
        // 第三段：方法的名称和形参列表

//        String pointCutForClassRegex = pointCut.substring(pointCut.lastIndexOf("(") - 4);
        // pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf("("))
//        pointCutPattern = Pattern.compile("class");
        methodCache = new HashMap<>();
        Pattern pointCutPattern = Pattern.compile(pointCut);

        Class<?> aspectClass = null;
        try {
            aspectClass = Class.forName(this.aopConfig.getAspectClass());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        HashMap<String, Method> aspectMethods = new HashMap<>();
        for (Method method : aspectClass.getMethods()) {
            aspectMethods.put(method.getName(), method);
        }
        // 以上都是初始化工作，准备阶段
        // 从这里开始封装HYAdvice
        for (Method method : this.targetClass.getMethods()) {
            String methodStr = method.toString();
            if (methodStr.contains("throws")) {
                methodStr = methodStr.substring(methodStr.lastIndexOf("throws"));
            }
            Matcher matcher = pointCutPattern.matcher(methodStr);
        }

    }

    public Map<String, HYAdvice> getAdvices(Method method, Object o) throws NoSuchMethodException {
        Map<String, HYAdvice> cache = methodCache.get(method);
        if (cache == null) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            this.methodCache.put(m, cache);
        }
        return cache;
    }

    public boolean pointCutMath() {
        return false;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setTargetClass(Class<?> cla) {
        this.targetClass = cla;
        parse();
    }
}
