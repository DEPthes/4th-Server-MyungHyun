package org.depth.beans.testbeans;


import lombok.Getter;

public class CircularDepBeanB {
    @Getter
    private final CircularDepBeanA beanA;

    public CircularDepBeanB(CircularDepBeanA beanA) {
        System.out.println("CircularDepBeanB constructor called.");
        this.beanA = beanA;
    }

    public void methodB() {
        System.out.println("Method B in CircularDepBeanB");
        // beanA.methodA(); // StackOverflowError를 피하기 위해 직접 호출은 주석 처리
    }
}