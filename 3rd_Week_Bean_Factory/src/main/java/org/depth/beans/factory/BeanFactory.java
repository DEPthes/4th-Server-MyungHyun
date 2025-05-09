package org.depth.beans.factory;

import org.depth.beans.factory.exception.BeansException;

public interface BeanFactory {
    Object getBean(String name) throws BeansException;

    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

    boolean containsBean(String name);
}
