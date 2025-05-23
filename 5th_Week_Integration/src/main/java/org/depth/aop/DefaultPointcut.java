package org.depth.aop;

import org.depth.aop.Pointcut;
import org.depth.aop.matcher.ClassMatcher;
import org.depth.aop.matcher.MethodMatcher;

public class DefaultPointcut implements Pointcut {

    private ClassMatcher classMatcher;   // 변경됨
    private MethodMatcher methodMatcher;

    public DefaultPointcut() {
        this.classMatcher = ClassMatcher.TRUE; // 변경됨
        this.methodMatcher = MethodMatcher.TRUE;
    }

    public DefaultPointcut(MethodMatcher methodMatcher) {
        this(ClassMatcher.TRUE, methodMatcher); // 변경됨
    }

    public DefaultPointcut(ClassMatcher classMatcher, MethodMatcher methodMatcher) { // 변경됨
        this.classMatcher = (classMatcher != null) ? classMatcher : ClassMatcher.TRUE; // 변경됨
        this.methodMatcher = (methodMatcher != null) ? methodMatcher : MethodMatcher.TRUE;
    }

    public void setClassMatcher(ClassMatcher classMatcher) { // 변경됨
        this.classMatcher = (classMatcher != null) ? classMatcher : ClassMatcher.TRUE; // 변경됨
    }

    public void setMethodMatcher(MethodMatcher methodMatcher) {
        this.methodMatcher = (methodMatcher != null) ? methodMatcher : MethodMatcher.TRUE;
    }

    @Override
    public ClassMatcher getClassMatcher() { // 변경됨
        return this.classMatcher;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this.methodMatcher;
    }

    @Override
    public String toString() {
        return "DefaultPointcut: ClassMatcher=" + classMatcher + ", MethodMatcher=" + methodMatcher;
    }
}