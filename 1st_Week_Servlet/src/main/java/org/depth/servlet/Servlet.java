package org.depth.servlet;

public interface Servlet {
    void init(ServletConfig config);

    void service(ServletRequest request, ServletResponse response);

    void destroy();
}
