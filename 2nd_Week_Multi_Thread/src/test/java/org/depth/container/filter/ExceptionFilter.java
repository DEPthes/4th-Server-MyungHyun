package org.depth.container.filter;

import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ExceptionFilter implements Filter {
    private final String exceptionMessage;
    private boolean initialized = false;
    private boolean destroyed = false;

    public ExceptionFilter(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    @Override
    public void init() {
        initialized = true;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
        // 의도적으로 예외 발생
        throw new IOException(exceptionMessage);
    }

    @Override
    public void destroy() {
        destroyed = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
} 