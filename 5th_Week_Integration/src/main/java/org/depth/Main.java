package org.depth;

import org.depth.beans.BeanDefinition;
import org.depth.web.context.WebServerApplicationContext;
import org.depth.web.servlet.DispatcherServlet;

public class Main {
    public static void main(String[] args) {
        WebServerApplicationContext context = new WebServerApplicationContext();
        context.registerBeanDefinition("testController", new BeanDefinition("testController", TestController.class));

        DispatcherServlet servlet = new DispatcherServlet();
        servlet.setWebApplicationContext(context);
        context.registerServlet(servlet);

        context.start(8080);
    }
}