package org.depth.servlet;

public interface Servlet {
    void init();

    void service(ServletRequest request, ServletResponse response);

    void destroy();

    String getServletName();

    String getServletPath();

    String getProtocol();
}
