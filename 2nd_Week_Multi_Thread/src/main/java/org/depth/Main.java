package org.depth;

import org.depth.container.HttpServletContainer;
import org.depth.container.listener.LoggingRequestListener;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;

        HttpServletContainer container = new HttpServletContainer();

        CustomServlet servlet = new CustomServlet();

        container.registerRequestListener(new LoggingRequestListener());

        container.registerServlet(servlet);

        container.start(8080);
    }
}