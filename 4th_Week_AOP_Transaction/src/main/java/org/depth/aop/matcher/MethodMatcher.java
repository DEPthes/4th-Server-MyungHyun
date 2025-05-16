package org.depth.aop.matcher;

import java.lang.reflect.Method;

public interface MethodMatcher {
    boolean matches(Method method, Class<?> targetClass);

    MethodMatcher TRUE = (method, targetClass) -> true;
}
