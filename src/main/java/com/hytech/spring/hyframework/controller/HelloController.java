package com.hytech.spring.hyframework.controller;

import com.hytech.spring.hyframework.annotation.HYAutoWired;
import com.hytech.spring.hyframework.annotation.HYController;
import com.hytech.spring.hyframework.annotation.HYRequestMapping;
import com.hytech.spring.hyframework.annotation.HYRequestParam;
import com.hytech.spring.hyframework.service.HelloService;

/**
 * @author dzp 2021/3/31
 */
@HYController
@HYRequestMapping("/hello")
public class HelloController {

    @HYAutoWired("helloServiceImpl")
    private HelloService helloService;

    @HYRequestMapping("/say")
    public String say(@HYRequestParam("name") String name) {
        return helloService.say(name);
    }

}
