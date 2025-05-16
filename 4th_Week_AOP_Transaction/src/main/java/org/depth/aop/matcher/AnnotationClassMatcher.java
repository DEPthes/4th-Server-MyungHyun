package org.depth.aop.matcher;

import org.depth.aop.utils.AnnotationUtils;

import java.lang.annotation.Annotation;

public class AnnotationClassMatcher implements ClassMatcher {
    private final Class<? extends Annotation> annotationType;

    public AnnotationClassMatcher(Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            throw new IllegalArgumentException("Annotation type must not be null");
        }
        this.annotationType = annotationType;
    }

    @Override
    public boolean matches(Class<?> clazz) {
        // 클래스 자체 또는 계층 내, 혹은 메서드 중 하나라도 어노테이션이 있으면
        // 초기 프록시 대상 선별에는 포함될 수 있도록 함.
        if (AnnotationUtils.findAnnotationOnClassOrInterfaces(clazz, this.annotationType) != null) {
            return true;
        }
        return AnnotationUtils.hasAnnotatedMethod(clazz, this.annotationType);
    }

    public Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }

    @Override
    public String toString() {
        return "AnnotationClassMatcher [annotationType=" + annotationType.getName() + "]";
    }
}