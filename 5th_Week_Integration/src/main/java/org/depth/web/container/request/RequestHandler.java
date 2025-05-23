package org.depth.web.container.request;

import java.net.Socket;

public interface RequestHandler {
    void handle(Socket clientSocket);
}
