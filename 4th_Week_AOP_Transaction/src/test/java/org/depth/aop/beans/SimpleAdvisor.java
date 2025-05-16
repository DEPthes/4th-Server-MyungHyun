package org.depth.aop.beans;

import org.depth.aop.Advisor;   // 프레임워크의 Advisor
import org.depth.aop.AroundAdvice;
import org.depth.aop.Pointcut;  // 프레임워크의 Pointcut

// 테스트용 Advisor 구현체
public class SimpleAdvisor implements Advisor {
    private final Pointcut pointcut;
    private final AroundAdvice advice;

    public SimpleAdvisor() {
        this.pointcut = new SpecificMethodPointcut(MyService.class, "performAction");
        this.advice = new LoggingAdvice(); // LoggingAdvice도 기본 생성자가 있어야 함
    }

    // 특정 Pointcut과 Advice를 외부에서 주입받는 생성자 (테스트 설정에서 직접 생성 시 사용 가능)
    public SimpleAdvisor(Pointcut pointcut, AroundAdvice advice) {
        this.pointcut = pointcut;
        this.advice = advice;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public AroundAdvice getAdvice() {
        return advice;
    }
}