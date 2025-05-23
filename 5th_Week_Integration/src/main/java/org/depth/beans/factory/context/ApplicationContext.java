package org.depth.beans.factory.context;

import org.depth.beans.factory.BeanFactory;

public interface ApplicationContext extends BeanFactory {
    String getApplicationName();

    long getStartupDate();
}
