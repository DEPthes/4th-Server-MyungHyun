package org.depth.servlet;

import java.io.InputStream;


public interface ServletRequest {
    Object getAttribute(String name);

    void setAttribute(String name, Object value);

    InputStream getContent();

    String getProtocol();
}
