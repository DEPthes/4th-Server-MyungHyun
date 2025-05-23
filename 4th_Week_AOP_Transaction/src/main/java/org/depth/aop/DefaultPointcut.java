package org.depth.aop;

import org.depth.aop.Pointcut;
import org.depth.aop.matcher.ClassMatcher;
import org.depth.aop.matcher.MethodMatcher;

public class DefaultPointcut implements Pointcut {

    private ClassMatcher classMatcher;   
    private MethodMatcher methodMatcher;

    public DefaultPointcut() {
        this.classMatcher = ClassMatcher.TRUE; 
        this.methodMatcher = MethodMatcher.TRUE;
    }

    public DefaultPointcut(MethodMatcher methodMatcher) {
        this(ClassMatcher.TRUE, methodMatcher); 
    }

    public DefaultPointcut(ClassMatcher classMatcher, MethodMatcher methodMatcher) { 
        this.classMatcher = (classMatcher != null) ? classMatcher : ClassMatcher.TRUE; 
        this.methodMatcher = (methodMatcher != null) ? methodMatcher : MethodMatcher.TRUE;
    }

    public void setClassMatcher(ClassMatcher classMatcher) { 
        this.classMatcher = (classMatcher != null) ? classMatcher : ClassMatcher.TRUE; 
    }

    public void setMethodMatcher(MethodMatcher methodMatcher) {
        this.methodMatcher = (methodMatcher != null) ? methodMatcher : MethodMatcher.TRUE;
    }

    @Override
    public ClassMatcher getClassMatcher() { 
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