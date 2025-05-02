package org.depth.container.filter;

import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface Filter {
    void init();
    void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException;
    void destroy();
} 