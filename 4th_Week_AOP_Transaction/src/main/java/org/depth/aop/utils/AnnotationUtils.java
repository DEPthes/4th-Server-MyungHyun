package org.depth.aop.utils;


import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
public class AnnotationUtils {
    public static <A extends Annotation> A findAnnotationOnClassOrInterfaces(Class<?> clazz, Class<A> annotationType) {
        if (clazz == null || annotationType == null) {
            return null;
        }
        Set<Class<?>> visited = new HashSet<>();
        return findAnnotationOnClassOrInterfacesRecursive(clazz, annotationType, visited);
    }

    private static <A extends Annotation> A findAnnotationOnClassOrInterfacesRecursive(
            Class<?> clazz, Class<A> annotationType, Set<Class<?>> visited) {
        if (clazz == null || clazz == Object.class || !visited.add(clazz)) {
            return null;
        }
        A annotation = clazz.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            annotation = findAnnotationOnClassOrInterfacesRecursive(ifc, annotationType, visited);
            if (annotation != null) {
                return annotation;
            }
        }
        return findAnnotationOnClassOrInterfacesRecursive(clazz.getSuperclass(), annotationType, visited);
    }

    public static boolean hasAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotationType) {
        if (clazz == null || annotationType == null) {
            return false;
        }
        Set<Class<?>> visited = new HashSet<>();
        return hasAnnotatedMethodRecursive(clazz, annotationType, visited);
    }

    private static boolean hasAnnotatedMethodRecursive(Class<?> clazz, Class<? extends Annotation> annotationType, Set<Class<?>> visited) {
        if (clazz == null || clazz == Object.class || !visited.add(clazz)) {
            return false;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationType)) {
                return true;
            }
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            // 인터페이스에 직접 선언된 메서드에 대한 어노테이션 확인
            if (hasAnnotatedMethodOnDeclaredMethods(ifc, annotationType, visited)) {
                return true;
            }
            // 인터페이스가 다른 인터페이스를 상속할 경우 재귀적으로 확인 (이미 visited로 처리)
            if (hasAnnotatedMethodRecursive(ifc, annotationType, visited)) {
                return true;
            }
        }
        return hasAnnotatedMethodRecursive(clazz.getSuperclass(), annotationType, visited);
    }

    // hasAnnotatedMethodRecursive에서 인터페이스의 declaredMethods만 확인하도록 보조
    private static boolean hasAnnotatedMethodOnDeclaredMethods(Class<?> clazz, Class<? extends Annotation> annotationType, Set<Class<?>> visited) {
        if (clazz == null || !clazz.isInterface() || !visited.add(clazz)) { // 인터페이스만, visited는 중복 방지
            return false;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationType)) {
                return true;
            }
        }
        return false;
    }


    public static Method getSpecificMethodFromClassIfPossible(Method method, Class<?> targetClass) {
        try {
            return targetClass.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return method;
        }
    }
}