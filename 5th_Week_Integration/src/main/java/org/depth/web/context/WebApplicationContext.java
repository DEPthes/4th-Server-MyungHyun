package org.depth.web.context;

import org.depth.beans.factory.context.ApplicationContext;
import org.depth.web.servlet.Servlet;

public interface WebApplicationContext extends ApplicationContext {
    Servlet getServlet();
}
