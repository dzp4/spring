package com.hytech.spring.framework.aop;

import com.hytech.spring.framework.aop.aspect.HYAdvice;
import com.hytech.spring.framework.aop.support.HYAdviceSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author dzp 2021/4/7
 */
public class JdkDynamicAopProxy implements InvocationHandler {

    private HYAdviceSupport config;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // todo should be Map<Method,List<Advice>>
        Map<String, HYAdvice> advices = config.getAdvices(method, null);

        try {
            advices.get("before").invoke();
            advices.get("after").invoke();

        } catch (Exception e) {
            advices.get("afterThrow").invoke();
        }

        return null;
    }


    public Object getProxy() {
        return null;
    }

}
