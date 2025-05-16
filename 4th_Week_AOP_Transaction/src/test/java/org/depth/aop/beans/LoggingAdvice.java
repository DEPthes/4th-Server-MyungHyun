package org.depth.aop.beans;

import org.depth.aop.AroundAdvice;
import org.depth.aop.invocation.MethodInvocation;

import java.lang.reflect.Method;
import java.util.Arrays;

// 테스트용 Advice 구현체 (MethodInterceptor 역할)
public class LoggingAdvice implements AroundAdvice {

    public LoggingAdvice() {} // BeanFactory가 생성할 수 있도록 기본 생성자

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object target = invocation.getThis(); // 원본 타겟 객체
        Object[] args = invocation.getArguments();

        System.out.println("[AOP LOG] BEFORE: " + target.getClass().getSimpleName() + "." + method.getName() + " with args " + Arrays.toString(args));
        Object result;
        try {
            result = invocation.proceed(); // 다음 Advice 또는 타겟 메서드 호출
            System.out.println("[AOP LOG] AFTER_RETURNING: " + target.getClass().getSimpleName() + "." + method.getName() + ", result: " + result);
            // 결과 조작은 신중해야 하지만, 필요한 경우 가능
            // if (result instanceof String) {
            //     return "[LOGGED VIA MethodInvocation] " + result;
            // }
            return result;
        } catch (Throwable e) {
            System.out.println("[AOP LOG] AFTER_THROWING: " + target.getClass().getSimpleName() + "." + method.getName() + ", exception: " + e.getMessage());
            throw e;
        }
    }
}