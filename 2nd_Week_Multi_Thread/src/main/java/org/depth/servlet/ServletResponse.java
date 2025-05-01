package org.depth.servlet;

import java.io.OutputStream;

public interface ServletResponse {
    byte[] getContent();

    Object getAttribute(String name);

    void setAttribute(String name, Object value);

    String getProtocol();
}
