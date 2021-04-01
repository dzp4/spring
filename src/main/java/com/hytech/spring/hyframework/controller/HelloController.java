package com.hytech.spring.hyframework.controller;

import com.hytech.spring.hyframework.annotation.HYController;
import com.hytech.spring.hyframework.annotation.HYRequestMapping;
import com.hytech.spring.hyframework.annotation.HYRequestParam;

/**
 * @author dzp 2021/3/31
 */
@HYController
@HYRequestMapping("/hello")
public class HelloController {

    @HYRequestMapping("/say")
    public String say(@HYRequestParam("name") String name) {
        System.out.println("invoke say method");
        return "Hello " + name;
    }

}
