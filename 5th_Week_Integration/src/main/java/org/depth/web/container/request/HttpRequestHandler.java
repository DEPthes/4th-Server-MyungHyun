package org.depth.web.container.request;

import lombok.RequiredArgsConstructor;
import org.depth.web.container.PathRoutingServletMap;
import org.depth.web.container.filter.Filter;
import org.depth.web.container.filter.FilterChain;
import org.depth.web.container.listener.RequestListener;
import org.depth.web.container.session.SessionManager;
import org.depth.web.http.handler.HttpRequestParser;
import org.depth.web.http.handler.HttpResponseWriter;
import org.depth.web.http.model.HttpHeader;
import org.depth.web.http.model.HttpSession;
import org.depth.web.servlet.Servlet;
import org.depth.web.servlet.http.HttpServletRequest;
import org.depth.web.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class HttpRequestHandler implements RequestHandler {
    private final PathRoutingServletMap pathRoutingServletMap;
    private final List<Filter> filters;
    private final SessionManager sessionManager;
    private final List<RequestListener> globalRequestListeners = new ArrayList<>();
    
    // 세션 쿠키 이름
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";

    // 요청 리스너 추가
    public void addRequestListener(RequestListener listener) {
        globalRequestListeners.add(listener);
    }
    
    // 요청 리스너 제거
    public void removeRequestListener(RequestListener listener) {
        globalRequestListeners.remove(listener);
    }

    // 클라이언트 요청 처리
    @Override
    public void handle(Socket clientSocket) {
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            request = HttpRequestParser.parse(reader);
            response = HttpResponseWriter.createDefaultResponse();
            
            // 전역 요청 리스너 등록
            for (RequestListener listener : globalRequestListeners) {
                request.addRequestListener(listener);
            }
            
            // 세션 처리
            processSession(request, response);
            
            // 요청 초기화 이벤트 발생
            boolean continueProcessing = request.notifyRequestInitialized(response);
            
            if (continueProcessing) {
                try {
                    Servlet bestMatchingServlet = pathRoutingServletMap.findBestMatchingFor(request.getPath());
    
                    if (bestMatchingServlet != null) {
                        FilterChain filterChain = new FilterChain(filters, bestMatchingServlet);
                        filterChain.doFilter(request, response);
                    } else {
                        response = HttpResponseWriter.createNotFoundResponse(request.getPath());
                    }
                } catch (Exception e) {
                    // 오류 처리 이벤트 발생
                    if (!request.notifyRequestError(response, e)) {
                        // 오류가 처리되지 않은 경우 500 응답 생성
                        response = HttpResponseWriter.createServerErrorResponse(e.getMessage());
                    }
                }
            }
            
            // 요청 완료 이벤트 발생
            request.notifyRequestCompleted(response);

            outputStream.write(response.getContent());
            outputStream.flush();
        } catch (IOException e) {
            // 요청 처리 중 예외가 발생하면 오류 이벤트 발생
            if (request != null && response != null) {
                request.notifyRequestError(response, e);
            }
            throw new RuntimeException(e);
        }
    }
    
    // 세션 처리 로직
    private void processSession(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 세션 ID 추출
        String sessionId = extractSessionIdFromCookies(request);
        
        // 세션 ID로 세션 조회 또는 생성
        HttpSession session;
        if (sessionId != null) {
            session = sessionManager.getSession(sessionId);
        } else {
            session = null;
        }
        
        // 세션이 없거나 만료된 경우 새 세션 생성
        if (session == null) {
            session = sessionManager.createSession();
            // 응답에 세션 쿠키 추가
            response.addHeader("Set-Cookie", SESSION_COOKIE_NAME + "=" + session.getId() + "; Path=/; HttpOnly");
        }
        
        // 요청에 세션 설정
        request.setSession(session);
    }
    
    // 쿠키에서 세션 ID 추출
    private String extractSessionIdFromCookies(HttpServletRequest request) {
        Optional<HttpHeader> cookieHeader = request.getHeaders().stream()
                .filter(header -> "Cookie".equalsIgnoreCase(header.getName()))
                .findFirst();
        
        if (cookieHeader.isPresent()) {
            String cookieValue = cookieHeader.get().getValue();
            String[] cookies = cookieValue.split(";");
            
            for (String cookie : cookies) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && SESSION_COOKIE_NAME.equals(parts[0])) {
                    return parts[1];
                }
            }
        }
        
        return null;
    }
}
