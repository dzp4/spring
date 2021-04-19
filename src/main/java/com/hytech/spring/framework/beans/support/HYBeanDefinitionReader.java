package com.hytech.spring.framework.beans.support;

import com.hytech.spring.framework.beans.config.HYBeanDefinition;
import com.hytech.spring.hyframework.util.StrUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author dzp 2021/4/2
 */
public class HYBeanDefinitionReader {


    private final Properties contextConfig = new Properties();

    // 保存扫描的结果
    private List<String> registryBeanClasses = new ArrayList<>();

    public HYBeanDefinitionReader(String... configLocations) {
        doLoadConfig(configLocations[0]);

        // 扫描配置文件中相关的类
        doScanner(contextConfig.getProperty("base-package"));

    }

    public List<HYBeanDefinition> loadBeanDefinitions() {
        List<HYBeanDefinition> result = new ArrayList<>();
        try {

            for (String className : registryBeanClasses) {
                Class<?> beanCla = Class.forName(className);
                // todo 待完善
                // 保存类对应的classname（全类名）、beanName
                // beanName有三种
                // 1.默认（类名首字母小写）
                String beanName = StrUtil.lowerCaseFist(beanCla.getSimpleName());
                String beanClassName = beanCla.getName();
                result.add(doCreateBeanDefinition(beanName,beanClassName));
                // 2.自定义的
                // 3.接口注入
                for (Class<?> i : beanCla.getInterfaces()) {
                    result.add(doCreateBeanDefinition(i.getName(), beanCla.getName()));
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        return result;
    }

    private HYBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        HYBeanDefinition beanDefinition = new HYBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(beanName);
        return beanDefinition;
    }

    private void doLoadConfig(String configuration) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configuration.replaceAll("classpath:", ""));
        try {
            contextConfig.load(is);
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
            registryBeanClasses.add(className);
        }
    }

    public Properties getConfig() {
        return this.contextConfig;
    }


}
