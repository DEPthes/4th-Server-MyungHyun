package org.depth.container.request;

import lombok.RequiredArgsConstructor;
import org.depth.container.PathRoutingServletMap;
import org.depth.container.filter.Filter;
import org.depth.container.filter.FilterChain;
import org.depth.http.HttpRequest;
import org.depth.http.handler.HttpRequestParser;
import org.depth.http.handler.HttpResponseWriter;
import org.depth.servlet.Servlet;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

@RequiredArgsConstructor
public class HttpRequestHandler implements RequestHandler {
    private final PathRoutingServletMap pathRoutingServletMap;
    private final List<Filter> filters;

    @Override
    public void handle(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            HttpServletRequest request = HttpRequestParser.parse(reader);
            HttpServletResponse response = HttpResponseWriter.createDefaultResponse();

            Servlet bestMatchingServlet = pathRoutingServletMap.findBestMatchingFor(request.getPath());

            if (bestMatchingServlet != null) {
                FilterChain filterChain = new FilterChain(filters, bestMatchingServlet);
                filterChain.doFilter(request, response);
            } else {
                response = HttpResponseWriter.createNotFoundResponse(request.getPath());
            }

            outputStream.write(response.getContent());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
