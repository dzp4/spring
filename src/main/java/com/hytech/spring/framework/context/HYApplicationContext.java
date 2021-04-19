package com.hytech.spring.framework.context;

import com.hytech.spring.framework.aop.JdkDynamicAopProxy;
import com.hytech.spring.framework.aop.conifg.HYAopConfig;
import com.hytech.spring.framework.aop.support.HYAdviceSupport;
import com.hytech.spring.framework.beans.HYBeanWrapper;
import com.hytech.spring.framework.beans.config.HYBeanDefinition;
import com.hytech.spring.framework.beans.support.HYBeanDefinitionReader;
import com.hytech.spring.hyframework.annotation.HYAutoWired;
import com.hytech.spring.hyframework.annotation.HYController;
import com.hytech.spring.hyframework.annotation.HYService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 职责：完成bean的创建和DI
 * 工厂类
 *
 * @author dzp 2021/4/2
 */
public class HYApplicationContext {

    private String[] configLocations;

    private Map<String, HYBeanDefinition> beanDefinitionMap = new HashMap<>();

    private HYBeanDefinitionReader reader;

    // 保存包装对象
    private Map<String, HYBeanWrapper> factoryBeanInstanceCache = new HashMap<>();
    // 保存原始对象
    private Map<String, Object> factoryBeanObjectCache = new HashMap<>();


    public HYApplicationContext(String... configLocations) {
        // 1.加载配置文件
        reader = new HYBeanDefinitionReader(configLocations);
        // 2.解析配置文件，封装成BeanDefinition
        List<HYBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        // 3.把BeanDefinition缓存起来
        doRegisterBeanDefinition(beanDefinitions);
        //
        doAutoWired();
    }

    private void doAutoWired() {
        // 调用getBean()
        // 这一步所有的bean并没有真正的实例化，还只是配置阶段
        for (Map.Entry<String, HYBeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            Object bean = getBean(beanName);
        }

    }

    private void doRegisterBeanDefinition(List<HYBeanDefinition> beanDefinitions) {

        for (HYBeanDefinition beanDefinition : beanDefinitions) {
            String factoryBeanName = beanDefinition.getFactoryBeanName();
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanDefinitionMap.containsKey(factoryBeanName) || beanDefinitionMap.containsKey(beanClassName)) {
                // todo
                throw new RuntimeException("The bean is exist");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }

    }

    // bean的实例化，DI是从这里开始的
    public Object getBean(String beanName) {
        // 1.拿到BeanDefinition配置信息，
        HYBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        // 2.反射实例化
        Object instance = instantiateBean(beanName, beanDefinition);
        // 3.封装成BeanWrapper
        HYBeanWrapper beanWrapper = new HYBeanWrapper(instance);
        // 4.保存到IoC容器
        factoryBeanInstanceCache.put(beanName, beanWrapper);
        // 5.执行注入
        populateBean(beanName, beanDefinition, beanWrapper);

        return beanWrapper.getWrapperInstance();
    }


    public Object getBean(Class<?> cla) {
        return getBean(cla.getName());
    }

    // 创建真正的实例对象
    private Object instantiateBean(String beanName, HYBeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            if (this.factoryBeanObjectCache.containsKey(beanName)) {
                instance = factoryBeanObjectCache.get(beanName);
            }
            Class<?> cla = Class.forName(beanClassName);
            instance = cla.newInstance();

            // 如果满足条件，就直接返回proxy对象
            // 1.加载配置文件
            HYAdviceSupport config = instantiateAopConfig(beanDefinition);
            config.setTarget(instance);
            config.setTargetClass(cla);

            // 判断规则，要不要生成代理类，如果要就覆盖原生对象
            if(config.pointCutMath()){
                instance = new JdkDynamicAopProxy().getProxy();
            }

            this.factoryBeanObjectCache.put(beanName, instance);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return instance;
    }

    private HYAdviceSupport instantiateAopConfig(HYBeanDefinition beanDefinition) {
        HYAopConfig aopConfig = new HYAopConfig();

        aopConfig.setPointCut(this.reader.getConfig().getProperty("point-cut"));
        aopConfig.setAspectClass(this.reader.getConfig().getProperty("aspect-class"));
        aopConfig.setAspectBefore(this.reader.getConfig().getProperty("aspect-before"));
        aopConfig.setAspectAfter(this.reader.getConfig().getProperty("aspect-after"));
        aopConfig.setAspectAfterThrow(this.reader.getConfig().getProperty("aspect-after-throw"));
        aopConfig.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspect-after-throwing-Name"));





        return null;
    }

    private void populateBean(String beanName, HYBeanDefinition beanDefinition, HYBeanWrapper beanWrapper) {
        // 可能会涉及到循环依赖
        // 循环依赖：两个类相互依赖，也就是有可能出现依赖一个还没实例化的类，那么就通过两次循环去解决
        // 第一次循环可以将所有类实例化，第二次再将没有注入的类给注入进去
        
        Object instance = beanWrapper.getWrapperInstance();
        Class<?> wrapperCla = beanWrapper.getWrapperCla();

        // todo 在spring中component
        if (!wrapperCla.isAnnotationPresent(HYController.class) && !wrapperCla.isAnnotationPresent(HYService.class)) {
            return;
        }

        for (Field field : wrapperCla.getDeclaredFields()) {
            if (!field.isAnnotationPresent(HYAutoWired.class)) {
                continue;
            }

            HYAutoWired r = field.getAnnotation(HYAutoWired.class);

            // 如果用户没有自自定义beanName，就默认根据类型注入
            String autowiredBeanName = r.value().trim();
            if (autowiredBeanName.isEmpty()) {
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);

            try {
                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null) {
                    continue;
                }
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }


    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[getBeanDefinitionCount()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }

}
