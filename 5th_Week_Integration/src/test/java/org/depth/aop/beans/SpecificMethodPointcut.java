package org.depth.aop.beans;

import org.depth.aop.Pointcut; // 프레임워크의 Pointcut 인터페이스
import org.depth.aop.matcher.ClassMatcher;
import org.depth.aop.matcher.MethodMatcher;

import java.lang.reflect.Method;

// 테스트용 Pointcut 구현체 (특정 클래스의 특정 메서드 이름 매칭)
public class SpecificMethodPointcut implements Pointcut {
    private final Class<?> targetClassToMatch;
    private final String methodNameRegexToMatch;

    public SpecificMethodPointcut(Class<?> targetClassToMatch, String methodNameRegexToMatch) {
        this.targetClassToMatch = targetClassToMatch;
        this.methodNameRegexToMatch = methodNameRegexToMatch;
    }

    public boolean matches(Class<?> targetClass) {
        return this.targetClassToMatch.isAssignableFrom(targetClass);
    }

    public boolean matches(Method method, Class<?> targetClass) {
        return matches(targetClass) && method.getName().matches(methodNameRegexToMatch);
    }

    @Override
    public ClassMatcher getClassMatcher() {
        return SpecificMethodPointcut.this::matches;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return SpecificMethodPointcut.this::matches;
    }
}