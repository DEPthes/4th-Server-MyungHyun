package org.depth.web.context;

import lombok.SneakyThrows;
import org.depth.web.container.HttpServletContainer;
import org.depth.web.servlet.http.HttpServlet;

public class WebServerApplicationContext extends GenericWebApplicationContext {
    private final HttpServletContainer servletContainer;

    public WebServerApplicationContext() {
        this.servletContainer = new HttpServletContainer();
    }

    public WebServerApplicationContext(HttpServletContainer servletContainer) {
        this.servletContainer = servletContainer;
    }

    public void registerServlet(HttpServlet servlet) {
        servletContainer.registerServlet(servlet);
    }

    @SneakyThrows
    public void start(int port) {
        servletContainer.start(port);
    }

    public void stop() {
        servletContainer.stop();
    }
}
