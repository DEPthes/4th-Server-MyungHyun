package org.depth.aop.beans;

public class AnotherService {

    public String greet() {
        System.out.println("AnotherService: greet executed");
        return "Hello from AnotherService";
    }
}