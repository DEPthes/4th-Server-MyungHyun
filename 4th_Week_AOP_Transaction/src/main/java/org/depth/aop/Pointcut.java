package org.depth.aop;

import org.depth.aop.matcher.ClassMatcher;
import org.depth.aop.matcher.MethodMatcher;

public interface Pointcut {
    ClassMatcher getClassMatcher();

    MethodMatcher getMethodMatcher();

    Pointcut TRUE = new Pointcut() {
        @Override
        public ClassMatcher getClassMatcher() {
            return ClassMatcher.TRUE;
        }

        @Override
        public MethodMatcher getMethodMatcher() {
            return MethodMatcher.TRUE;
        }
    };
}
