package org.depth.aop.proxy;


import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.depth.aop.Advisor;
import org.depth.aop.AroundAdvice;
import org.depth.aop.invocation.ReflectiveMethodInvocation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class DynamicAdvisedInterceptor implements MethodInterceptor {

    private final Object target; // 실제 대상 객체
    private final List<Advisor> advisors; // 이 빈에 적용 가능한 모든 Advisor 목록

    public DynamicAdvisedInterceptor(Object target, List<Advisor> advisors) {
        this.target = target;
        this.advisors = advisors;
    }


    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Class<?> targetClass = this.target.getClass();

        // 1. 현재 호출된 메서드에 적용될 AroundAdvice 목록을 필터링
        List<AroundAdvice> chain = this.advisors.stream()
                .filter(advisor -> advisor.getPointcut().getMethodMatcher().matches(method, targetClass))
                .map(Advisor::getAdvice) // Advisor가 AroundAdvice를 반환한다고 가정
                .collect(Collectors.toList());

        // 2. 적용할 Advice가 없다면, 원본 타겟 메서드 실행 (리플렉션 또는 methodProxy.invoke(target, args))
        if (chain.isEmpty()) {
            // return methodProxy.invoke(this.target, args); // CGLIB MethodProxy 사용 가능
            return method.invoke(this.target, args);      // 또는 직접 리플렉션 사용
        }

        // 3. MethodInvocation을 생성하고 Advice 체인 실행 시작
        ReflectiveMethodInvocation invocation = new ReflectiveMethodInvocation(
                proxy, this.target, method, args, targetClass, chain
        );

        return invocation.proceed();
    }
}