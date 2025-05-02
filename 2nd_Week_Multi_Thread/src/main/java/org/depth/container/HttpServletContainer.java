package org.depth.container;

import lombok.RequiredArgsConstructor;
import org.depth.container.request.HttpRequestHandler;
import org.depth.servlet.http.HttpServlet;

import java.util.List;


public class HttpServletContainer {
    private final PathRoutingServletMap servletMap;
    private final HttpRequestHandler httpRequestHandler;

    public HttpServletContainer() {
        this.servletMap = new PathRoutingServletMap();
        this.httpRequestHandler = new HttpRequestHandler(this.servletMap);
    }

    public void registerServlet(HttpServlet servlet) {
        servlet.init();
        this.servletMap.put(servlet.getServletPath(), servlet);
    }

    public void unregisterServlet(HttpServlet servlet) {
        servlet.destroy();
        this.servletMap.remove(servlet.getServletPath());
    }
}
