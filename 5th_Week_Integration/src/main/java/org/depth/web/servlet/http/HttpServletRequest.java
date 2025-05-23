package org.depth.web.servlet.http;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.depth.web.container.listener.RequestListener;
import org.depth.web.http.HttpRequest;
import org.depth.web.http.model.HttpHeader;
import org.depth.web.http.model.HttpSession;
import org.depth.web.servlet.ServletRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class HttpServletRequest extends HttpRequest implements ServletRequest {
    // 세션 설정
    // 세션 반환 (없으면 null)
    private HttpSession session;
    private final Map<String, Object> requestAttributes = new HashMap<>();
    private final List<RequestListener> listeners = new ArrayList<>();
    
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

    /**
     * 요청 리스너를 등록합니다.
     * @param listener 요청 리스너
     */
    public void addRequestListener(RequestListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 요청 리스너를 제거합니다.
     * @param listener 요청 리스너
     */
    public void removeRequestListener(RequestListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 요청 속성을 설정합니다.
     * @param name 속성 이름
     * @param value 속성 값
     */
    public void setRequestAttribute(String name, Object value) {
        Object oldValue = requestAttributes.put(name, value);
        
        // 속성이 새로 추가되거나 변경된 경우 리스너 알림
        if (oldValue != value) {
            notifyAttributeAdded(name, value);
        }
    }
    
    /**
     * 요청 속성을 조회합니다.
     * @param name 속성 이름
     * @return 속성 값 (없으면 null)
     */
    public Object getRequestAttribute(String name) {
        return requestAttributes.get(name);
    }
    
    /**
     * 요청 속성을 제거합니다.
     * @param name 속성 이름
     */
    public void removeRequestAttribute(String name) {
        if (requestAttributes.containsKey(name)) {
            requestAttributes.remove(name);
            notifyAttributeRemoved(name);
        }
    }
    
    /**
     * 요청이 초기화되었음을 리스너에게 알립니다.
     * @param response HTTP 응답
     * @return 요청 처리를 계속해야 하면 true
     */
    public boolean notifyRequestInitialized(HttpServletResponse response) {
        boolean continueProcessing = true;
        
        for (RequestListener listener : listeners) {
            if (!listener.requestInitialized(this, response)) {
                continueProcessing = false;
            }
        }
        
        return continueProcessing;
    }
    
    /**
     * 요청이 완료되었음을 리스너에게 알립니다.
     * @param response HTTP 응답
     */
    public void notifyRequestCompleted(HttpServletResponse response) {
        for (RequestListener listener : listeners) {
            listener.requestCompleted(this, response);
        }
    }
    
    /**
     * 요청 처리 중 오류가 발생했음을 리스너에게 알립니다.
     * @param response HTTP 응답
     * @param exception 발생한 예외
     * @return 예외가 처리되었으면 true
     */
    public boolean notifyRequestError(HttpServletResponse response, Throwable exception) {
        boolean errorHandled = false;
        
        for (RequestListener listener : listeners) {
            if (listener.requestError(this, response, exception)) {
                errorHandled = true;
            }
        }
        
        return errorHandled;
    }
    
    /**
     * 요청 속성이 추가되었음을 리스너에게 알립니다.
     */
    private void notifyAttributeAdded(String name, Object value) {
        for (RequestListener listener : listeners) {
            listener.attributeAdded(this, name, value);
        }
    }
    
    /**
     * 요청 속성이 제거되었음을 리스너에게 알립니다.
     */
    private void notifyAttributeRemoved(String name) {
        for (RequestListener listener : listeners) {
            listener.attributeRemoved(this, name);
        }
    }
    
    @Override
    public Object getAttribute(String name) {
        // 먼저 요청 속성에서 찾기
        Object attribute = requestAttributes.get(name);
        if (attribute != null) {
            return attribute;
        }
        
        // 기존 상속 속성에서 찾기
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
        // 기존 요청 객체 속성 설정
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
            default -> setRequestAttribute(name, value);
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
