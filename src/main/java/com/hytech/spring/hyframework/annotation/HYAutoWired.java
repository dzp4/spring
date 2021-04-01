package com.hytech.spring.hyframework.annotation;

import java.lang.annotation.*;

/**
 * @author dzp 2021/3/31
 */
@Documented
@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HYAutoWired {

    String value() default "";

}
