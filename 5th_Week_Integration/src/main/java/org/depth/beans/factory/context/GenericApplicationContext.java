package org.depth.beans.factory.context;

import org.depth.beans.BeanDefinition;
import org.depth.beans.factory.BeanDefinitionRegistry;
import org.depth.beans.factory.ListableBeanFactory;
import org.depth.beans.factory.exception.BeansException;

public class GenericApplicationContext implements ApplicationContext, BeanDefinitionRegistry {
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

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanFactory.getBeanDefinition(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanFactory.containsBeanDefinition(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanFactory.getBeanDefinitionCount();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanFactory.getBeanDefinitionNames();
    }
}
