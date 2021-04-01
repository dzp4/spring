package com.hytech.spring.hyframework.service;

import com.hytech.spring.hyframework.annotation.HYService;

/**
 * @author dzp 2021/4/1
 */
@HYService
public class HelloServiceImpl implements HelloService {

    @Override
    public String say(String name) {
        return "hello " + name;
    }
}
