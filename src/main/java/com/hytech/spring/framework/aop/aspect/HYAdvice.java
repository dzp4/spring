package com.hytech.spring.framework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author dzp 2021/4/7
 */
@Data
public class HYAdvice {

    private Object aspect;

    private Method adviceMethod;

    private String throwName;

    public HYAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }
}
