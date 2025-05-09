package org.depth.beans.testbeans;

import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
public class TestBeanC {
    private String name;

    public TestBeanC(String name) { // 오버로딩된 생성자 (테스트용)
        System.out.println("TestBeanC constructor(String) called with: " + name);
        this.name = name;
    }


    public void doSomethingInC() {
        System.out.println("TestBeanC.doSomethingInC() called. Name: " + (name != null ? name : "DefaultName"));
    }

    public String getName() {
        return name;
    }
}