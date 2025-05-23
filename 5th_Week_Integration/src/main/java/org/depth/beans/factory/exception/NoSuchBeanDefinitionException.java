package org.depth.beans.factory.exception;

public class NoSuchBeanDefinitionException extends BeansException{
    public NoSuchBeanDefinitionException(String beanName) {
        super("No bean named '" + beanName + "' is defined");
    }
}
