package com.hytech.spring.framework.aop.conifg;

import lombok.Data;

/**
 * @author dzp 2021/4/7
 */
@Data
public class HYAopConfig {

    private String pointCut;

    private String aspectClass;

    private String aspectBefore;

    private String aspectAfter;

    private String aspectAfterThrow;

    private String aspectAfterThrowingName;

}
