package org.depth.beans.testbeans;

import lombok.Getter;

@Getter
public class TestBeanB {
    private final TestBeanC beanC; // 생성자 주입

    public TestBeanB(TestBeanC beanC) {
        System.out.println("TestBeanB constructor(TestBeanC) called.");
        this.beanC = beanC;
    }

    public void doSomethingInB() {
        System.out.println("TestBeanB.doSomethingInB() called.");
        if (beanC != null) {
            beanC.doSomethingInC();
        }
    }
}