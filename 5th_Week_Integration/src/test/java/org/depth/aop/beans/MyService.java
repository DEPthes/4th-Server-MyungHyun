package org.depth.aop.beans;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class MyService {
    private DependencyBean dependency;

    public String performAction(String param) {
        System.out.println("MyService: performAction executed with param - " + param);
        if (dependency != null) {
            dependency.doSomething();
        }
        return "MyService: Result for " + param;
    }

    public void anotherAction() {
        System.out.println("MyServiceImpl: anotherAction executed");
    }

    public void setDependency(DependencyBean dependency) {
        this.dependency = dependency;
    }

    public DependencyBean getDependency() {
        return this.dependency;
    }
}