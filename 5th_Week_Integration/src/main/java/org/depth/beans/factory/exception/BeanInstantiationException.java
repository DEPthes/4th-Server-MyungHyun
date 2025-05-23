package org.depth.beans.factory.exception;

public class BeanInstantiationException extends BeanCreationException {
    public BeanInstantiationException(String beanName, String message, Throwable cause) {
        super(beanName, "Could not instantiate bean class: " + message, cause);
    }
    public BeanInstantiationException(String beanName, String message) {
        super(beanName, "Could not instantiate bean class: " + message);
    }
}
