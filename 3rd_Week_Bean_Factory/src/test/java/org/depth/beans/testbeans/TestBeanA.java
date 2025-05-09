package org.depth.beans.testbeans;

import lombok.Getter;
import lombok.Setter;

@Getter
public class TestBeanA {
    private final TestBeanB beanB; // 생성자 주입
    @Setter
    private TestBeanC beanC;       // Setter 주입
    @Setter
    private String message;

    public TestBeanA(TestBeanB beanB) {
        System.out.println("TestBeanA constructor(TestBeanB) called.");
        this.beanB = beanB;
    }

    public void doSomethingInA() {
        System.out.println("TestBeanA.doSomethingInA() called. Message: " + message);
        if (beanB != null) {
            beanB.doSomethingInB();
        }
        if (beanC != null) {
            beanC.doSomethingInC();
        }
    }
}