package org.depth.servlet.http;

import org.depth.servlet.Servlet;
import org.depth.servlet.ServletConfig;
import org.depth.servlet.ServletRequest;
import org.depth.servlet.ServletResponse;


public abstract class HttpServlet implements Servlet, ServletConfig {
    private ServletConfig config;
    
    @Override
    public void init(ServletConfig config) {
        this.config = config;
    }

    @Override
    public void destroy() {
        // 해제할 리소스가 있을시 해제
    }

    @Override
    public String getServletName() {
        return config.getServletName();
    }

    @Override
    public String getServletPath() {
        return config.getServletPath();
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) {
        //Default Behavior
        response.setStatusCode(405);
        response.setStatusText("Method Not Allowed");
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) {
        HttpServletRequest request;
        HttpServletResponse response;

        try {
            request = (HttpServletRequest) req;
            response = (HttpServletResponse) res;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid request or response type", e);
        }

        service(request, response);
    }

    @Override
    public String getProtocol() {
        return "HTTP/1.1";
    }
}
