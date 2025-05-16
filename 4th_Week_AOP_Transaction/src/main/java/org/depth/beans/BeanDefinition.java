package org.depth.beans;


import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class BeanDefinition {
    public BeanDefinition(String beanName, Class<?> beanClass) {
        this.beanName = beanName;
        this.beanClass = beanClass;
    }

    private final String beanName;
    private final Class<?> beanClass;
    private final List<String> constructorArgBeanNames = new ArrayList<>(); // 생성자 주입을 위한 의존성 Bean 이름 목록
    private final Map<String, String> propertyBeanNames = new HashMap<>();

    public void addConstructorArgBeanName(String beanName) {
        constructorArgBeanNames.add(beanName);
    }

    public void addPropertyBeanName(String propertyName, String beanName) {
        propertyBeanNames.put(propertyName, beanName);
    }
}
