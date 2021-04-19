package com.hytech.spring.framework.beans;

/**
 * @author dzp 2021/4/4
 */
public class HYBeanWrapper {

    private Object wrapperInstance;
    private Class<?> wrapperCla;

    public HYBeanWrapper(Object instance) {
        this.wrapperInstance = instance;
        this.wrapperCla = instance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public void setWrapperInstance(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
    }

    public Class<?> getWrapperCla() {
        return wrapperCla;
    }

    public void setWrapperCla(Class<?> wrapperCla) {
        this.wrapperCla = wrapperCla;
    }
}
