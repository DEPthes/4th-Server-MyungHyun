package org.depth.container.request;

import lombok.RequiredArgsConstructor;
import org.depth.container.PathRoutingServletMap;
import org.depth.http.handler.HttpRequestParser;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

@RequiredArgsConstructor
public class HttpRequestHandler implements RequestHandler {
    private final PathRoutingServletMap pathRoutingServletMap;

    @Override
    public HttpServletResponse handle(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            HttpServletRequest request = HttpRequestParser.parse(reader);
            HttpServletResponse response = new HttpServletResponse();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
