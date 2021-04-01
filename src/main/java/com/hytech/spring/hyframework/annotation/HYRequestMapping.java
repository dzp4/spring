package com.hytech.spring.hyframework.annotation;

import java.lang.annotation.*;

/**
 * @author dzp 2021/3/31
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HYRequestMapping {

    String value() default "";
}
