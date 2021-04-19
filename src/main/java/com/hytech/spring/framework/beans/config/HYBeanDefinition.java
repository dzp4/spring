package com.hytech.spring.framework.beans.config;

/**
 * @author dzp 2021/4/2
 */
public class HYBeanDefinition {

    private String factoryBeanName;

    private String beanClassName;


    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}
