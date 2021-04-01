package com.hytech.spring.hyframework.annotation;

import java.lang.annotation.*;

/**
 * @author dzp 2021/3/31
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface HYRequestParam {
    String value() default "";
}
