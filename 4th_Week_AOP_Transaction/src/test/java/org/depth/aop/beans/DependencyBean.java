package org.depth.aop.beans;

// 의존성 주입 테스트용 빈
public class DependencyBean {
    public DependencyBean() {
        // System.out.println("DependencyBean: Constructor called");
    }
    public void doSomething() {
        System.out.println("DependencyBean: doSomething executed");
    }
}
