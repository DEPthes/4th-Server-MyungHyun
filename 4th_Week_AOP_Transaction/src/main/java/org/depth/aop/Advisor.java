package org.depth.aop;

public interface Advisor {
    Pointcut getPointcut();
    AroundAdvice getAdvice();
}
