package com.hytech.spring.busi.controller;

import com.hytech.spring.framework.webmvc.servlet.HYModelAndView;
import com.hytech.spring.hyframework.annotation.HYAutoWired;
import com.hytech.spring.hyframework.annotation.HYController;
import com.hytech.spring.hyframework.annotation.HYRequestMapping;
import com.hytech.spring.hyframework.annotation.HYRequestParam;
import com.hytech.spring.busi.service.HelloService;

import java.util.HashMap;
import java.util.Map;

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

    @HYRequestMapping("/aaa.*")
    public String aaa(@HYRequestParam("name") String name) {
        return helloService.say(name);
    }

    @HYRequestMapping("/first.html")
    public HYModelAndView first(@HYRequestParam("name") String name) {
        String say = helloService.say(name);
        Map<String, Object> model = new HashMap<>();
        model.put("data", say);
        model.put("token", "aacc");
        return new HYModelAndView("first.html", model);
    }


}
