package org.depth.beans.factory.context;

import org.depth.beans.factory.ListableBeanFactory;
import org.depth.beans.factory.exception.BeansException;

public class GenericApplicationContext implements ApplicationContext {
    private final ListableBeanFactory beanFactory;
    private long startupDate;

    public GenericApplicationContext() {
        this.beanFactory = new ListableBeanFactory();
    }

    public GenericApplicationContext(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public String getApplicationName() {
        return "";
    }

    @Override
    public long getStartupDate() {
        return startupDate;
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return beanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return beanFactory.getBean(name, requiredType);
    }

    @Override
    public boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }
}
