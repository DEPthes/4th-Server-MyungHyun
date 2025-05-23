package org.depth.aop.matcher;


import org.depth.aop.utils.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AnnotationMethodMatcher implements MethodMatcher {
    private final Class<? extends Annotation> annotationType;

    public AnnotationMethodMatcher(Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            throw new IllegalArgumentException("Annotation type must not be null");
        }
        this.annotationType = annotationType;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        // 메서드 자체에 어노테이션이 있는지 확인 (가장 구체적)
        Method specificMethodToCheck = AnnotationUtils.getSpecificMethodFromClassIfPossible(method, targetClass);
        if (specificMethodToCheck.isAnnotationPresent(this.annotationType)) {
            return true;
        }
        // 원본 메서드 객체(예: 인터페이스 메서드)에도 어노테이션이 있는지 확인
        if (method != specificMethodToCheck && method.isAnnotationPresent(this.annotationType)) {
            return true;
        }

        // 메서드에 어노테이션이 없는 경우, 클래스 레벨 어노테이션 확인
        Annotation classLevelAnnotation = AnnotationUtils.findAnnotationOnClassOrInterfaces(targetClass, this.annotationType);
        if (classLevelAnnotation != null) {
            // 클래스 레벨 어노테이션은 public 메서드에 적용 (일반적인 @Transactional 규칙)
            return Modifier.isPublic(method.getModifiers());
        }
        return false;
    }

    public Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }

    @Override
    public String toString() {
        return "AnnotationMethodMatcher [annotationType=" + annotationType.getName() + "]";
    }
}