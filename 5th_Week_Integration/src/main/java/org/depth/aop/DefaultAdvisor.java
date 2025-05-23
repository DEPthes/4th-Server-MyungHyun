package org.depth.aop;

public class DefaultAdvisor implements Advisor {
    private final Pointcut pointcut;
    private final AroundAdvice advice; // 현재는 AroundAdvice만 지원한다고 가정

    public DefaultAdvisor(Pointcut pointcut, AroundAdvice advice) {
        if (pointcut == null) {
            throw new IllegalArgumentException("Pointcut must not be null");
        }
        if (advice == null) {
            throw new IllegalArgumentException("Advice must not be null");
        }
        this.pointcut = pointcut;
        this.advice = advice;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public AroundAdvice getAdvice() { // Advisor 인터페이스가 AroundAdvice를 반환하도록 정의되었다고 가정
        return this.advice;
    }
}
