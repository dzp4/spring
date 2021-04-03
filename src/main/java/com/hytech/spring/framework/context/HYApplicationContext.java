package com.hytech.spring.framework.context;

import com.hytech.spring.framework.bean.config.HYBeanDefinition;
import com.hytech.spring.framework.bean.support.HYBeanDefinitionReader;

import java.util.HashMap;
import java.util.Map;

/**
 * 职责：完成bean的创建和DI
 *
 * @author dzp 2021/4/2
 */
public class HYApplicationContext {

    private String[] configLocations;

    private Map<String, HYBeanDefinition> beanDefinitionMap = new HashMap<>();

    private HYBeanDefinitionReader reader;

    public HYApplicationContext(String... configLocations) {

        reader = new HYBeanDefinitionReader(configLocations);

        // 1.加载配置文件
        // 2.解析配置文件，封装成BeanDefinition
        // 3.把BeanDefinition缓存起来

    }

    public Object getBean(String beanName) {
        return null;
    }

    public Object getBean(Class<?> cla) {
        return getBean(cla.getName());
    }

}
