package org.depth.web.container.filter;


import org.depth.web.servlet.http.HttpServletRequest;
import org.depth.web.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface Filter {
    void init();
    void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException;
    void destroy();
} 