package com.hytech.spring.framework.webmvc.servlet;

import com.hytech.spring.hyframework.annotation.HYRequestParam;
import com.hytech.spring.hyframework.util.StrUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dzp 2021/4/5
 */
public class HYHandlerAdapter {

    public HYModelAndView handler(HttpServletRequest req, HttpServletResponse res, HYHandlerMapping handler) throws InvocationTargetException, IllegalAccessException {

        // 保存形参列表，将参数名称和参数的位置，这种关系保存起来
        Map<String, Integer> paramIndexMapping = new HashMap<>();
        // 通过运行时状态拿注解的值
        Annotation[][] pa = handler.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation annotation : pa[i]) {
                if (!(annotation instanceof HYRequestParam)) {
                    continue;
                }
                String paramName = ((HYRequestParam) annotation).value();
                if (paramName.isEmpty()) {
                    continue;
                }
                paramIndexMapping.put(paramName, i);
            }
        }
        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> parameterType = paramTypes[i];
            if (parameterType == HttpServletRequest.class||parameterType == HttpServletResponse.class) {
                paramIndexMapping.put(parameterType.getName(), i);
            }
        }

        // 去拼接实参列表
        Map<String, String[]> params = req.getParameterMap();
        Object[] paramsValues = new Object[paramTypes.length];

        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(params.get(param.getKey())).replaceAll("\\[|\\]", "").replaceAll("s+", "");

            if (paramIndexMapping.containsKey(value)) {
                Integer index = paramIndexMapping.get(value);
                // 允许自定义的类型转换器Converter
                paramsValues[index] = castStringValue(value, paramTypes[index]);
            }

        }

        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            Integer index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramsValues[index] = req;
        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            Integer index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramsValues[index] = res;
        }

        Object result = handler.getMethod().invoke(handler.getController(), paramsValues);

        if (result == null || result instanceof Void) {
            return null;
        }
        boolean isModelAndView = handler.getMethod().getReturnType() == HYModelAndView.class;
        if (isModelAndView) {
            return (HYModelAndView) result;
        }

        return null;
    }

    // todo 待优化
    private Object castStringValue(String value, Class<?> paramType) {

        if (String.class == paramType) {
            return value;
        } else if (Integer.class == paramType) {
            return Integer.valueOf(value);
        }

        return null;
    }

}
