package org.depth.servlet.http;

import lombok.EqualsAndHashCode;
import org.depth.http.HttpRequest;
import org.depth.http.model.HttpHeader;
import org.depth.http.model.HttpSession;
import org.depth.servlet.ServletRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class HttpServletRequest extends HttpRequest implements ServletRequest {
    private HttpSession session;
    
    // 기본 생성자
    public HttpServletRequest() {
        super();
        this.session = null;
    }
    
    // 복사 생성자
    public HttpServletRequest(String method, String path, String version, List<HttpHeader> headers, String body) {
        super(method, path, version, headers, body);
        this.session = null;
    }
    
    // 세션 유무 확인
    public boolean hasSession() {
        return session != null;
    }
    
    // 세션 반환 (없으면 null)
    public HttpSession getSession() {
        return session;
    }
    
    // 세션 설정
    public void setSession(HttpSession session) {
        this.session = session;
    }
    
    @Override
    public Object getAttribute(String name) {
        return switch (name) {
            case "method" -> getMethod();
            case "path" -> getPath();
            case "version" -> getVersion();
            case "headers" -> getHeaders();
            case "body" -> getBody();
            case "session" -> getSession();
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
                    List<HttpHeader> headers = new ArrayList<>();
                    for (Object o : list) {
                        if (o instanceof HttpHeader header) {
                            headers.add(header);
                        }
                    }
                    setHeaders(headers);
                } else {
                    throw new IllegalArgumentException("Headers must be a list of HttpHeader");
                }
            }
            case "body" -> setBody((String) value);
            case "session" -> {
                if (value instanceof HttpSession) {
                    setSession((HttpSession) value);
                } else {
                    throw new IllegalArgumentException("Session must be of type HttpSession");
                }
            }
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
