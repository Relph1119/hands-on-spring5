package com.teapot.vip.spring.framework.aop.config;

import lombok.Data;

@Data
public class TPAopConfig {
    private String pointCut;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectClass;
    private String aspectAfterThrow;
    private String aspectAfterThrowing;
    private String aspectAfterThrowingName;


}
