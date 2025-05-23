package org.depth.web.servlet.http;


import org.depth.web.servlet.Servlet;
import org.depth.web.servlet.ServletRequest;
import org.depth.web.servlet.ServletResponse;

public abstract class HttpServlet implements Servlet {
    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        // 해제할 리소스가 있을시 해제
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
