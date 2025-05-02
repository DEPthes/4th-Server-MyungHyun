package org.depth;

import org.depth.container.HttpServletContainer;
import org.depth.container.ServletContainer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;

        HttpServletContainer container = new HttpServletContainer();

        CustomServlet servlet = new CustomServlet();

        container.registerServlet(servlet);

        container.start(8080);
    }
}