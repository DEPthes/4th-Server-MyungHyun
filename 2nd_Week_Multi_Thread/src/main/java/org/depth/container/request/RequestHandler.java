package org.depth.container.request;

import org.depth.servlet.ServletRequest;
import org.depth.servlet.ServletResponse;

import java.net.Socket;

public interface RequestHandler {
    ServletResponse handle(Socket clientSocket);
}
