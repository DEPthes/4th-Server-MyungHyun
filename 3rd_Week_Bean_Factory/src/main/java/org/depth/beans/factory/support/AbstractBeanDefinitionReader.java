package org.depth.beans.factory.support;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.depth.beans.factory.BeanDefinitionRegistry;

@RequiredArgsConstructor
public abstract class AbstractBeanDefinitionReader {
    @NonNull
    private final BeanDefinitionRegistry registry;

    protected BeanDefinitionRegistry getRegistry() {
        return registry;
    }

    public abstract int loadBeanDefinitions(String location) throws Exception;
    public abstract int loadBeanDefinitions(String... locations) throws Exception;
}
