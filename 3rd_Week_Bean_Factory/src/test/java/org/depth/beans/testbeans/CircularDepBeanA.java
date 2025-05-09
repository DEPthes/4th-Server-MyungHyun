package org.depth.beans.testbeans;

import lombok.Getter;

public class CircularDepBeanA {
    @Getter
    private final CircularDepBeanB beanB;
    @Getter
    private final TestBeanC someOtherBean; // 순환과 관계없는 빈

    public CircularDepBeanA(CircularDepBeanB beanB, TestBeanC someOtherBean) {
        System.out.println("CircularDepBeanA constructor called.");
        this.beanB = beanB;
        this.someOtherBean = someOtherBean;
    }

    public void methodA() {
        System.out.println("Method A in CircularDepBeanA");
        beanB.methodB();
    }
}
