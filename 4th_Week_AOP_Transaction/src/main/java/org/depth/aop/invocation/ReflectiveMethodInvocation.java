package org.depth.aop.invocation;

import org.depth.aop.AroundAdvice;

import java.lang.reflect.Method;
import java.util.List;

public class ReflectiveMethodInvocation implements MethodInvocation {

    protected final Object proxy; // 프록시 객체 (필요한 경우)
    protected final Object target; // 원본 타겟 객체
    protected final Method method; // 호출된 메서드
    protected Object[] arguments; // 메서드 인자
    private final Class<?> targetClass; // 타겟 클래스

    private final List<AroundAdvice> interceptorsAndDynamicMethodMatchers; // 적용될 Advice 목록

    private int currentInterceptorIndex = -1; // 현재 실행 중인 Advice 인덱스

    public ReflectiveMethodInvocation(
            Object proxy, Object target, Method method, Object[] arguments,
            Class<?> targetClass, List<AroundAdvice> interceptorsAndDynamicMethodMatchers) {

        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = method;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public Object getThis() {
        return this.target; // 원본 타겟 객체 반환
    }

    @Override
    public Object proceed() throws Throwable {
        // 모든 Advice가 실행되었거나, Advice가 없는 경우
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return invokeJoinpoint(); // 타겟 메서드 직접 호출
        }

        // 다음 Advice 실행
        this.currentInterceptorIndex++;
        AroundAdvice interceptor = this.interceptorsAndDynamicMethodMatchers.get(this.currentInterceptorIndex);

        // 다음 Advice의 invoke 메서드 호출, MethodInvocation 자신을 넘김
        return interceptor.invoke(this);
    }

    /**
     * 타겟 객체의 원본 메서드를 리플렉션을 사용하여 호출합니다.
     */
    protected Object invokeJoinpoint() throws Throwable {
        // 접근성 처리 (필요한 경우)
//         Method originalMethod = AopUtils.getMostSpecificMethod(this.method, this.targetClass);
//         ReflectionUtils.makeAccessible(originalMethod);
//         return originalMethod.invoke(this.target, this.arguments);
        return this.method.invoke(this.target, this.arguments); // 간단하게 직접 호출
    }

}