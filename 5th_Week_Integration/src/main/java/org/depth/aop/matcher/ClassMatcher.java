package org.depth.aop.matcher;

public interface ClassMatcher {
    boolean matches(Class<?> clazz);

    ClassMatcher TRUE = clazz -> true;
}
