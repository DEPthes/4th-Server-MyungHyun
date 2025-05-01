package org.depth.servlet.http;

import lombok.EqualsAndHashCode;
import org.depth.http.HttpRequest;
import org.depth.http.model.HttpHeader;
import org.depth.servlet.ServletRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@SuperBuilder
public class HttpServletRequest extends HttpRequest implements ServletRequest {
    @Override
    public Object getAttribute(String name) {
        return switch (name) {
            case "method" -> getMethod();
            case "path" -> getPath();
            case "version" -> getVersion();
            case "headers" -> getHeaders();
            case "body" -> getBody();
            default -> null;
        };
    }

    @Override
    public void setAttribute(String name, Object value) {
        switch (name) {
            case "method" -> setMethod((String) value);
            case "path" -> setPath((String) value);
            case "version" -> setVersion((String) value);
            case "headers" -> {
                if (value instanceof List<?> list) {
                    setHeaders(list.stream()
                            .map(o -> (HttpHeader) o)
                            .toList());
                } else {
                    throw new IllegalArgumentException("Headers must be a list of HttpHeader");
                }
            }
            case "body" -> setBody((String) value);
        }
    }

    @Override
    public InputStream getContent() {
        String rawRequest = this.toRawRequest();
        return new ByteArrayInputStream(rawRequest.getBytes());
    }

    @Override
    public String getProtocol() {
        return getVersion();
    }
}
