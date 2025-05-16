package org.depth.aop;

import org.depth.aop.invocation.MethodInvocation;

public interface AroundAdvice {
    Object invoke(MethodInvocation invocation) throws Throwable;
}
