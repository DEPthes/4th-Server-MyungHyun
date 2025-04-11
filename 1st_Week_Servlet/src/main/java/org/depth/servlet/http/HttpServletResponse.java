package org.depth.servlet.http;

import org.depth.http.HttpResponse;
import org.depth.http.model.HttpHeader;
import org.depth.servlet.ServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpServletResponse extends HttpResponse implements ServletResponse {
    @Override
    public byte[] getContent() {
        return this.toRawResponse().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Object getAttribute(String name) {
        return switch (name) {
            case "version" -> this.getVersion();
            case "statusCode" -> this.getStatusCode();
            case "statusText" -> this.getStatusText();
            case "headers" -> this.getHeaders();
            case "body" -> this.getBody();
            default -> null;
        };
    }

    @Override
    public void setAttribute(String name, Object value) {
        switch (name) {
            case "version" -> this.setVersion((String) value);
            case "statusCode" -> this.setStatusCode((int) value);
            case "statusText" -> this.setStatusText((String) value);
            case "headers" -> {
                if (value instanceof List<?> list) {
                    setHeaders(list.stream()
                            .map(o -> (HttpHeader) o)
                            .toList());
                } else {
                    throw new IllegalArgumentException("Headers must be a list of HttpHeader");
                }
            }
            case "body" -> this.setBody((String) value);
        };
    }

    @Override
    public String getProtocol() {
        return this.getVersion();
    }
}
